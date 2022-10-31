grammar JingleBells;

program : declaration+ ;

// declaration : TYPE NAME '=' expression ';' ;
declaration : TYPE NAME defaultValue? ':' calculator+ ;

defaultValue : '=' INTEGER ;

calculator : '(' nameList ')' '->' expression ';' ;

nameList : NAME+ ;

expression
    : '-' expression                              # expOpNeg
    | expression operation=('*'|'/'|'%') expression   # expOpMulDiv
    | expression operation=('+'|'-') expression   # expOpSumSub
    | NAME                                        # expJingleName
    | INTEGER                                     # expValue
    | '(' expression ')'                          # expBraces
;

TYPE : 'int' ;

NAME : LETTER (LETTER | DIGIT)* ;

INTEGER : DIGIT+ ;

LETTER : [a-zA-Z] ;
DIGIT : [0-9] ;

WS : [ \r\t\n]+ -> skip ;
