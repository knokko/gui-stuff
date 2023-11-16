// Generated from java-escape by ANTLR 4.11.1
package procmodel.compiler.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ProcModelParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ProcModelVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#start}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStart(ProcModelParser.StartContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#outerStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuterStatement(ProcModelParser.OuterStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#innerStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerStatement(ProcModelParser.InnerStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#relativeImportPrefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelativeImportPrefix(ProcModelParser.RelativeImportPrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#relativeImportPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelativeImportPath(ProcModelParser.RelativeImportPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#importPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportPath(ProcModelParser.ImportPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#importModel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportModel(ProcModelParser.ImportModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#importTriangles}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportTriangles(ProcModelParser.ImportTrianglesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#importAlias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportAlias(ProcModelParser.ImportAliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#importValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImportValue(ProcModelParser.ImportValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#dynamicBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDynamicBlock(ProcModelParser.DynamicBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#childModelBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChildModelBlock(ProcModelParser.ChildModelBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#dynamicDeclarationBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDynamicDeclarationBlock(ProcModelParser.DynamicDeclarationBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#childModel}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChildModel(ProcModelParser.ChildModelContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclaration(ProcModelParser.ParameterDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#dynamicDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDynamicDeclaration(ProcModelParser.DynamicDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(ProcModelParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#variableReassignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableReassignment(ProcModelParser.VariableReassignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#variableReassignmentTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableReassignmentTarget(ProcModelParser.VariableReassignmentTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(ProcModelParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#functionInvocation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionInvocation(ProcModelParser.FunctionInvocationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#readIndexed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReadIndexed(ProcModelParser.ReadIndexedContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#writeIndexed}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWriteIndexed(ProcModelParser.WriteIndexedContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ProcModelParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#variableProperty}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableProperty(ProcModelParser.VariablePropertyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#positionConstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionConstructor(ProcModelParser.PositionConstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#listElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListElement(ProcModelParser.ListElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#listDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListDeclaration(ProcModelParser.ListDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoop(ProcModelParser.ForLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoopHeader}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoopHeader(ProcModelParser.ForLoopHeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoopVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoopVariable(ProcModelParser.ForLoopVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoopComparator1}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoopComparator1(ProcModelParser.ForLoopComparator1Context ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoopComparator2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoopComparator2(ProcModelParser.ForLoopComparator2Context ctx);
	/**
	 * Visit a parse tree produced by {@link ProcModelParser#forLoopComparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoopComparator(ProcModelParser.ForLoopComparatorContext ctx);
}