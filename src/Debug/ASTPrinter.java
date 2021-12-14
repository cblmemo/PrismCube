package Debug;

import AST.ASTNode;
import AST.DefineNode.*;
import AST.ExpressionNode.*;
import AST.PrimaryNode.*;
import AST.ProgramNode;
import AST.StatementNode.*;
import AST.TypeNode.*;
import Memory.Memory;

/**
 * This class prints abstract syntax tree generated
 * by AST builder to the screen. Using idents, it is
 * easy to read.
 * This class is inspired by MasterBall.
 *
 * @author rainy memory
 * @version 1.0.0
 * @see FrontEnd.ASTBuilder
 */

public class ASTPrinter {
    private int indentCnt = 0;

    static boolean print = false;

    public static void enable() {
        print = true;
    }

    public void print(Memory memory) {
        if (print) {
            printf("-----------------------------------------ASTPrinter-----------------------------------------\n");
            printNode(memory.getASTRoot());
            printf("-----------------------------------------ASTPrinter-----------------------------------------\n");
        }
    }

    /**
     * This method implements printf in c++
     * with auto-indent.
     *
     * @see java.io.PrintStream
     */
    private void printf(String format, Object... args) {
        for (int i = 0; i < indentCnt; i++) System.out.print("\t");
        System.out.printf(format, args);
    }

    private void enter(String name) {
        printf("[begin] %s:\n", name);
        indentCnt++;
    }

    private void leave(String name) {
        indentCnt--;
        printf("[ end ] %s.\n", name);
    }

    private void printNode(ProgramNode node) {
        enter("ProgramNode");
        printf("program defines: (%d)\n", node.getDefines().size());
        if (node.isInvalid()) printf("main function error!!!!!!!!!!!!!!!\n");
        for (var innerNode : node.getDefines()) {
            if (innerNode instanceof ClassDefineNode) printNode((ClassDefineNode) innerNode);
            if (innerNode instanceof VariableDefineNode) printNode((VariableDefineNode) innerNode);
            if (innerNode instanceof FunctionDefineNode) printNode((FunctionDefineNode) innerNode);
        }
        leave("ProgramNode");
    }

    private void printNode(ClassDefineNode node) {
        enter("ClassDefineNode");
        printf("class name: %s\n", node.getClassName());
        if (node.isInvalid()) {
            printf("Invalid Class Define!!\nerror message: %s.\n", node.getMessage());
            return;
        }
        if (node.getMembers().size() == 0) {
            printf("class has no variable.\n");
        } else {
            printf("class variables: (%d)\n", node.getMembers().size());
            for (var variable : node.getMembers()) {
                printNode(variable);
            }
        }
        if (node.getMethods().size() == 0) {
            printf("class has no functions.\n");
        } else {
            printf("class functions: (%d)\n", node.getMethods().size());
            for (var function : node.getMethods()) {
                printNode(function);
            }
        }
        if (node.getConstructor() == null) {
            printf("class has no constructor.\n");
        } else {
            printf("class constructors:\n");
            printNode(node.getConstructor());
        }
        leave("ClassDefineNode");
    }

    private void printNode(VariableDefineNode node) {
        enter("VariableDefineNode");
        printf("variable type:\n");
        printNode(node.getType());
        printf("single variable defines: (%d)\n", node.getSingleDefines().size());
        for (var singleDefine : node.getSingleDefines()) {
            printNode(singleDefine);
        }
        leave("VariableDefineNode");
    }

    private void printNode(FunctionDefineNode node) {
        enter("FunctionDefineNode");
        printf("function return type:\n");
        printNode(node.getReturnType());
        printf("function name: %s\n", node.getFunctionName());
        if (node.getParameters().size() == 0) {
            printf("function has no parameters.\n");
        } else {
            printf("function parameters: (%d)\n", node.getParameters().size());
            for (var parameter : node.getParameters()) {
                printNode(parameter);
            }
        }
        if (node.getStatements().size() == 0) {
            printf("function has no statements.\n");
        } else {
            printf("function statements: (%d)\n", node.getStatements().size());
            for (var statement : node.getStatements()) {
                printNode(statement);
            }
        }
        leave("FunctionDefineNode");
    }

    private void printNode(ConstructorDefineNode node) {
        enter("ConstructorDefineNode");
        printf("constructor name: %s\n", node.getConstructorName());
        if (node.getStatements().size() == 0) {
            printf("constructor has no statements.\n");
        } else {
            printf("constructor statements: (%d)\n", node.getStatements().size());
            for (var statement : node.getStatements()) {
                printNode(statement);
            }
        }
        leave("ConstructorDefineNode");
    }

    private void printNode(TypeNode node) {
        enter("TypeNode");
        if (node instanceof ArrayTypeNode) {
            printf("[ArrayTypeNode]\n");
            printf("dimension: %d\n", ((ArrayTypeNode) node).getDimension());
            printf("element type:\n");
            printNode(((ArrayTypeNode) node).getElementType());
        }
        if (node instanceof BuiltinTypeNode) {
            printf("[BuiltinTypeNode]\n");
            printf("type name: %s\n", node.getTypeName());
        }
        if (node instanceof ClassTypeNode) {
            printf("[ClassTypeNode]\n");
            printf("type name: %s\n", node.getTypeName());
        }
        if (node instanceof ReturnTypeNode) {
            printf("[ReturnTypeNode]\n");
            printf("type name: %s\n", node.getTypeName());
        }
        if (node instanceof SpecialTypeNode) {
            printf("[SpecialTypeNode]\n");
            printf("type name: %s\n", node.getTypeName());
        }
        leave("TypeNode");
    }

    private void printNode(SingleVariableDefineNode node) {
        enter("SingleVariableDefineNode");
        printf("variable name: %s\n", node.getVariableNameStr());
        if (node.getInitializeValue() == null) {
            printf("single variable define has no initialize value.\n");
        } else {
            printf("initialize value:\n");
            printNode(node.getInitializeValue());
        }
        leave("SingleVariableDefineNode");
    }

    private void printNode(ParameterDefineNode node) {
        enter("ParameterDefineNode");
        printf("parameter type:\n");
        printNode(node.getType());
        printf("parameter name: %s\n", node.getParameterName());
        leave("ParameterDefineNode");
    }

    private void printNode(StatementNode node) {
        enter("StatementNode");
        if (node instanceof VariableDefineNode) {
            printf("[variableDefineStatementNode]\n");
            printNode((VariableDefineNode) node);
        }
        if (node instanceof BlockStatementNode) {
            printf("[BlockStatementNode]\n");
            if (((BlockStatementNode) node).getStatements().size() == 0) {
                printf("blockStatement has no statement.\n");
            } else {
                printf("block statement: (%d)\n", ((BlockStatementNode) node).getStatements().size());
                for (var statement : ((BlockStatementNode) node).getStatements()) {
                    printNode(statement);
                }
            }
        }
        if (node instanceof BreakStatementNode) {
            printf("[BreakStatementNode]\n");
        }
        if (node instanceof ContinueStatementNode) {
            printf("[ContinueStatementNode]\n");
        }
        if (node instanceof EmptyStatementNode) {
            printf("[EmptyStatementNode]\n");
        }
        if (node instanceof ExpressionStatementNode) {
            printf("[ExpressionStatementNode]\n");
            printNode(((ExpressionStatementNode) node).getExpression());
        }
        if (node instanceof ForStatementNode) {
            printf("[ForStatementNode]\n");
            printf("initialize:\n");
            ASTNode init = ((ForStatementNode) node).getInitializeStatement();
            if (init instanceof VariableDefineNode) printNode((VariableDefineNode) init);
            else printNode((ExpressionNode) init);
            printf("condition:\n");
            printNode(((ForStatementNode) node).getConditionExpression());
            printf("step:\n");
            printNode(((ForStatementNode) node).getStepExpression());
            printf("loop body:\n");
            printNode(((ForStatementNode) node).getLoopBody());
        }
        if (node instanceof IfStatementNode) {
            printf("[IfStatementNode]\n");
            printf("condition:\n");
            printNode(((IfStatementNode) node).getConditionExpression());
            printf("true:\n");
            printNode(((IfStatementNode) node).getTrueStatement());
            if (((IfStatementNode) node).hasElse()) {
                printf("false:\n");
                printNode(((IfStatementNode) node).getFalseStatement());
            }
        }
        if (node instanceof ReturnStatementNode) {
            printf("[ReturnStatementNode]\n");
            printf("return value:\n");
            printNode(((ReturnStatementNode) node).getReturnValue());
        }
        if (node instanceof WhileStatementNode) {
            printf("[WhileStatementNode]\n");
            printf("condition:\n");
            printNode(((WhileStatementNode) node).getConditionExpression());
            printf("loop body:\n");
            printNode(((WhileStatementNode) node).getLoopBody());
        }
        leave("StatementNode");
    }

    private void printNode(ExpressionNode node) {
        enter("ExpressionNode");
        if (node instanceof AddressingExpressionNode) {
            printf("[AddressingExpressionNode]\n");
            printf("array:\n");
            printNode(((AddressingExpressionNode) node).getArray());
            printf("index:\n");
            printNode(((AddressingExpressionNode) node).getIndex());
        }
        if (node instanceof AssignExpressionNode) {
            printf("[AssignExpressionNode]\n");
            printf("lhs:\n");
            printNode(((AssignExpressionNode) node).getLhs());
            printf("op: %s\n", ((AssignExpressionNode) node).getOp());
            printf("rhs:\n");
            printNode(((AssignExpressionNode) node).getRhs());
        }
        if (node instanceof PrimaryNode) {
            printf("[PrimaryNode]\n");
            printNode((PrimaryNode) node);
        }
        if (node instanceof BinaryExpressionNode) {
            printf("[BinaryExpressionNode]\n");
            printf("lhs:\n");
            printNode(((BinaryExpressionNode) node).getLhs());
            printf("op: %s\n", ((BinaryExpressionNode) node).getOp());
            printf("rhs:\n");
            printNode(((BinaryExpressionNode) node).getRhs());
        }
        if (node instanceof FunctionCallExpressionNode) {
            printf("[FunctionCallExpressionNode]\n");
            printf("function:\n");
            printNode(((FunctionCallExpressionNode) node).getFunction());
            if (((FunctionCallExpressionNode) node).getArguments().size() == 0) {
                printf("function call expression has no argument.\n");
            } else {
                printf("arguments: (%d)\n", ((FunctionCallExpressionNode) node).getArguments().size());
                for (var argument : ((FunctionCallExpressionNode) node).getArguments()) {
                    printNode(argument);
                }
            }
        }
        if (node instanceof LambdaExpressionNode) {
            printf("[LambdaExpressionNode]\n");
            if (((LambdaExpressionNode) node).getParameters() == null) {
                printf("lambda function without parameter parentheses.\n");
            } else {
                if (((LambdaExpressionNode) node).getParameters().size() == 0) {
                    printf("lambda function has no parameter.\n");
                } else {
                    printf("lambda function parameters: (%d)\n", ((LambdaExpressionNode) node).getParameters().size());
                    for (var parameter : ((LambdaExpressionNode) node).getParameters()) {
                        printNode(parameter);
                    }
                }
            }
            if (((LambdaExpressionNode) node).getStatements().size() == 0) {
                printf("lambda function has no statements.\n");
            } else {
                printf("lambda function statements: (%d)\n", ((LambdaExpressionNode) node).getStatements().size());
                for (var statement : ((LambdaExpressionNode) node).getStatements()) {
                    printNode(statement);
                }
            }
            if (((LambdaExpressionNode) node).getArguments().size() == 0) {
                printf("lambda function has no argument.\n");
            } else {
                printf("lambda function arguments: (%d)\n", ((LambdaExpressionNode) node).getArguments().size());
                for (var argument : ((LambdaExpressionNode) node).getArguments()) {
                    printNode(argument);
                }
            }
        }
        if (node instanceof MemberAccessExpressionNode) {
            printf("[MemberAccessExpressionNode]\n");
            printf("instance:\n");
            printNode(((MemberAccessExpressionNode) node).getInstance());
            printf("member name: %s\n", ((MemberAccessExpressionNode) node).getMemberName());
        }
        if (node instanceof NewTypeExpressionNode) {
            printf("[NewTypeExpressionNode]\n");
            if (((NewTypeExpressionNode) node).isInvalid()) {
                printf("ErrorNewArray!\n");
            } else {
                printf("nonarray type:\n");
                printNode(((NewTypeExpressionNode) node).getRootElementType());
                printf("dimension: %d\n", ((NewTypeExpressionNode) node).getDimension());
                if (((NewTypeExpressionNode) node).getDimensionExpressions().size() != 0) {
                    printf("dimension expressions: (%d)\n", ((NewTypeExpressionNode) node).getDimensionExpressions().size());
                    for (var expression : ((NewTypeExpressionNode) node).getDimensionExpressions()) {
                        printNode(expression);
                    }
                }
            }
        }
        if (node instanceof PostCrementExpressionNode) {
            printf("[PostCrementExpressionNode]\n");
            printf("lhs:\n");
            printNode(((PostCrementExpressionNode) node).getLhs());
            printf("op: %s\n", ((PostCrementExpressionNode) node).getOp());
        }
        if (node instanceof UnaryExpressionNode) {
            printf("[UnaryExpressionNode]\n");
            printf("op: %s\n", ((UnaryExpressionNode) node).getOp());
            printf("rhs:\n");
            printNode(((UnaryExpressionNode) node).getRhs());
        }
        leave("ExpressionNode");
    }

    private void printNode(PrimaryNode node) {
        enter("PrimaryNode");
        if (node instanceof BoolConstantPrimaryNode) {
            printf("[BoolConstantPrimaryNode]\n");
            printf("bool value: %b\n", ((BoolConstantPrimaryNode) node).getBoolConstant());
        }
        if (node instanceof IdentifierPrimaryNode) {
            printf("[IdentifierPrimaryNode]\n");
            printf("identifier name: %s\n", ((IdentifierPrimaryNode) node).getIdentifier());
        }
        if (node instanceof NullConstantPrimaryNode) {
            printf("[NullConstantPrimaryNode]\n");
            printf("null\n");
        }
        if (node instanceof NumericalConstantPrimaryNode) {
            printf("[NumericalConstantPrimaryNode]\n");
            printf("numerical constant value: %d\n", ((NumericalConstantPrimaryNode) node).getNumericalConstant());
        }
        if (node instanceof StringConstantPrimaryNode) {
            printf("[StringConstantPrimaryNode]\n");
            printf("string constant value: %s\n", ((StringConstantPrimaryNode) node).getStringConstant());
        }
        if (node instanceof ThisPrimaryNode) {
            printf("[ThisPrimaryNode]\n");
            printf("this\n");
        }
        leave("PrimaryNode");
    }
}
