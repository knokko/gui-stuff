package procmodel.compiler

import org.antlr.v4.runtime.BailErrorStrategy
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import procmodel.compiler.generated.ProcModelBaseListener
import procmodel.compiler.generated.ProcModelLexer
import procmodel.compiler.generated.ProcModelParser
import procmodel.exceptions.PmCompileError
import procmodel.importer.PmImporter
import procmodel.lang.functions.PmBuiltinFunctions
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.instructions.PmInstructionType
import procmodel.lang.types.*
import procmodel.program.*
import java.io.File
import java.io.PrintWriter
import java.lang.RuntimeException

class PmCompiler<VertexValue : PmValue>(
    private val importer: PmImporter<VertexValue>,
    private val extraFunctions: Map<String, Int>,
    private val extraTypes: List<PmType>,
    private val isChild: Boolean
) : ProcModelBaseListener() {

    private val baseInstructions = mutableListOf<PmInstruction>()
    private var instructions = baseInstructions
    private val dynamicDeclarations = mutableListOf<MutableList<PmInstruction>>()
    private val dynamicChildInstructions = mutableListOf<MutableList<PmInstruction>>()
    private var isInsideDynamicDeclaration = false
    private var isInsideChildParametersBlock = false
    private val staticParameters = mutableMapOf<String, PmType>()
    private val dynamicParameters = mutableMapOf<String, PmType>()
    private val types = PmTypes()
    private val functions = PmFunctions()
    private val importedModelIDs = mutableMapOf<String, String>()

    private val loopIndexStack = mutableListOf<Int>()
    private val functionIndexStack = mutableListOf<Int>()

    lateinit var program: PmProgram

    private fun getParameterCountOfBuiltinFunction(name: String): Int? {
        if (name == "outputValue") return 1
        val builtinFunction = PmBuiltinFunctions.MAP[name]
        if (builtinFunction != null) return builtinFunction.parameterTypes.size
        return extraFunctions[name]
    }

    override fun visitErrorNode(node: ErrorNode?) {
        println("Encountered error $node")
    }

    override fun enterStart(ctx: ProcModelParser.StartContext?) {
        types.pushScope()

        for (type in PmBuiltinTypes.ALL) {
            // I'm planning to get rid of ANY in the future
            if (type != PmBuiltinTypes.ANY) types.defineType(type.name, type)
        }
        for (type in extraTypes) {
            types.defineType(type.name, type)
        }

        functions.pushScope()
    }

    override fun exitStart(ctx: ProcModelParser.StartContext?) {
        types.popScope()
        functions.popScope()

        instructions.add(PmInstruction.exitProgram(ctx!!.stop.line))

        val children = mutableMapOf<String, PmChildProgram>()
        if (!isChild) {
            for (childProgram in importer.cache.models.values) {

                children[childProgram.id] = PmChildProgram(
                    this.instructions.size,
                    childProgram.program.staticParameters,
                    childProgram.program.dynamicParameters
                )

                for (instruction in childProgram.program.body.instructions) {
                    if (instruction.type == PmInstructionType.CreateDynamicMatrix || instruction.type == PmInstructionType.CreateChildModel) {
                        val indexInstruction = instructions.removeLast()
                        val oldIndex = indexInstruction.value!!.intValue()

                        val offset = if (instruction.type == PmInstructionType.CreateDynamicMatrix) {
                            dynamicDeclarations.size
                        } else dynamicChildInstructions.size

                        val newIndex = PmInt(oldIndex + offset)
                        instructions.add(PmInstruction.pushValue(newIndex, indexInstruction.lineNumber))
                    }
                    instructions.add(instruction)
                }
                this.dynamicDeclarations.addAll(childProgram.program.dynamicMatrices.map {
                    it.instructions.toMutableList()
                })
                this.dynamicChildInstructions.addAll(childProgram.program.childInvocations.map {
                    it.instructions.toMutableList()
                })
            }
            val instructionDump = PrintWriter(File("instructions.txt"))
            for ((index, instruction) in instructions.withIndex()) {
                instructionDump.println(String.format("%s, // index %d", instruction, index))
            }
            instructionDump.println("---- DYNAMIC DECLARATIONS ----")
            for ((blockIndex, instructions) in this.dynamicDeclarations.withIndex()) {
                for ((index, instruction) in instructions.withIndex()) {
                    instructionDump.println(String.format("%d %d %s", blockIndex, index, instruction))
                }
                instructionDump.println("-----------------------------------------")
            }
            instructionDump.println("---- DYNAMIC CHILD PARAMETER BLOCKS ----")
            for ((childIndex, instructions) in this.dynamicChildInstructions.withIndex()) {
                for ((index, instruction) in instructions.withIndex()) {
                    instructionDump.println(String.format("%d %d %s", childIndex, index, instruction))
                }
                instructionDump.println("-----------------------------------------")
            }
            instructionDump.flush()
            instructionDump.close()
        }

        program = PmProgram(
            body = PmProgramBody(instructions.toList()),
            dynamicMatrices = dynamicDeclarations.map { PmDynamicMatrixConstructor(it.toList()) },
            childInvocations = dynamicChildInstructions.map { PmParameterPropagator(it.toList()) },
            children = children.toMap(),
            staticParameters = staticParameters.toMap(),
            dynamicParameters = dynamicParameters.toMap()
        )
    }

    override fun enterDynamicDeclaration(ctx: ProcModelParser.DynamicDeclarationContext?) {
        val identifiers = ctx!!.IDENTIFIER()
        val typeName = identifiers[0].text
        if (typeName != "Matrix") throw PmCompileError("Matrix is currently the only dynamic type")

        if (identifiers.size % 2 == 0) throw PmCompileError("Missing name or type for transfer variable ${identifiers.last().text}")

        for (index in 1 until identifiers.size step 2) {
            val transferTypeName = identifiers[index].text
            val transferVariableName = identifiers[index + 1].text
            val transferVariableType = types.getType(transferTypeName)
            instructions.add(PmInstruction.transferVariable(transferVariableName, transferVariableType, ctx.start.line))
        }
    }

    override fun enterDynamicDeclarationBlock(ctx: ProcModelParser.DynamicDeclarationBlockContext?) {
        if (isInsideDynamicDeclaration) throw PmCompileError("Nested dynamic declarations are forbidden")
        isInsideDynamicDeclaration = true

        val dynamicIndex = dynamicDeclarations.size
        instructions.add(PmInstruction.pushValue(PmInt(dynamicIndex), ctx!!.start.line))
        instructions.add(PmInstruction.createDynamicMatrix(ctx.start.line))

        val dynamicDeclaration = mutableListOf<PmInstruction>()
        dynamicDeclarations.add(dynamicDeclaration)
        instructions = dynamicDeclaration
    }

    override fun exitDynamicDeclarationBlock(ctx: ProcModelParser.DynamicDeclarationBlockContext?) {
        if (!isInsideDynamicDeclaration) throw PmCompileError("There is no dynamic declaration to exit")
        instructions.add(PmInstruction.exitProgram(ctx!!.stop.line))
        isInsideDynamicDeclaration = false

        instructions = baseInstructions
    }

    override fun exitDynamicDeclaration(ctx: ProcModelParser.DynamicDeclarationContext?) {
        if (ctx!!.PARAMETER_TYPE().text != "dynamic") throw PmCompileError("Dynamic declarations must be dynamic")
    }

    override fun enterFunctionDeclaration(ctx: ProcModelParser.FunctionDeclarationContext?) {

        // This ensures that the program will 'jump over the function declaration'
        // it prevents the function from being invoked when it is declared
        instructions.add(PmInstruction.pushValue(PmBoolean(true), ctx!!.start.line))
        functionIndexStack.add(instructions.size)
        instructions.add(PmInstruction.pushValue(PmInt(-1), ctx.start.line))
        instructions.add(PmInstruction.jump(ctx.start.line))

        // And store the actual function
        val startInstruction = instructions.size

        // This ensures that all parameters are defined
        val parameters = (2 until ctx.IDENTIFIER().size step 2).map { rawIndex ->
            val parameterType = types.getType(ctx.IDENTIFIER(rawIndex).text)
            val parameterName = ctx.IDENTIFIER(rawIndex + 1).text
            Pair(parameterType, parameterName)
        }

        instructions.add(PmInstruction.pushScope(ctx.start.line))
        for ((parameterType, parameterName) in parameters.reversed()) {
            instructions.add(PmInstruction.declareVariable(parameterName, parameterType, ctx.start.line))
        }

        val returnType = types.getType(ctx.IDENTIFIER(0).text)
        val function = PmFunction(startInstruction, returnType)

        val name = ctx.IDENTIFIER(1).text
        val signature = PmFunctionSignature(name, parameters.size)

        functions.defineUserFunction(signature, function)
    }

    override fun exitFunctionDeclaration(ctx: ProcModelParser.FunctionDeclarationContext?) {

        // The result is implicitly none when no result expression is given
        if (ctx!!.expression() == null) {
            instructions.add(PmInstruction.pushValue(PmNone(), ctx.stop.line))
        }

        // These two instructions are basically a type check
        instructions.add(PmInstruction.declareVariable("\$result", types.getType(ctx.IDENTIFIER(0).text), ctx.stop.line))
        instructions.add(PmInstruction.pushVariable("\$result", ctx.stop.line))

        // Swap the result with the return address
        instructions.add(PmInstruction.swap(ctx.stop.line))

        val returnInstructionAddress = instructions.size + 5
        instructions.add(PmInstruction.pushValue(PmInt(returnInstructionAddress), ctx.stop.line))

        // Jump offset = return address - address of return jump
        instructions.add(PmInstruction.subtract(ctx.stop.line))

        instructions.add(PmInstruction.pushValue(PmBoolean(true), ctx.stop.line))
        instructions.add(PmInstruction.swap(ctx.stop.line))
        instructions.add(PmInstruction.popScope(ctx.stop.line))
        instructions.add(PmInstruction.jump(ctx.stop.line))

        // Finish the 'function declaration jump' that was created during enterFunctionDeclaration
        val jumpOverIndex = functionIndexStack.removeLast()
        val targetIndex = instructions.size
        instructions[jumpOverIndex] = PmInstruction.pushValue(PmInt(targetIndex - jumpOverIndex - 1), ctx.start.line)
    }

    override fun exitParameterDeclaration(ctx: ProcModelParser.ParameterDeclarationContext?) {
        val typeName = ctx!!.IDENTIFIER(0).text
        val type = types.getType(typeName)
        if (type == PmBuiltinTypes.ANY) throw PmCompileError("Parameters of type Any are forbidden")
        if (type == PmBuiltinTypes.VOID) throw PmCompileError("Parameters of type void are forbidden")
        val name = ctx.IDENTIFIER(1).text

        if (ctx.PARAMETER_TYPE().text == "static") {
            if (staticParameters.containsKey(name)) throw PmCompileError("Duplicate static parameter $name")
            staticParameters[name] = type
        } else if (ctx.PARAMETER_TYPE().text == "dynamic") {
            if (dynamicParameters.containsKey(name)) throw PmCompileError("Duplicate dynamic parameter $name")
            dynamicParameters[name] = type
        } else {
            throw PmCompileError("Unknown parameter type " + ctx.PARAMETER_TYPE().text)
        }
    }

    override fun enterFunctionInvocation(ctx: ProcModelParser.FunctionInvocationContext?) {
        val functionName = ctx!!.IDENTIFIER().text
        val builtinParameterCount = getParameterCountOfBuiltinFunction(functionName)
        if (builtinParameterCount == null) {

            // I need to remember the return address via this instruction, but I don't know its value, yet
            // I will supply it during exitFunctionInvocation()
            functionIndexStack.add(instructions.size)
            instructions.add(PmInstruction.pushValue(PmInt(-1), ctx.start.line))
        }
    }

    override fun exitFunctionInvocation(ctx: ProcModelParser.FunctionInvocationContext?) {
        val functionName = ctx!!.IDENTIFIER().text
        val builtinParameterCount = getParameterCountOfBuiltinFunction(functionName)
        if (builtinParameterCount != null) {
            if (ctx.expression().size != builtinParameterCount) {
                throw PmCompileError(
                    "Built-in function $functionName requires $builtinParameterCount parameters, but got ${ctx.expression().size}",
                    ctx.start.line, ctx.start.charPositionInLine
                )
            }

            instructions.add(PmInstruction.invokeBuiltinFunction(functionName, ctx.start.line))
        } else {
            val functionSignature = PmFunctionSignature(functionName, ctx.expression().size)
            val function = functions.getUserFunction(functionSignature)

            // Jump to the function
            instructions.add(PmInstruction.pushValue(PmBoolean(true), ctx.start.line))

            val jumpInstructionIndex = instructions.size + 1
            instructions.add(PmInstruction.pushValue(
                PmInt(function.startInstruction - jumpInstructionIndex), ctx.start.line
            ))
            instructions.add(PmInstruction.jump(ctx.start.line))

            // Fix the 'remember return address' instruction from enterFunctionInvocation()...
            instructions[functionIndexStack.removeLast()] = PmInstruction.pushValue(
                PmInt(jumpInstructionIndex + 1), ctx.start.line
            )
        }
    }

    override fun exitImportValue(ctx: ProcModelParser.ImportValueContext?) {
        val isRelative = ctx!!.importPath().relativeImportPrefix() != null
        val relativePath = "/" + ctx.importPath().relativeImportPath().text
        val importedValue = importer.importValue(relativePath, isRelative, extraFunctions, extraTypes)
        val typeName = ctx.IDENTIFIER().text
        val name = ctx.importAlias()?.IDENTIFIER()?.text ?: ctx.importPath().relativeImportPath().IDENTIFIER().last().text

        val variableType = types.getType(typeName)

        instructions.add(PmInstruction.pushValue(importedValue, ctx.start.line))
        instructions.add(PmInstruction.declareVariable(name, variableType, ctx.start.line))
    }

    override fun exitImportModel(ctx: ProcModelParser.ImportModelContext?) {
        val isRelative = ctx!!.importPath().relativeImportPrefix() != null
        val relativePath = "/" + ctx.importPath().relativeImportPath().text
        val name = ctx.importAlias()?.IDENTIFIER()?.text ?: ctx.importPath().relativeImportPath().IDENTIFIER().last().text
        if (importedModelIDs.containsKey(name)) throw PmCompileError("Duplicate import $name")

        importedModelIDs[name] = importer.importModel(relativePath, isRelative, extraFunctions, extraTypes)
    }

    override fun exitImportTriangles(ctx: ProcModelParser.ImportTrianglesContext?) {
        val isRelative = ctx!!.importPath().relativeImportPrefix() != null
        val relativePath = "/" + ctx.importPath().relativeImportPath().text
        val importedTriangles = importer.importTriangles(relativePath, isRelative)

        val importedValue = when (ctx.IDENTIFIER().text) {
            "triangles" -> importedTriangles.second
            "vertices" -> importedTriangles.first
            else -> throw PmCompileError("Expected to import 'triangles' or 'vertices', but got ${ctx.IDENTIFIER().text}")
        }

        val name = ctx.importAlias()?.IDENTIFIER()?.text ?: ctx.importPath().relativeImportPath().IDENTIFIER().last().text

        // TODO Use a Set instead of a List for the vertices, when Set support is added
        instructions.add(PmInstruction.pushValue(PmList(importedValue.toMutableList()), ctx.start.line))
        instructions.add(PmInstruction.declareVariable(name, PmBuiltinTypes.LIST, ctx.stop.line))
    }

    override fun enterChildModelBlock(ctx: ProcModelParser.ChildModelBlockContext?) {
        if (isInsideChildParametersBlock) throw PmCompileError("Can't nest a child model in the parameters of another child model")
        isInsideChildParametersBlock = true

        val newBlock = mutableListOf<PmInstruction>()
        this.dynamicChildInstructions.add(newBlock)
        this.instructions = newBlock
    }

    override fun exitChildModel(ctx: ProcModelParser.ChildModelContext?) {
        if (isInsideChildParametersBlock) {
            instructions.add(PmInstruction.exitProgram(ctx!!.stop.line))
            this.instructions = baseInstructions
        } else {
            val newBlock = mutableListOf<PmInstruction>()
            this.dynamicChildInstructions.add(newBlock)
            newBlock.add(PmInstruction.pushValue(PmMap(), ctx!!.stop.line))
            newBlock.add(PmInstruction.invokeBuiltinFunction("outputValue", ctx.stop.line))
            newBlock.add(PmInstruction.delete(ctx.stop.line))
            newBlock.add(PmInstruction.exitProgram(ctx.stop.line))
        }

        val modelName = ctx.IDENTIFIER().text
        val modelID = importedModelIDs[modelName] ?: throw PmCompileError("Unknown child model $modelName")

        instructions.add(PmInstruction.pushValue(PmInt(this.dynamicChildInstructions.size - 1), ctx.start.line))
        instructions.add(PmInstruction.createChildModel(modelID, ctx.start.line))

        isInsideChildParametersBlock = false
    }

    override fun exitInnerStatement(ctx: ProcModelParser.InnerStatementContext?) {

        // In statements like `functionCall(x);`, the result is ignored, and should therefore be deleted from the stack
        if (ctx!!.functionInvocation() != null) {
            instructions.add(PmInstruction.delete(ctx.start.line))
        }
    }

    override fun exitVariableDeclaration(ctx: ProcModelParser.VariableDeclarationContext?) {
        val typeName = ctx!!.IDENTIFIER(0).text
        val type = types.getType(typeName)
        // TODO Allow types that are not defined yet
        if (ctx.expression() == null) {
            val createDefaultValue = type.createDefaultValue
                    ?: throw PmCompileError(
                        "Type $typeName doesn't have a default value", ctx.start.line, ctx.start.charPositionInLine
                    )
            instructions.add(PmInstruction.pushValue(createDefaultValue(), ctx.start.line))
        }
        instructions.add(PmInstruction.declareVariable(ctx.IDENTIFIER(1).text, type, ctx.start.line))
    }

    override fun exitVariableReassignmentTarget(ctx: ProcModelParser.VariableReassignmentTargetContext?) {
        val identifiers = ctx!!.IDENTIFIER().toMutableList()
        if (identifiers.size > 1) {
            instructions.add(PmInstruction.pushVariable(identifiers.removeFirst().text, ctx.start.line))
            while (identifiers.size > 1) {
                instructions.add(PmInstruction.pushProperty(identifiers.removeFirst().text, ctx.start.line))
            }
        }
    }

    override fun exitUpdateArrayOrMap(ctx: ProcModelParser.UpdateArrayOrMapContext?) {
        instructions.add(PmInstruction.updateListOrMap(ctx!!.start.line))
    }

    override fun exitVariableReassignment(ctx: ProcModelParser.VariableReassignmentContext?) {
        val identifiers = ctx!!.variableReassignmentTarget().IDENTIFIER().toMutableList()
        if (identifiers.size == 1) {
            instructions.add(PmInstruction.reassignVariable(identifiers.last().text, ctx.start.line))
        } else {
            instructions.add(PmInstruction.setProperty(identifiers.last().text, ctx.start.line))
        }
    }

    override fun enterListDeclaration(ctx: ProcModelParser.ListDeclarationContext?) {
        instructions.add(PmInstruction.pushValue(PmList(mutableListOf()), ctx!!.start.line))
    }

    override fun exitListElement(ctx: ProcModelParser.ListElementContext?) {
        instructions.add(PmInstruction.invokeBuiltinFunction("add", ctx!!.start.line))
    }

    override fun exitExpression(ctx: ProcModelParser.ExpressionContext?) {
        val nextInstruction = ExpressionComputer.compute(ctx!!)
        if (nextInstruction != null) instructions.add(nextInstruction)
    }

    override fun exitForLoopHeader(ctx: ProcModelParser.ForLoopHeaderContext?) {
        loopIndexStack.add(instructions.size)

        instructions.add(PmInstruction.duplicate(ctx!!.start.line))
        instructions.add(PmInstruction.pushScope(ctx.start.line))
        instructions.add(PmInstruction.pushVariable(ctx.forLoopVariable().text, ctx.start.line))

        if (ctx.forLoopComparator2().text == "<=") {
            instructions.add(PmInstruction.smallerThan(ctx.start.line))
        } else {
            instructions.add(PmInstruction.smallerOrEqual(ctx.start.line))
        }
        // Note: the jumpOffset will be fixed later
        instructions.add(PmInstruction.pushValue(PmInt(-1), ctx.start.line))
        instructions.add(PmInstruction.jump(ctx.start.line))
    }

    override fun exitForLoopComparator1(ctx: ProcModelParser.ForLoopComparator1Context?) {
        if (ctx!!.text == "<") {
            instructions.add(PmInstruction.pushValue(PmInt(1), ctx.start.line))
            instructions.add(PmInstruction.add(ctx.start.line))
        } else if (ctx.text != "<=") {
            throw PmCompileError("Unexpected lower bound comparator of for loop: ${ctx.text}")
        }
    }

    override fun exitForLoopVariable(ctx: ProcModelParser.ForLoopVariableContext?) {
        instructions.add(PmInstruction.pushScope(ctx!!.start.line))
        instructions.add(PmInstruction.declareVariable(ctx.text, PmBuiltinTypes.INT, ctx.start.line))
    }

    override fun exitForLoop(ctx: ProcModelParser.ForLoopContext?) {
        instructions.add(PmInstruction.popScope(ctx!!.start.line))
        instructions.add(PmInstruction.pushVariable(ctx.forLoopHeader().forLoopVariable().text, ctx.start.line))
        instructions.add(PmInstruction.pushValue(PmInt(1), ctx.start.line))
        instructions.add(PmInstruction.add(ctx.start.line))
        instructions.add(PmInstruction.reassignVariable(ctx.forLoopHeader().forLoopVariable().text, ctx.start.line))

        instructions.add(PmInstruction.pushValue(PmBoolean(true), ctx.start.line))
        val jumpBackInstructionIndex = instructions.size
        val targetInstructionIndex = loopIndexStack.removeLast()
        instructions.add(PmInstruction.pushValue(
            PmInt(targetInstructionIndex - jumpBackInstructionIndex - 1), ctx.start.line
        ))
        instructions.add(PmInstruction.jump(ctx.start.line))
        instructions.add(PmInstruction.delete(ctx.start.line))
        instructions.add(PmInstruction.delete(ctx.start.line))
        instructions.add(PmInstruction.popScope(ctx.start.line))
        instructions.add(PmInstruction.popScope(ctx.start.line))

        val exitOffsetInstructionIndex = targetInstructionIndex + 4
        instructions[exitOffsetInstructionIndex] = PmInstruction.pushValue(
            PmInt(2 + jumpBackInstructionIndex - exitOffsetInstructionIndex), ctx.start.line
        )
    }

    companion object {

        fun <VertexValue : PmValue> compile(
            sourceCode: String,
            importer: PmImporter<VertexValue>,
            extraFunctions: Map<String, Int>,
            extraTypes: List<PmType>,
            isChild: Boolean = false
        ): PmProgram {
            val compiler = PmCompiler(importer, extraFunctions, extraTypes, isChild)

            val lexer = ProcModelLexer(CharStreams.fromString(sourceCode))
            val parser = ProcModelParser(CommonTokenStream(lexer))
            parser.errorHandler = BailErrorStrategy()

            try {
                val context = parser.start()
                ParseTreeWalker.DEFAULT.walk(compiler, context)
            } catch (cancelled: ParseCancellationException) {
                val cause = cancelled.cause
                if (cause is RecognitionException) {
                    cause.printStackTrace()
                    throw PmCompileError(
                        "unexpected '${cause.offendingToken.text}' at '${cause.ctx.text}'",
                        cause.offendingToken.line, cause.offendingToken.charPositionInLine
                    )
                } else {
                    throw PmCompileError(cancelled.message ?: cancelled.cause?.message ?: "unknown")
                }
            }

            return compiler.program
        }
    }
}
