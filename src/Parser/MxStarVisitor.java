package Parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MxStarParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface MxStarVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link MxStarParser#program}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProgram(MxStarParser.ProgramContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#programDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProgramDefine(MxStarParser.ProgramDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#classDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitClassDefine(MxStarParser.ClassDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#variableDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVariableDefine(MxStarParser.VariableDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#singleVariableDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSingleVariableDefine(MxStarParser.SingleVariableDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#methodDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMethodDefine(MxStarParser.MethodDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#constructorDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitConstructorDefine(MxStarParser.ConstructorDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#functionDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFunctionDefine(MxStarParser.FunctionDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#parameterList}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParameterList(MxStarParser.ParameterListContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#parameterDefine}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParameterDefine(MxStarParser.ParameterDefineContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#returnType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReturnType(MxStarParser.ReturnTypeContext ctx);

    /**
     * Visit a parse tree produced by the {@code arrayType}
     * labeled alternative in {@link MxStarParser#type}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitArrayType(MxStarParser.ArrayTypeContext ctx);

    /**
     * Visit a parse tree produced by the {@code nArrayType}
     * labeled alternative in {@link MxStarParser#type}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNArrayType(MxStarParser.NArrayTypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#nonArrayType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNonArrayType(MxStarParser.NonArrayTypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#builtinType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBuiltinType(MxStarParser.BuiltinTypeContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#suite}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSuite(MxStarParser.SuiteContext ctx);

    /**
     * Visit a parse tree produced by the {@code blockStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBlockStatement(MxStarParser.BlockStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code variableDefineStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVariableDefineStatement(MxStarParser.VariableDefineStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code ifStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIfStatement(MxStarParser.IfStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code forStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForStatement(MxStarParser.ForStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code whileStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitWhileStatement(MxStarParser.WhileStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code returnStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReturnStatement(MxStarParser.ReturnStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code breakStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBreakStatement(MxStarParser.BreakStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code continueStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitContinueStatement(MxStarParser.ContinueStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code expressionStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExpressionStatement(MxStarParser.ExpressionStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code emptyStatement}
     * labeled alternative in {@link MxStarParser#statement}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitEmptyStatement(MxStarParser.EmptyStatementContext ctx);

    /**
     * Visit a parse tree produced by the {@code binaryExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBinaryExpression(MxStarParser.BinaryExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code lambdaExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLambdaExpression(MxStarParser.LambdaExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code addressingExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAddressingExpression(MxStarParser.AddressingExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code atomExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAtomExpression(MxStarParser.AtomExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code postCrementExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPostCrementExpression(MxStarParser.PostCrementExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code assignExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAssignExpression(MxStarParser.AssignExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code memberAccessExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMemberAccessExpression(MxStarParser.MemberAccessExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code functionCallExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFunctionCallExpression(MxStarParser.FunctionCallExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code unaryExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUnaryExpression(MxStarParser.UnaryExpressionContext ctx);

    /**
     * Visit a parse tree produced by the {@code newTypeExpression}
     * labeled alternative in {@link MxStarParser#expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNewTypeExpression(MxStarParser.NewTypeExpressionContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#primary}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPrimary(MxStarParser.PrimaryContext ctx);

    /**
     * Visit a parse tree produced by the {@code errorNewArray}
     * labeled alternative in {@link MxStarParser#newType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitErrorNewArray(MxStarParser.ErrorNewArrayContext ctx);

    /**
     * Visit a parse tree produced by the {@code newArray}
     * labeled alternative in {@link MxStarParser#newType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNewArray(MxStarParser.NewArrayContext ctx);

    /**
     * Visit a parse tree produced by the {@code newObjectConstruct}
     * labeled alternative in {@link MxStarParser#newType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNewObjectConstruct(MxStarParser.NewObjectConstructContext ctx);

    /**
     * Visit a parse tree produced by the {@code newObject}
     * labeled alternative in {@link MxStarParser#newType}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNewObject(MxStarParser.NewObjectContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#argumentList}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitArgumentList(MxStarParser.ArgumentListContext ctx);

    /**
     * Visit a parse tree produced by {@link MxStarParser#literal}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLiteral(MxStarParser.LiteralContext ctx);
}