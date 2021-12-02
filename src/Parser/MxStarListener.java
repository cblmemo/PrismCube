// Generated from C:/RainyMemory's Workplace/IdeaProjects/PrismCube/src/Parser\MxStar.g4 by ANTLR 4.9.1
package Parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MxStarParser}.
 */
public interface MxStarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MxStarParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MxStarParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MxStarParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#programDefine}.
	 * @param ctx the parse tree
	 */
	void enterProgramDefine(MxStarParser.ProgramDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#programDefine}.
	 * @param ctx the parse tree
	 */
	void exitProgramDefine(MxStarParser.ProgramDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#classDefine}.
	 * @param ctx the parse tree
	 */
	void enterClassDefine(MxStarParser.ClassDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#classDefine}.
	 * @param ctx the parse tree
	 */
	void exitClassDefine(MxStarParser.ClassDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#variableDefine}.
	 * @param ctx the parse tree
	 */
	void enterVariableDefine(MxStarParser.VariableDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#variableDefine}.
	 * @param ctx the parse tree
	 */
	void exitVariableDefine(MxStarParser.VariableDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#singleVariableDefine}.
	 * @param ctx the parse tree
	 */
	void enterSingleVariableDefine(MxStarParser.SingleVariableDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#singleVariableDefine}.
	 * @param ctx the parse tree
	 */
	void exitSingleVariableDefine(MxStarParser.SingleVariableDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#methodDefine}.
	 * @param ctx the parse tree
	 */
	void enterMethodDefine(MxStarParser.MethodDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#methodDefine}.
	 * @param ctx the parse tree
	 */
	void exitMethodDefine(MxStarParser.MethodDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#constructorDefine}.
	 * @param ctx the parse tree
	 */
	void enterConstructorDefine(MxStarParser.ConstructorDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#constructorDefine}.
	 * @param ctx the parse tree
	 */
	void exitConstructorDefine(MxStarParser.ConstructorDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#functionDefine}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefine(MxStarParser.FunctionDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#functionDefine}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefine(MxStarParser.FunctionDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(MxStarParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(MxStarParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#parameterDefine}.
	 * @param ctx the parse tree
	 */
	void enterParameterDefine(MxStarParser.ParameterDefineContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#parameterDefine}.
	 * @param ctx the parse tree
	 */
	void exitParameterDefine(MxStarParser.ParameterDefineContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#returnType}.
	 * @param ctx the parse tree
	 */
	void enterReturnType(MxStarParser.ReturnTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#returnType}.
	 * @param ctx the parse tree
	 */
	void exitReturnType(MxStarParser.ReturnTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayType}
	 * labeled alternative in {@link MxStarParser#type}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(MxStarParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayType}
	 * labeled alternative in {@link MxStarParser#type}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(MxStarParser.ArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nArrayType}
	 * labeled alternative in {@link MxStarParser#type}.
	 * @param ctx the parse tree
	 */
	void enterNArrayType(MxStarParser.NArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nArrayType}
	 * labeled alternative in {@link MxStarParser#type}.
	 * @param ctx the parse tree
	 */
	void exitNArrayType(MxStarParser.NArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void enterNonArrayType(MxStarParser.NonArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void exitNonArrayType(MxStarParser.NonArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#builtinType}.
	 * @param ctx the parse tree
	 */
	void enterBuiltinType(MxStarParser.BuiltinTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#builtinType}.
	 * @param ctx the parse tree
	 */
	void exitBuiltinType(MxStarParser.BuiltinTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#suite}.
	 * @param ctx the parse tree
	 */
	void enterSuite(MxStarParser.SuiteContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#suite}.
	 * @param ctx the parse tree
	 */
	void exitSuite(MxStarParser.SuiteContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#forInitializeStatement}.
	 * @param ctx the parse tree
	 */
	void enterForInitializeStatement(MxStarParser.ForInitializeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#forInitializeStatement}.
	 * @param ctx the parse tree
	 */
	void exitForInitializeStatement(MxStarParser.ForInitializeStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code blockStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(MxStarParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code blockStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(MxStarParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableDefineStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterVariableDefineStatement(MxStarParser.VariableDefineStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableDefineStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitVariableDefineStatement(MxStarParser.VariableDefineStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ifStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(MxStarParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ifStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(MxStarParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(MxStarParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(MxStarParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code whileStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(MxStarParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code whileStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(MxStarParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code returnStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(MxStarParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code returnStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(MxStarParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code breakStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStatement(MxStarParser.BreakStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code breakStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStatement(MxStarParser.BreakStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code continueStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStatement(MxStarParser.ContinueStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code continueStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStatement(MxStarParser.ContinueStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expressionStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(MxStarParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expressionStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(MxStarParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code emptyStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(MxStarParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code emptyStatement}
	 * labeled alternative in {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(MxStarParser.EmptyStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpression(MxStarParser.BinaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpression(MxStarParser.BinaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code lambdaExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaExpression(MxStarParser.LambdaExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lambdaExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaExpression(MxStarParser.LambdaExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addressingExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAddressingExpression(MxStarParser.AddressingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addressingExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAddressingExpression(MxStarParser.AddressingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpression(MxStarParser.AtomExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpression(MxStarParser.AtomExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code postCrementExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPostCrementExpression(MxStarParser.PostCrementExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code postCrementExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPostCrementExpression(MxStarParser.PostCrementExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpression(MxStarParser.AssignExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpression(MxStarParser.AssignExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code memberAccessExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMemberAccessExpression(MxStarParser.MemberAccessExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code memberAccessExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMemberAccessExpression(MxStarParser.MemberAccessExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpression(MxStarParser.FunctionCallExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpression(MxStarParser.FunctionCallExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(MxStarParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(MxStarParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newTypeExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNewTypeExpression(MxStarParser.NewTypeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newTypeExpression}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNewTypeExpression(MxStarParser.NewTypeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(MxStarParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(MxStarParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorNewArray}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void enterErrorNewArray(MxStarParser.ErrorNewArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorNewArray}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void exitErrorNewArray(MxStarParser.ErrorNewArrayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newArray}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void enterNewArray(MxStarParser.NewArrayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newArray}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void exitNewArray(MxStarParser.NewArrayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newObjectConstruct}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void enterNewObjectConstruct(MxStarParser.NewObjectConstructContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newObjectConstruct}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void exitNewObjectConstruct(MxStarParser.NewObjectConstructContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newObject}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void enterNewObject(MxStarParser.NewObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newObject}
	 * labeled alternative in {@link MxStarParser#newType}.
	 * @param ctx the parse tree
	 */
	void exitNewObject(MxStarParser.NewObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(MxStarParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(MxStarParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(MxStarParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(MxStarParser.LiteralContext ctx);
}