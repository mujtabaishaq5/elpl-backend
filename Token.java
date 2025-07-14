package com.syed.elpl_backend;

public class Token {
    TokenType type;
    String value;
    int line;
    int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return type + "(" + value + ") at line " + line + ", column " + column;
    }
}
