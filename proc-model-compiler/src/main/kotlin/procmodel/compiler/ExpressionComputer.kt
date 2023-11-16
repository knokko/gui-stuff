package procmodel.compiler

import procmodel.compiler.generated.ProcModelParser
import procmodel.lang.instructions.PmInstruction
import procmodel.lang.types.PmFloat
import procmodel.lang.types.PmInt
import procmodel.lang.types.PmString
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt

internal object ExpressionComputer {
    fun compute(ctx: ProcModelParser.ExpressionContext): PmInstruction? {
        if (ctx.FLOAT_LITERAL() != null) return PmInstruction.pushValue(
            PmFloat(parseFloat(ctx.FLOAT_LITERAL().text)), ctx.start.line
        )
        if (ctx.INT_LITERAL() != null) return PmInstruction.pushValue(
            PmInt(parseInt(ctx.INT_LITERAL().text)), ctx.start.line
        )
        if (ctx.STRING_LITERAL() != null) {
            val literal = ctx.STRING_LITERAL().text
            return PmInstruction.pushValue(
                PmString(literal.substring(1 until literal.length - 1)), ctx.start.line
            )
        }
        if (ctx.variableProperty() != null) return PmInstruction.pushProperty(
            ctx.variableProperty().IDENTIFIER().text, ctx.start.line
        )
        if (ctx.readIndexed() != null) return PmInstruction.readIndexed(ctx.start.line)

        if (ctx.DIVIDE() != null) return PmInstruction.divide(ctx.start.line)
        if (ctx.TIMES() != null) return PmInstruction.multiply(ctx.start.line)
        if (ctx.PLUS() != null) return PmInstruction.add(ctx.start.line)
        if (ctx.MINUS() != null) return PmInstruction.subtract(ctx.start.line)

        if (ctx.IDENTIFIER() != null) return PmInstruction.pushVariable(ctx.IDENTIFIER().text, ctx.start.line)

        // The position constructor is just syntactic sugar
        if (ctx.positionConstructor() != null) return PmInstruction.invokeBuiltinFunction(
            "constructPosition", ctx.start.line
        )

        return null
    }
}
