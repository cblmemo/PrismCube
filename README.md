# PrismCube

![PrismCube](assets/PrismCube.png)

PrismCube, a toy Mx* compiler with java implementation.

![](https://img.shields.io/badge/version-1.0.0-green.svg)
![](https://img.shields.io/badge/JDK_version-17-orange.svg)
[![](https://img.shields.io/badge/source_language-Mx*-yellow.svg)](https://github.com/ACMClassCourses/Compiler-Design-Implementation)
![](https://img.shields.io/github/last-commit/Rainy-Memory/PrismCube)

![](https://img.shields.io/github/languages/top/Rainy-Memory/PrismCube)
![](https://img.shields.io/github/languages/code-size/Rainy-Memory/PrismCube)
![](https://img.shields.io/github/repo-size/Rainy-Memory/PrismCube)

## Mx* Language

Mx* is a simplified language from the mix of Standard C++ and Java. Details of Mx* can be found [here](https://github.com/ACMClassCourses/Compiler-Design-Implementation).

## Progress

 - [x] g4
 - [x] ArgumentProcessor (inside Memory)
 - [x] Preprocessor
 - [x] ASTBuilder
 - [x] ASTPrinter (inspired by [Masterball](https://github.com/SiriusNEO/Masterball))
 - [x] SymbolCollector
 - [x] ScopePrinter
 - [x] SemanticChecker
 - [x] ConstStringCollector
 - [x] ConstExprCalculator
 - [x] IRBuilder
 - [x] IRPrinter
 - [ ] CondeGen (developing...)

## File Structure

```
📦src
 ┣ 📂AST
 ┃ ┣ 📂DefineNode
 ┃ ┃ ┣ 📜ClassDefineNode.java
 ┃ ┃ ┣ 📜ConstructorDefineNode.java
 ┃ ┃ ┣ 📜FunctionDefineNode.java
 ┃ ┃ ┣ 📜ParameterDefineNode.java
 ┃ ┃ ┣ 📜ProgramDefineNode.java
 ┃ ┃ ┣ 📜SingleVariableDefineNode.java
 ┃ ┃ ┗ 📜VariableDefineNode.java
 ┃ ┣ 📂ExpressionNode
 ┃ ┃ ┣ 📜AddressingExpressionNode.java
 ┃ ┃ ┣ 📜AssignExpressionNode.java
 ┃ ┃ ┣ 📜BinaryExpressionNode.java
 ┃ ┃ ┣ 📜ExpressionNode.java
 ┃ ┃ ┣ 📜FunctionCallExpressionNode.java
 ┃ ┃ ┣ 📜LambdaExpressionNode.java
 ┃ ┃ ┣ 📜LeftValueExpressionNode.java
 ┃ ┃ ┣ 📜MemberAccessExpressionNode.java
 ┃ ┃ ┣ 📜NewTypeExpressionNode.java
 ┃ ┃ ┣ 📜PostCrementExpressionNode.java
 ┃ ┃ ┗ 📜UnaryExpressionNode.java
 ┃ ┣ 📂PrimaryNode
 ┃ ┃ ┣ 📜BoolConstantPrimaryNode.java
 ┃ ┃ ┣ 📜IdentifierPrimaryNode.java
 ┃ ┃ ┣ 📜NullConstantPrimaryNode.java
 ┃ ┃ ┣ 📜NumericalConstantPrimaryNode.java
 ┃ ┃ ┣ 📜PrimaryNode.java
 ┃ ┃ ┣ 📜StringConstantPrimaryNode.java
 ┃ ┃ ┗ 📜ThisPrimaryNode.java
 ┃ ┣ 📂StatementNode
 ┃ ┃ ┣ 📜BlockStatementNode.java
 ┃ ┃ ┣ 📜BreakStatementNode.java
 ┃ ┃ ┣ 📜ContinueStatementNode.java
 ┃ ┃ ┣ 📜EmptyStatementNode.java
 ┃ ┃ ┣ 📜ExpressionStatementNode.java
 ┃ ┃ ┣ 📜ForStatementNode.java
 ┃ ┃ ┣ 📜IfStatementNode.java
 ┃ ┃ ┣ 📜ReturnStatementNode.java
 ┃ ┃ ┣ 📜StatementNode.java
 ┃ ┃ ┗ 📜WhileStatementNode.java
 ┃ ┣ 📂TypeNode
 ┃ ┃ ┣ 📜ArrayTypeNode.java
 ┃ ┃ ┣ 📜BuiltinTypeNode.java
 ┃ ┃ ┣ 📜ClassTypeNode.java
 ┃ ┃ ┣ 📜ReturnTypeNode.java
 ┃ ┃ ┣ 📜SpecialTypeNode.java
 ┃ ┃ ┗ 📜TypeNode.java
 ┃ ┣ 📜ASTNode.java
 ┃ ┣ 📜ASTVisitor.java
 ┃ ┗ 📜ProgramNode.java
 ┣ 📂Debug
 ┃ ┣ 📜ASTPrinter.java
 ┃ ┣ 📜MemoLog.java
 ┃ ┗ 📜ScopePrinter.java
 ┣ 📂FrontEnd
 ┃ ┣ 📜ASTBuilder.java
 ┃ ┣ 📜ConstStringCollector.java
 ┃ ┣ 📜IRBuilder.java
 ┃ ┣ 📜IRPrinter.java
 ┃ ┣ 📜Preprocessor.java
 ┃ ┣ 📜SemanticChecker.java
 ┃ ┗ 📜SymbolCollector.java
 ┣ 📂IR
 ┃ ┣ 📂Instruction
 ┃ ┃ ┣ 📜IRAllocaInstruction.java
 ┃ ┃ ┣ 📜IRBinaryInstruction.java
 ┃ ┃ ┣ 📜IRBitcastInstruction.java
 ┃ ┃ ┣ 📜IRBrInstruction.java
 ┃ ┃ ┣ 📜IRCallInstruction.java
 ┃ ┃ ┣ 📜IRGetelementptrInstruction.java
 ┃ ┃ ┣ 📜IRIcmpInstruction.java
 ┃ ┃ ┣ 📜IRInstruction.java
 ┃ ┃ ┣ 📜IRLoadInstruction.java
 ┃ ┃ ┣ 📜IRReturnInstruction.java
 ┃ ┃ ┣ 📜IRStoreInstruction.java
 ┃ ┃ ┣ 📜IRTruncInstruction.java
 ┃ ┃ ┗ 📜IRZextInstruction.java
 ┃ ┣ 📂Operand
 ┃ ┃ ┣ 📜IRConstBool.java
 ┃ ┃ ┣ 📜IRConstChar.java
 ┃ ┃ ┣ 📜IRConstInt.java
 ┃ ┃ ┣ 📜IRConstString.java
 ┃ ┃ ┣ 📜IRGlobalVariableRegister.java
 ┃ ┃ ┣ 📜IRLabel.java
 ┃ ┃ ┣ 📜IRNull.java
 ┃ ┃ ┣ 📜IROperand.java
 ┃ ┃ ┣ 📜IRRegister.java
 ┃ ┃ ┗ 📜IRZeroInitializer.java
 ┃ ┣ 📂TypeSystem
 ┃ ┃ ┣ 📜IRArrayType.java
 ┃ ┃ ┣ 📜IRIntType.java
 ┃ ┃ ┣ 📜IRNullType.java
 ┃ ┃ ┣ 📜IRPointerType.java
 ┃ ┃ ┣ 📜IRStructureType.java
 ┃ ┃ ┣ 📜IRTypeSystem.java
 ┃ ┃ ┗ 📜IRVoidType.java
 ┃ ┣ 📜IRBasicBlock.java
 ┃ ┣ 📜IRFunction.java
 ┃ ┣ 📜IRGlobalDefine.java
 ┃ ┣ 📜IRModule.java
 ┃ ┗ 📜IRVisitor.java
 ┣ 📂Memory
 ┃ ┗ 📜Memory.java
 ┣ 📂Parser
 ┃ ┣ 📜MxStar.g4
 ┃ ┣ 📜MxStar.interp
 ┃ ┣ 📜MxStar.tokens
 ┃ ┣ 📜MxStarBaseListener.java
 ┃ ┣ 📜MxStarBaseVisitor.java
 ┃ ┣ 📜MxStarLexer.interp
 ┃ ┣ 📜MxStarLexer.java
 ┃ ┣ 📜MxStarLexer.tokens
 ┃ ┣ 📜MxStarListener.java
 ┃ ┣ 📜MxStarParser.java
 ┃ ┗ 📜MxStarVisitor.java
 ┣ 📂Utility
 ┃ ┣ 📂Entity
 ┃ ┃ ┣ 📜ConstructorEntity.java
 ┃ ┃ ┣ 📜Entity.java
 ┃ ┃ ┣ 📜FunctionEntity.java
 ┃ ┃ ┣ 📜MethodEntity.java
 ┃ ┃ ┗ 📜VariableEntity.java
 ┃ ┣ 📂error
 ┃ ┃ ┣ 📜ArgumentParseError.java
 ┃ ┃ ┣ 📜error.java
 ┃ ┃ ┣ 📜IRError.java
 ┃ ┃ ┣ 📜LogError.java
 ┃ ┃ ┣ 📜SemanticError.java
 ┃ ┃ ┗ 📜SyntaxError.java
 ┃ ┣ 📂Scope
 ┃ ┃ ┣ 📜BlockScope.java
 ┃ ┃ ┣ 📜BracesScope.java
 ┃ ┃ ┣ 📜BranchScope.java
 ┃ ┃ ┣ 📜ClassScope.java
 ┃ ┃ ┣ 📜ConstructorScope.java
 ┃ ┃ ┣ 📜FunctionScope.java
 ┃ ┃ ┣ 📜GlobalScope.java
 ┃ ┃ ┣ 📜LoopScope.java
 ┃ ┃ ┣ 📜MethodScope.java
 ┃ ┃ ┗ 📜Scope.java
 ┃ ┣ 📂Type
 ┃ ┃ ┣ 📜ArrayType.java
 ┃ ┃ ┣ 📜ClassType.java
 ┃ ┃ ┗ 📜Type.java
 ┃ ┣ 📜Cursor.java
 ┃ ┗ 📜MxStarErrorListener.java
 ┗ 📜PrismCube.java
```
