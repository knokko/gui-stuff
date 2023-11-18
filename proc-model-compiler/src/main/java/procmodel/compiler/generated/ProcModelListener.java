// Generated from java-escape by ANTLR 4.11.1
package procmodel.compiler.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ProcModelParser}.
 */
public interface ProcModelListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(ProcModelParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(ProcModelParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#outerStatement}.
	 * @param ctx the parse tree
	 */
	void enterOuterStatement(ProcModelParser.OuterStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#outerStatement}.
	 * @param ctx the parse tree
	 */
	void exitOuterStatement(ProcModelParser.OuterStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#innerStatement}.
	 * @param ctx the parse tree
	 */
	void enterInnerStatement(ProcModelParser.InnerStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#innerStatement}.
	 * @param ctx the parse tree
	 */
	void exitInnerStatement(ProcModelParser.InnerStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#relativeImportPrefix}.
	 * @param ctx the parse tree
	 */
	void enterRelativeImportPrefix(ProcModelParser.RelativeImportPrefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#relativeImportPrefix}.
	 * @param ctx the parse tree
	 */
	void exitRelativeImportPrefix(ProcModelParser.RelativeImportPrefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#relativeImportPath}.
	 * @param ctx the parse tree
	 */
	void enterRelativeImportPath(ProcModelParser.RelativeImportPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#relativeImportPath}.
	 * @param ctx the parse tree
	 */
	void exitRelativeImportPath(ProcModelParser.RelativeImportPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#importPath}.
	 * @param ctx the parse tree
	 */
	void enterImportPath(ProcModelParser.ImportPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#importPath}.
	 * @param ctx the parse tree
	 */
	void exitImportPath(ProcModelParser.ImportPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#importModel}.
	 * @param ctx the parse tree
	 */
	void enterImportModel(ProcModelParser.ImportModelContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#importModel}.
	 * @param ctx the parse tree
	 */
	void exitImportModel(ProcModelParser.ImportModelContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#importTriangles}.
	 * @param ctx the parse tree
	 */
	void enterImportTriangles(ProcModelParser.ImportTrianglesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#importTriangles}.
	 * @param ctx the parse tree
	 */
	void exitImportTriangles(ProcModelParser.ImportTrianglesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#importAlias}.
	 * @param ctx the parse tree
	 */
	void enterImportAlias(ProcModelParser.ImportAliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#importAlias}.
	 * @param ctx the parse tree
	 */
	void exitImportAlias(ProcModelParser.ImportAliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#importValue}.
	 * @param ctx the parse tree
	 */
	void enterImportValue(ProcModelParser.ImportValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#importValue}.
	 * @param ctx the parse tree
	 */
	void exitImportValue(ProcModelParser.ImportValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#dynamicBlock}.
	 * @param ctx the parse tree
	 */
	void enterDynamicBlock(ProcModelParser.DynamicBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#dynamicBlock}.
	 * @param ctx the parse tree
	 */
	void exitDynamicBlock(ProcModelParser.DynamicBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#childModelBlock}.
	 * @param ctx the parse tree
	 */
	void enterChildModelBlock(ProcModelParser.ChildModelBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#childModelBlock}.
	 * @param ctx the parse tree
	 */
	void exitChildModelBlock(ProcModelParser.ChildModelBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#dynamicDeclarationBlock}.
	 * @param ctx the parse tree
	 */
	void enterDynamicDeclarationBlock(ProcModelParser.DynamicDeclarationBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#dynamicDeclarationBlock}.
	 * @param ctx the parse tree
	 */
	void exitDynamicDeclarationBlock(ProcModelParser.DynamicDeclarationBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#childModel}.
	 * @param ctx the parse tree
	 */
	void enterChildModel(ProcModelParser.ChildModelContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#childModel}.
	 * @param ctx the parse tree
	 */
	void exitChildModel(ProcModelParser.ChildModelContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(ProcModelParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(ProcModelParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#dynamicDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterDynamicDeclaration(ProcModelParser.DynamicDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#dynamicDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitDynamicDeclaration(ProcModelParser.DynamicDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(ProcModelParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(ProcModelParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#variableReassignment}.
	 * @param ctx the parse tree
	 */
	void enterVariableReassignment(ProcModelParser.VariableReassignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#variableReassignment}.
	 * @param ctx the parse tree
	 */
	void exitVariableReassignment(ProcModelParser.VariableReassignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#variableReassignmentTarget}.
	 * @param ctx the parse tree
	 */
	void enterVariableReassignmentTarget(ProcModelParser.VariableReassignmentTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#variableReassignmentTarget}.
	 * @param ctx the parse tree
	 */
	void exitVariableReassignmentTarget(ProcModelParser.VariableReassignmentTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(ProcModelParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(ProcModelParser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void enterFunctionInvocation(ProcModelParser.FunctionInvocationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#functionInvocation}.
	 * @param ctx the parse tree
	 */
	void exitFunctionInvocation(ProcModelParser.FunctionInvocationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#readIndexed}.
	 * @param ctx the parse tree
	 */
	void enterReadIndexed(ProcModelParser.ReadIndexedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#readIndexed}.
	 * @param ctx the parse tree
	 */
	void exitReadIndexed(ProcModelParser.ReadIndexedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#writeIndexed}.
	 * @param ctx the parse tree
	 */
	void enterWriteIndexed(ProcModelParser.WriteIndexedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#writeIndexed}.
	 * @param ctx the parse tree
	 */
	void exitWriteIndexed(ProcModelParser.WriteIndexedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ProcModelParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ProcModelParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#variableProperty}.
	 * @param ctx the parse tree
	 */
	void enterVariableProperty(ProcModelParser.VariablePropertyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#variableProperty}.
	 * @param ctx the parse tree
	 */
	void exitVariableProperty(ProcModelParser.VariablePropertyContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#positionConstructor}.
	 * @param ctx the parse tree
	 */
	void enterPositionConstructor(ProcModelParser.PositionConstructorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#positionConstructor}.
	 * @param ctx the parse tree
	 */
	void exitPositionConstructor(ProcModelParser.PositionConstructorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#listElement}.
	 * @param ctx the parse tree
	 */
	void enterListElement(ProcModelParser.ListElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#listElement}.
	 * @param ctx the parse tree
	 */
	void exitListElement(ProcModelParser.ListElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#listDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterListDeclaration(ProcModelParser.ListDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#listDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitListDeclaration(ProcModelParser.ListDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#setDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterSetDeclaration(ProcModelParser.SetDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#setDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitSetDeclaration(ProcModelParser.SetDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoop}.
	 * @param ctx the parse tree
	 */
	void enterForLoop(ProcModelParser.ForLoopContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoop}.
	 * @param ctx the parse tree
	 */
	void exitForLoop(ProcModelParser.ForLoopContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoopHeader}.
	 * @param ctx the parse tree
	 */
	void enterForLoopHeader(ProcModelParser.ForLoopHeaderContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoopHeader}.
	 * @param ctx the parse tree
	 */
	void exitForLoopHeader(ProcModelParser.ForLoopHeaderContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoopVariable}.
	 * @param ctx the parse tree
	 */
	void enterForLoopVariable(ProcModelParser.ForLoopVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoopVariable}.
	 * @param ctx the parse tree
	 */
	void exitForLoopVariable(ProcModelParser.ForLoopVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoopComparator1}.
	 * @param ctx the parse tree
	 */
	void enterForLoopComparator1(ProcModelParser.ForLoopComparator1Context ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoopComparator1}.
	 * @param ctx the parse tree
	 */
	void exitForLoopComparator1(ProcModelParser.ForLoopComparator1Context ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoopComparator2}.
	 * @param ctx the parse tree
	 */
	void enterForLoopComparator2(ProcModelParser.ForLoopComparator2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoopComparator2}.
	 * @param ctx the parse tree
	 */
	void exitForLoopComparator2(ProcModelParser.ForLoopComparator2Context ctx);
	/**
	 * Enter a parse tree produced by {@link ProcModelParser#forLoopComparator}.
	 * @param ctx the parse tree
	 */
	void enterForLoopComparator(ProcModelParser.ForLoopComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ProcModelParser#forLoopComparator}.
	 * @param ctx the parse tree
	 */
	void exitForLoopComparator(ProcModelParser.ForLoopComparatorContext ctx);
}