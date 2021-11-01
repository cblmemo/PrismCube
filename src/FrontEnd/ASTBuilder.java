package FrontEnd;

import AST.*;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Parser.MxStarBaseVisitor;
import Parser.MxStarParser;
import Utility.Cursor;
import Utility.Memory;

import java.util.Stack;

import static Debug.MemoLog.log;

public class ASTBuilder extends MxStarBaseVisitor<ASTNode> {
    enum identifierType {
        function, variable
    }

    private Stack<identifierType> stack = new Stack<>();

    public void build(Memory memory) {
        log.Infof("Build started.\n");

        ProgramNode ASTRoot = (ProgramNode) visit(memory.getParseTreeRoot());
        memory.setASTRoot(ASTRoot);

        log.Infof("Build finished.\n");
    }

    // Program

    @Override
    public ASTNode visitProgram(MxStarParser.ProgramContext ctx) {
        ProgramNode root = new ProgramNode(new Cursor(ctx));
        ctx.programDefine().forEach(innerCtx -> {
            ProgramDefineNode define = (ProgramDefineNode) visit(innerCtx);
            root.addDefine(define);
        });
        return root;
    }

    // Defines

    @Override
    public ASTNode visitProgramDefine(MxStarParser.ProgramDefineContext ctx) {
        if (ctx.classDefine() != null) return visit(ctx.classDefine());
        if (ctx.variableDefine() != null) return visit(ctx.variableDefine());
        return visit(ctx.functionDefine());
    }

    @Override
    public ASTNode visitClassDefine(MxStarParser.ClassDefineContext ctx) {
        IdentifierPrimaryNode className = new IdentifierPrimaryNode(ctx.Identifier().getText(), false, false, new Cursor(ctx));
        ClassDefineNode ret = new ClassDefineNode(className, new Cursor(ctx));
        if (ctx.variableDefine() != null) {
            ctx.variableDefine().forEach(innerCtx -> {
                VariableDefineNode node = (VariableDefineNode) visit(innerCtx);
                ret.addVariable(node);
            });
        }
        if (ctx.methodDefine() != null) {
            ctx.methodDefine().forEach(innerCtx -> {
                ProgramDefineNode node = (ProgramDefineNode) visit(innerCtx);
                if (node instanceof ConstructorDefineNode) ret.setConstructor((ConstructorDefineNode) node);
                else ret.addFunction((FunctionDefineNode) node);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitVariableDefine(MxStarParser.VariableDefineContext ctx) {
        TypeNode type = (TypeNode) visit(ctx.type());
        VariableDefineNode ret = new VariableDefineNode(type, new Cursor(ctx));
        ctx.singleVariableDefine().forEach(innerCtx -> {
            SingleVariableDefineNode singleDefine = (SingleVariableDefineNode) visit(innerCtx);
            singleDefine.setType(type);
            ret.addStatement(singleDefine);
        });
        return ret;
    }

    @Override
    public ASTNode visitSingleVariableDefine(MxStarParser.SingleVariableDefineContext ctx) {
        IdentifierPrimaryNode variableName = new IdentifierPrimaryNode(ctx.Identifier().getText(), true, false, new Cursor(ctx));
        if (ctx.expression() == null) return new SingleVariableDefineNode(variableName, null, new Cursor(ctx));
        ExpressionNode initializeValue = (ExpressionNode) visit(ctx.expression());
        return new SingleVariableDefineNode(variableName, initializeValue, new Cursor(ctx));
    }

    @Override
    public ASTNode visitMethodDefine(MxStarParser.MethodDefineContext ctx) {
        if (ctx.functionDefine() != null) return visit(ctx.functionDefine());
        return visit(ctx.constructorDefine());
    }

    @Override
    public ASTNode visitConstructorDefine(MxStarParser.ConstructorDefineContext ctx) {
        IdentifierPrimaryNode constructorName = new IdentifierPrimaryNode(ctx.Identifier().getText(), false, true, new Cursor(ctx));
        ConstructorDefineNode ret = new ConstructorDefineNode(constructorName, new Cursor(ctx));
        if (ctx.suite().statement() != null) {
            ctx.suite().statement().forEach(innerCtx -> {
                StatementNode statement = (StatementNode) visit(innerCtx);
                ret.addStatement(statement);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitFunctionDefine(MxStarParser.FunctionDefineContext ctx) {
        TypeNode returnType = (TypeNode) visit(ctx.returnType());
        IdentifierPrimaryNode functionName = new IdentifierPrimaryNode(ctx.Identifier().getText(), false, true, new Cursor(ctx));
        FunctionDefineNode ret = new FunctionDefineNode(functionName, returnType, new Cursor(ctx));
        if (ctx.parameterList() != null) {
            ctx.parameterList().parameterDefine().forEach(innerCtx -> {
                ParameterDefineNode parameter = (ParameterDefineNode) visit(innerCtx);
                ret.addParameter(parameter);
            });
        }
        if (ctx.suite().statement() != null) {
            ctx.suite().statement().forEach(innerCtx -> {
                StatementNode statement = (StatementNode) visit(innerCtx);
                ret.addStatement(statement);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitParameterDefine(MxStarParser.ParameterDefineContext ctx) {
        TypeNode parameterType = (TypeNode) visit(ctx.type());
        IdentifierPrimaryNode parameterName = new IdentifierPrimaryNode(ctx.Identifier().getText(), true, false, new Cursor(ctx));
        return new ParameterDefineNode(parameterType, parameterName, new Cursor(ctx));
    }

    // Types

    @Override
    public ASTNode visitReturnType(MxStarParser.ReturnTypeContext ctx) {
        if (ctx.Void() != null) return new ReturnTypeNode("void", new Cursor(ctx));
        return visit(ctx.type());
    }

    @Override
    public ASTNode visitNArrayType(MxStarParser.NArrayTypeContext ctx) {
        return visit(ctx.nonArrayType());
    }

    @Override
    public ASTNode visitArrayType(MxStarParser.ArrayTypeContext ctx) {
        TypeNode elementType = (TypeNode) visit(ctx.type());
        return new ArrayTypeNode(elementType.getTypeName() + "[]", elementType, new Cursor(ctx));
    }

    @Override
    public ASTNode visitNonArrayType(MxStarParser.NonArrayTypeContext ctx) {
        if (ctx.builtinType() != null) return visit(ctx.builtinType());
        return new ClassTypeNode(new IdentifierPrimaryNode(ctx.Identifier().getText(), false, false, new Cursor(ctx)), new Cursor(ctx));
    }

    @Override
    public ASTNode visitBuiltinType(MxStarParser.BuiltinTypeContext ctx) {
        if (ctx.Int() != null) return new BuiltinTypeNode("int", new Cursor(ctx));
        if (ctx.Bool() != null) return new BuiltinTypeNode("bool", new Cursor(ctx));
        return new BuiltinTypeNode("string", new Cursor(ctx));
    }

    // Statements

    @Override
    public ASTNode visitBlockStatement(MxStarParser.BlockStatementContext ctx) {
        BlockStatementNode ret = new BlockStatementNode(new Cursor(ctx));
        if (ctx.suite().statement() != null) {
            ctx.suite().statement().forEach(innerCtx -> {
                StatementNode node = (StatementNode) visit(innerCtx);
                ret.addStatement(node);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitVariableDefineStatement(MxStarParser.VariableDefineStatementContext ctx) {
        return visit(ctx.variableDefine());
    }

    @Override
    public ASTNode visitIfStatement(MxStarParser.IfStatementContext ctx) {
        ExpressionNode conditionExpression = (ExpressionNode) visit(ctx.expression());
        IfStatementNode ret = new IfStatementNode(conditionExpression, new Cursor(ctx));
        ret.setTrueStatement((StatementNode) visit(ctx.trueStatement));
        if (ctx.falseStatement != null) ret.setFalseStatement((StatementNode) visit(ctx.falseStatement));
        return ret;
    }

    @Override
    public ASTNode visitForStatement(MxStarParser.ForStatementContext ctx) {
        StatementNode loopBody = (StatementNode) visit(ctx.statement());
        ForStatementNode ret = new ForStatementNode(loopBody, new Cursor(ctx));
        if (ctx.initializeExpression != null) ret.setInitializeExpression((ExpressionNode) visit(ctx.initializeExpression));
        if (ctx.conditionExpression != null) ret.setConditionExpression((ExpressionNode) visit(ctx.conditionExpression));
        if (ctx.stepExpression != null) ret.setStepExpression((ExpressionNode) visit(ctx.stepExpression));
        return ret;
    }

    @Override
    public ASTNode visitWhileStatement(MxStarParser.WhileStatementContext ctx) {
        ExpressionNode conditionExpression = (ExpressionNode) visit(ctx.conditionExpression);
        StatementNode loopBody = (StatementNode) visit(ctx.statement());
        return new WhileStatementNode(conditionExpression, loopBody, new Cursor(ctx));
    }

    @Override
    public ASTNode visitReturnStatement(MxStarParser.ReturnStatementContext ctx) {
        ReturnStatementNode ret = new ReturnStatementNode(new Cursor(ctx));
        if (ctx.expression() != null) ret.setReturnValue((ExpressionNode) visit(ctx.expression()));
        return ret;
    }

    @Override
    public ASTNode visitBreakStatement(MxStarParser.BreakStatementContext ctx) {
        return new BreakStatementNode(new Cursor(ctx));
    }

    @Override
    public ASTNode visitContinueStatement(MxStarParser.ContinueStatementContext ctx) {
        return new ContinueStatementNode(new Cursor(ctx));
    }

    @Override
    public ASTNode visitExpressionStatement(MxStarParser.ExpressionStatementContext ctx) {
        return new ExpressionStatementNode((ExpressionNode) visit(ctx.expression()), new Cursor(ctx));
    }

    @Override
    public ASTNode visitEmptyStatement(MxStarParser.EmptyStatementContext ctx) {
        return new EmptyStatementNode(new Cursor(ctx));
    }

    // Expressions

    @Override
    public ASTNode visitAtomExpression(MxStarParser.AtomExpressionContext ctx) {
        return visit(ctx.primary());
    }

    @Override
    public ASTNode visitNewTypeExpression(MxStarParser.NewTypeExpressionContext ctx) {
        return visit(ctx.newType());
    }

    @Override
    public ASTNode visitMemberAccessExpression(MxStarParser.MemberAccessExpressionContext ctx) {
        stack.push(identifierType.variable);
        ExpressionNode instance = (ExpressionNode) visit(ctx.expression());
        stack.pop();
        IdentifierPrimaryNode memberName;
        if (!stack.empty() && stack.peek() == identifierType.function) memberName = new IdentifierPrimaryNode(ctx.Identifier().getText(), false, true, new Cursor(ctx));
        else memberName = new IdentifierPrimaryNode(ctx.Identifier().getText(), true, false, new Cursor(ctx));
        return new MemberAccessExpressionNode(instance, memberName, new Cursor(ctx));
    }

    @Override
    public ASTNode visitLambdaExpression(MxStarParser.LambdaExpressionContext ctx) {
        LambdaExpressionNode ret = new LambdaExpressionNode(new Cursor(ctx));
        if (ctx.parameterList() != null) {
            ret.createParameterList();
            ctx.parameterList().parameterDefine().forEach(innerCtx -> {
                ParameterDefineNode parameter = (ParameterDefineNode) visit(innerCtx);
                ret.addParameter(parameter);
            });
        }
        if (ctx.suite().statement() != null) {
            ctx.suite().statement().forEach(innerCtx -> {
                StatementNode statement = (StatementNode) visit(innerCtx);
                ret.addStatement(statement);
            });
        }
        if (ctx.argumentList() != null) {
            ctx.argumentList().expression().forEach(innerCtx -> {
                ExpressionNode expression = (ExpressionNode) visit(innerCtx);
                ret.addArgument(expression);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitFunctionCallExpression(MxStarParser.FunctionCallExpressionContext ctx) {
        stack.push(identifierType.function);
        ExpressionNode function = (ExpressionNode) visit(ctx.expression());
        stack.pop();
        FunctionCallExpressionNode ret = new FunctionCallExpressionNode(function, new Cursor(ctx));
        if (function instanceof MemberAccessExpressionNode) {
            ((MemberAccessExpressionNode) function).setAccessMethod(true);
            ret.setInstance(((MemberAccessExpressionNode) function).getInstance());
            ret.setFunctionName(((MemberAccessExpressionNode) function).getMemberName());
        } else if (function instanceof IdentifierPrimaryNode) {
            ret.setFunctionName(((IdentifierPrimaryNode) function).getIdentifier());
        } else ret.setInvalid(true);
        if (ctx.argumentList() != null) {
            ctx.argumentList().expression().forEach(innerCtx -> {
                ExpressionNode argument = (ExpressionNode) visit(innerCtx);
                ret.addArgument(argument);
            });
        }
        return ret;
    }

    @Override
    public ASTNode visitAddressingExpression(MxStarParser.AddressingExpressionContext ctx) {
        ExpressionNode array = (ExpressionNode) visit(ctx.array);
        ExpressionNode index = (ExpressionNode) visit(ctx.index);
        return new AddressingExpressionNode(array, index, new Cursor(ctx));
    }

    @Override
    public ASTNode visitPostCrementExpression(MxStarParser.PostCrementExpressionContext ctx) {
        ExpressionNode lhs = (ExpressionNode) visit(ctx.expression());
        return new PostCrementExpressionNode(lhs, ctx.op.getText(), new Cursor(ctx));
    }

    @Override
    public ASTNode visitUnaryExpression(MxStarParser.UnaryExpressionContext ctx) {
        ExpressionNode rhs = (ExpressionNode) visit(ctx.expression());
        return new UnaryExpressionNode(rhs, ctx.op.getText(), new Cursor(ctx));
    }

    @Override
    public ASTNode visitBinaryExpression(MxStarParser.BinaryExpressionContext ctx) {
        ExpressionNode lhs = (ExpressionNode) visit(ctx.leftExpression);
        ExpressionNode rhs = (ExpressionNode) visit(ctx.rightExpression);
        return new BinaryExpressionNode(lhs, rhs, ctx.op.getText(), new Cursor(ctx));
    }

    @Override
    public ASTNode visitAssignExpression(MxStarParser.AssignExpressionContext ctx) {
        ExpressionNode lhs = (ExpressionNode) visit(ctx.leftExpression);
        ExpressionNode rhs = (ExpressionNode) visit(ctx.rightExpression);
        return new AssignExpressionNode(lhs, rhs, ctx.op.getText(), new Cursor(ctx));
    }

    // Primaries

    @Override
    public ASTNode visitPrimary(MxStarParser.PrimaryContext ctx) {
        if (ctx.expression() != null) return visit(ctx.expression());
        if (ctx.This() != null) return new ThisPrimaryNode(new Cursor(ctx));
        if (ctx.literal() != null) return visit(ctx.literal());
        if (!stack.empty() && stack.peek() == identifierType.function) return new IdentifierPrimaryNode(ctx.Identifier().getText(), false, true, new Cursor(ctx));
        else return new IdentifierPrimaryNode(ctx.Identifier().getText(), true, false, new Cursor(ctx));
    }

    @Override
    public ASTNode visitLiteral(MxStarParser.LiteralContext ctx) {
        if (ctx.NumericalConstant() != null) return new NumericalConstantPrimaryNode(ctx.NumericalConstant().getText(), new Cursor(ctx));
        if (ctx.BoolConstant() != null) return new BoolConstantPrimaryNode(ctx.BoolConstant().getText(), new Cursor(ctx));
        if (ctx.StringConstant() != null) return new StringConstantPrimaryNode(ctx.StringConstant().getText(), new Cursor(ctx));
        return new NullConstantPrimaryNode(new Cursor(ctx));
    }

    // News

    @Override
    public ASTNode visitErrorNewArray(MxStarParser.ErrorNewArrayContext ctx) {
        return new NewTypeExpressionNode(null, true, new Cursor(ctx));
    }

    @Override
    public ASTNode visitNewArray(MxStarParser.NewArrayContext ctx) {
        TypeNode nonArrayType = (TypeNode) visit(ctx.nonArrayType());
        NewTypeExpressionNode ret = new NewTypeExpressionNode(nonArrayType, false, new Cursor(ctx));
        for (var c : ctx.getText().toCharArray()) {
            if (c == '[') {
                ret.increaseDimension();
            }
        }
        ctx.expression().forEach(innerCtx -> {
            ret.addDimensionExpression((ExpressionNode) visit(innerCtx));
        });
        return ret;
    }

    @Override
    public ASTNode visitNewObject(MxStarParser.NewObjectContext ctx) {
        TypeNode nonArrayType = (TypeNode) visit(ctx.nonArrayType());
        return new NewTypeExpressionNode(nonArrayType, false, new Cursor(ctx));
    }

    @Override
    public ASTNode visitNewObjectConstruct(MxStarParser.NewObjectConstructContext ctx) {
        TypeNode nonArrayType = (TypeNode) visit(ctx.nonArrayType());
        return new NewTypeExpressionNode(nonArrayType, false, new Cursor(ctx));
    }
}
