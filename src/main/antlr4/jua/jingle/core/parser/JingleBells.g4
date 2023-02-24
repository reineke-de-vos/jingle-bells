grammar JingleBells;

program : declaration+ ;

declaration : TYPE NAME defaultValue? ':' calculator+ ;

defaultValue : '=' intValue | floatValue ;

calculator : '(' nameList ')' '->' expression ';' ;

nameList : NAME+ ;

expression
    : '-' expression                                            # expOpNeg
    | left=expression operation=('*'|'/'|'%') right=expression  # expOpMulDiv
    | left=expression operation=('+'|'-') right=expression      # expOpAddSub
    | NAME                                                      # expJingleName
    | INTEGER                                                   # expIntValue
    | FLOAT                                                     # expFloatValue
    | '(' expression ')'                                        # expParenth
;

intValue : minus='-'? INTEGER ;
floatValue : minus='-'? FLOAT ;

TYPE : 'int' | 'float' ;

NAME : LETTER (LETTER | DIGIT)* ;

INTEGER : DIGIT+ ;

FLOAT
    : DIGIT+ '.' DIGIT* EXP?
    | DIGIT+ EXP?
    | '.' DIGIT+ EXP?
;

EXP : ('E' | 'e') ('+' | '-')? INTEGER ;

LETTER : [a-zA-Z] ;
DIGIT : [0-9] ;

WS : [ \r\t\n]+ -> skip ;
