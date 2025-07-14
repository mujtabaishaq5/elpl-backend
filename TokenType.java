package com.syed.elpl_backend;


public enum TokenType {

    // Keywords
    LET, BE, PRINT,
    IF, THEN, OTHERWISE,
    REPEAT, TIMES,
    WHILE,
    FUNCTION, CALL,TRUE,
   FALSE, AND, OR, NOT, FLOAT,
   FOR, TO, LBRACKET, RBRACKET, COMMA,ARRAY,STOP,RETURN,LPAREN,RPAREN, // For `for` and arrays
   SEMICOLON,MINUS,MOD,


    // Comparators
    IS_EQUAL_TO, IS_GREATER_THAN, IS_LESS_THAN,IS_GREATER_THAN_OR_EQUAL_TO,IS_LESS_THAN_OR_EQUAL_TO,
    NOT_EQUAL_TO,

    // Operators
    ADD, SUBTRACT, MULTIPLY, DIVIDE,

    // Literals
    IDENTIFIER, NUMBER, STRING,

    // Block delimiters
    LBRACE, RBRACE,

    // Misc
    COMMENT,
    EOF // End of file/input
}
