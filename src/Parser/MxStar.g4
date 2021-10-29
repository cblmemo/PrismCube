grammar MxStar;

program: (programDefine)* EOF;

programDefine: classDefine | variableDefine | functionDefine;

classDefine: Class className = Identifier '{' (variableDefine | methodDefine)* '}' ';';

variableDefine: type singleVariableDefine (',' singleVariableDefine)* ';';

singleVariableDefine: variableName = Identifier ('=' expression)?;

methodDefine: constructorDefine | functionDefine;

constructorDefine: className = Identifier '(' ')' suite;

functionDefine: returnType functionName = Identifier '(' parameterList? ')' suite;

parameterList: parameterDefine (',' parameterDefine)*;

parameterDefine: type parameterName = Identifier;

returnType: Void | type;

type
    : nonArrayType                                                                                       #nArrayType
    | type '[' ']'                                                                                       #arrayType
    ;

nonArrayType: builtinType | className = Identifier;

builtinType: Bool | Int | String;

suite: '{' statement* '}';

statement
    : suite                                                                                              #blockStatement
    | variableDefine                                                                                     #variableDefineStatement
    | If '(' expression ')' trueStatement = statement
        (Else falseStatement = statement)?                                                               #ifStatement
    | For   '(' initializeExpression = expression? ';'
                conditionExpression  = expression? ';'
                stepExpression       = expression? ')' statement                                         #forStatement
    | While '(' conditionExpression  = expression  ')' statement                                         #whileStatement
    | Return expression? ';'                                                                             #returnStatement
    | Break ';'                                                                                          #breakStatement
    | Continue ';'                                                                                       #continueStatement
    | expression ';'                                                                                     #expressionStatement
    | ';'                                                                                                #emptyStatement
    ;

expression
    : primary                                                                                            #atomExpression
    | newType                                                                                            #newTypeExpression
    | expression '.' memberName = Identifier                                                             #memberAccessExpression
    | '[&]' ('(' parameterList? ')')? '->' suite '(' argumentList? ')'                                   #lambdaExpression
    | function = expression '(' argumentList? ')'                                                        #functionCallExpression
    | array = expression '[' index = expression ']'                                                      #addressingExpression
    | expression op = ('++' | '--')                                                                      #postCrementExpression
    | <assoc=right> op = ('++' | '--') expression                                                        #unaryExpression
    | <assoc=right> op = ('+' | '-') expression                                                          #unaryExpression
    | <assoc=right> op = ('!' | '~') expression                                                          #unaryExpression
    | leftExpression = expression op = ('*' | '/' | '%') rightExpression = expression                    #binaryExpression
    | leftExpression = expression op = ('+' | '-') rightExpression = expression                          #binaryExpression
    | leftExpression = expression op = ('<<' | '>>') rightExpression = expression                        #binaryExpression
    | leftExpression = expression op = ('<' | '<=' | '>' | '>=') rightExpression = expression            #binaryExpression
    | leftExpression = expression op = ('==' | '!=') rightExpression = expression                        #binaryExpression
    | leftExpression = expression op = '&' rightExpression = expression                                  #binaryExpression
    | leftExpression = expression op = '^' rightExpression = expression                                  #binaryExpression
    | leftExpression = expression op = '|' rightExpression = expression                                  #binaryExpression
    | leftExpression = expression op = '&&' rightExpression = expression                                 #binaryExpression
    | leftExpression = expression op = '||' rightExpression = expression                                 #binaryExpression
    | <assoc=right> leftExpression = expression op = '=' rightExpression = expression                    #assignExpression
    ;

primary: '(' expression ')' | This | literal | Identifier;

newType
    : New nonArrayType ('[' expression ']')* ('[' ']')+ ('[' expression ']')+ ('[' expression? ']')*     #errorNewArray
    | New nonArrayType ('[' expression ']')+ ('[' ']')*                                                  #newArray
    | New nonArrayType '(' ')'                                                                           #newObjectConstruct
    | New nonArrayType                                                                                   #newObject
    ;

argumentList: expression (',' expression)*;

literal: NumericalConstant | BoolConstant | StringConstant | NullConstant;

NumericalConstant: [1-9][0-9]* | '0';

BoolConstant: True | False;

StringConstant: '"' ('\\\\' | '\\"' | .)*? '"';

NullConstant: Null;

Class: 'class';
Void: 'void';
Bool: 'bool';
Int: 'int';
String: 'string';
If: 'if';
Else: 'else';
For: 'for';
While: 'while';
Return: 'return';
Break: 'break';
Continue: 'continue';
This: 'this';
True: 'true';
False: 'false';
Null: 'null';
New: 'new';

Identifier: [a-zA-Z][a-zA-Z_0-9]*;

// skip white space
WhiteSpace: [ \t]+ -> skip;
NewLine: ('\r' '\n'? | '\n') -> skip;
BlockComment: '/*' .*? '*/' -> skip;
LineComment: '//' ~[\r\n]* -> skip;