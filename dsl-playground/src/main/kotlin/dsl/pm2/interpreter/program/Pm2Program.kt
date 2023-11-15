package dsl.pm2.interpreter.program

import dsl.pm2.ProcModel2Lexer
import dsl.pm2.ProcModel2Parser
import dsl.pm2.interpreter.*
import dsl.pm2.interpreter.importer.Pm2Importer
import dsl.pm2.interpreter.instruction.Pm2Instruction
import dsl.pm2.interpreter.value.Pm2Value
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.RuntimeException
import kotlin.jvm.Throws

class Pm2Program(
    val instructions: List<Pm2Instruction>,
    /**
     * A list of instructions blocks: the instruction block with index N contains the instructions to construct dynamic
     * matrix N
     */
    val dynamicBlocks: List<List<Pm2Instruction>>,
    /**
     * A list of instructions blocks: the instruction block with index N contains the instructions to convert
     * the dynamic parameters of this (parent) model to the dynamic parameters of child model N
     */
    val dynamicChildParameterBlocks: List<List<Pm2Instruction>>,
    val staticParameters: Map<String, Pm2Type>,
    val dynamicParameters: Map<String, Pm2Type>
) {

    lateinit var childProgramTable: Map<String, Triple<Int, Map<String, Pm2Type>, Map<String, Pm2Type>>>

    @Throws(Pm2RuntimeError::class)
    fun run(rawStaticParameters: Pm2Value): Pm2Model {
        return Pm2VertexProcessor(this, rawStaticParameters).execute()
    }

    @Throws(Pm2RuntimeError::class)
    fun getResultValue(): Pm2Value {
        if (dynamicParameters.isNotEmpty()) throw Pm2RuntimeError("Dynamic parameters are only allowed on models")
        val processor = Pm2ValueProcessor(instructions)
        processor.execute()
        return processor.result ?: throw Pm2RuntimeError("No result was produced")
    }

    companion object {
        fun compile(sourceCode: String, importer: Pm2Importer, isChild: Boolean = false): Pm2Program {
            val converter = Pm2Converter(importer, isChild)

            val lexer = ProcModel2Lexer(CharStreams.fromString(sourceCode))
            val parser = ProcModel2Parser(CommonTokenStream(lexer))
            parser.errorHandler = BailErrorStrategy()

            try {
                val context = parser.start()
                ParseTreeWalker.DEFAULT.walk(converter, context)
            } catch (cancelled: ParseCancellationException) {
                val cause = cancelled.cause
                if (cause is RecognitionException) {
                    cause.printStackTrace()
                    throw Pm2CompileError(
                        "unexpected '${cause.offendingToken.text}' at '${cause.ctx.text}'",
                        cause.offendingToken.line, cause.offendingToken.charPositionInLine
                    )
                } else {
                    throw Pm2CompileError(cancelled.message ?: cancelled.cause?.message ?: "unknown")
                }
            }

            return converter.program
        }
    }
}
