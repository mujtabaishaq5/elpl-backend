package com.syed.elpl_backend;

import java.util.*;

class Lexer {
    private String input;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            if (Character.isWhitespace(current())) {
                advance();
                continue;
            }

            // Single-line comment: starts with /
            if (current() == '/') {
                skipSingleLineComment();
                continue;
            }

            // Multi-line comment: > ... <
            if (current() == '>') {
                skipMultiLineComment();
                continue;
            }

            // --- Language Keywords ---
            if (match("let")) tokens.add(new Token(TokenType.LET, "let", line, column));
            else if(match("float")) tokens.add(new Token(TokenType.FLOAT, "float", line, column));
            else if (match("for")) tokens.add(new Token(TokenType.FOR, "for", line, column));
            else if (match("to")) tokens.add(new Token(TokenType.TO, "to", line, column));
            else if (current() == '[') { tokens.add(new Token(TokenType.LBRACKET, "[", line, column)); advance(); }
            else if (current() == ']') { tokens.add(new Token(TokenType.RBRACKET, "]", line, column)); advance(); }
            else if (current() == ',') { tokens.add(new Token(TokenType.COMMA, ",", line, column)); advance(); }

            else if (match("be")) tokens.add(new Token(TokenType.BE, "be", line, column));
            else if (match("print")) tokens.add(new Token(TokenType.PRINT, "print", line, column));
            else if (match("add")) tokens.add(new Token(TokenType.ADD, "add", line, column));
            else if (match("subtract")) tokens.add(new Token(TokenType.SUBTRACT, "subtract", line, column));
            else if (match("multiply")) tokens.add(new Token(TokenType.MULTIPLY, "multiply", line, column));
            else if (match("divide")) tokens.add(new Token(TokenType.DIVIDE, "divide", line , column));

            // Booleans 
            else if (match("true")) tokens.add(new Token(TokenType.TRUE, "true", line, column));
            else if (match("false")) tokens.add(new Token(TokenType.FALSE, "false", line, column));


            // Arrays
            else if (match("Array")) tokens.add(new Token(TokenType.ARRAY, "Array", line, column));

            // stop

            else if (match("stop")) tokens.add(new Token(TokenType.STOP, "stop", line, column));

            //MOD
            else if(match("mod")) tokens.add(new Token(TokenType.MOD, "mod", line, column));
            


            // Conditional logic
            else if (match("is greater than or equal to")) tokens.add(new Token(TokenType.IS_GREATER_THAN_OR_EQUAL_TO, "is greater than or equal to", line, column));

            else if (match("is less than or equal to")) tokens.add(new Token(TokenType.IS_LESS_THAN_OR_EQUAL_TO, "is less than or equal to", line, column));

            else if (match("not equal to")) tokens.add(new Token(TokenType.NOT_EQUAL_TO, "not equal to", line, column));
            
            else if (match("is greater than")) tokens.add(new Token(TokenType.IS_GREATER_THAN, "is greater than", line, column));
            else if (match("is less than")) tokens.add(new Token(TokenType.IS_LESS_THAN, "is less than", line, column));
            else if (match("is equal to")) tokens.add(new Token(TokenType.IS_EQUAL_TO, "is equal to", line, column));
            else if (match("otherwise")) tokens.add(new Token(TokenType.OTHERWISE, "otherwise", line, column));
            else if (match("if")) tokens.add(new Token(TokenType.IF, "if", line, column));
            else if (match("then")) tokens.add(new Token(TokenType.THEN, "then", line, column));
            else if (match("and")) tokens.add(new Token(TokenType.AND, "and", line, column));
            else if (match("or")) tokens.add(new Token(TokenType.OR, "or", line, column));
            else if (match("not")) tokens.add(new Token(TokenType.NOT, "not", line, column));

            // semicolon
            else if (current() == ';') { tokens.add(new Token(TokenType.SEMICOLON, ";", line, column)); advance(); }

            // minus
            else if (current() == '-'){
                tokens.add(new Token(TokenType.MINUS, "-", line, column));
                advance();
                continue;
            }
            
         

            // Loops
            else if (match("repeat")) tokens.add(new Token(TokenType.REPEAT, "repeat", line, column));
            else if (match("times")) tokens.add(new Token(TokenType.TIMES, "times", line, column));
            else if (match("while")) tokens.add(new Token(TokenType.WHILE, "while", line, column));

            // return
            else if(match("return")) tokens.add(new Token(TokenType.RETURN, "return", line, column));

            // Functions
            else if (match("function")) tokens.add(new Token(TokenType.FUNCTION, "function", line, column));
            else if (match("call")) tokens.add(new Token(TokenType.CALL, "call", line, column));

            // right and left parentheses
            else if(current() == '('){
                tokens.add(new Token(TokenType.LPAREN, "(", line, column));
                advance();
            } else if(current() == ')'){
                tokens.add(new Token(TokenType.RPAREN, ")", line, column));
                advance();
            }

            // Blocks
            else if (current() == '{') {
                tokens.add(new Token(TokenType.LBRACE, "{", line, column));
                advance();
            } else if (current() == '}') {
                tokens.add(new Token(TokenType.RBRACE, "}", line, column));
                advance();
            }

            // String literals (e.g., "hello")
            else if (current() == '"') {
                tokens.add(new Token(TokenType.STRING, readString(), line, column));
            }

            // Numbers (e.g., 123)
            else if (Character.isDigit(current())) {
                tokens.add(new Token(TokenType.NUMBER, readNumber(), line, column));
            }

            // Identifiers (e.g., variable names)
            else if (Character.isLetter(current())) {
                tokens.add(new Token(TokenType.IDENTIFIER, readIdentifier(), line, column));
            }

            // Unknown character
            else {
                throw new RuntimeException("Unexpected character: " + current());
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }
    private int advance(){
        return pos++;
    }
    private char peek(){
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }


    // Match keyword and ensure it is followed by space or end of input
    private boolean match(String keyword) {
        int len = keyword.length();
        if (input.regionMatches(pos, keyword, 0, len)) {
            if (pos + len == input.length() || Character.isWhitespace(input.charAt(pos + len)) || isPunctuation(input.charAt(pos + len))) {
                pos += len;
                return true;
            }
        }
        return false;
    }
    private boolean isPunctuation(char c){
        return ",.{}[]()\"".indexOf(c) >= 0;
    }

    private char current() {
        return input.charAt(pos);
    }

    // Read string literal
    private String readString() {
        advance(); // Skip opening quote
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && input.charAt(pos) != '"') {
            sb.append(input.charAt(advance()));
        }
        advance(); // Skip closing quote
        return sb.toString();
    }

    // Read number literal
   private String readNumber() {
    StringBuilder sb = new StringBuilder();
    boolean hasDecimal = false;

    while (pos < input.length()) {
        char ch = input.charAt(pos);

        if (Character.isDigit(ch)) {
            sb.append(ch);
            advance();
        } else if (ch == '.' && !hasDecimal) {
            hasDecimal = true;
            sb.append(ch);
            advance();
        } else {
            break;
        }
    }
   /* if (sb.charAt(sb.length() - 1) == ".") {
        throw new RuntimeException("Invalid number format: ends with decimal point");
    }  unResolved at line 158 will resolve later*/

    return sb.toString();
}


    // Read identifier (variable/function name)
    private String readIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            sb.append(input.charAt(advance()));
        }
        return sb.toString();
    }

    // Skip single-line comments
    private void skipSingleLineComment() {
        advance(); // Skip '/'
        while (pos < input.length() && input.charAt(pos) != '\n') {
            advance();
        }
    }

    // Skip multi-line comments
    private void skipMultiLineComment() {
        advance(); // Skip '>'
        while (pos < input.length()) {
            if (input.charAt(pos) == '<') {
                advance(); // Skip closing '<'
                break;
            }
            advance();
        }
    }
}
