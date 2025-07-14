package com.syed.elpl_backend;

import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parse() {
        List<ASTNode> statements = new ArrayList<>();
        while (!match(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        return new Program(statements);
    }

    private ASTNode parseStatement() {
        if (check(TokenType.LET) || check(TokenType.FLOAT)) return parseAssignment();
        if (match(TokenType.PRINT)) return parsePrint();
        if (match(TokenType.IF)) return parseIf();
        if (match(TokenType.FOR)) return parseFor();
        if (match(TokenType.REPEAT)) return parseRepeat();
        if (match(TokenType.WHILE)) return parseWhile();
        if (match(TokenType.ARRAY)) return parseArrayDeclaration();
        if (match(TokenType.FUNCTION)) return parseFunction();
        if (match(TokenType.CALL)){
            String functionName = consume(TokenType.IDENTIFIER).value;
            return parseFunctionCall(functionName);
        }
        if (match(TokenType.NOT)) return parseFactor();
        if (match(TokenType.STOP)) return parseStop();
        if (match(TokenType.RETURN)) return parseReturn();
        if (check(TokenType.IDENTIFIER) && lookAheadIsArrayAssignment()){
            return parseArrayAssignment();
        }
        throw new RuntimeException("Unknown statement at: " + peek().value);
    }
    private boolean lookAheadIsArrayAssignment(){
        int current = pos;
        if (current + 3 >= tokens.size()) return false;

        return tokens.get(current + 1).type == TokenType.LBRACKET && tokens.get(current + 3).type == TokenType.BE;
    }
    private ASTNode parseArrayAssignment() {
    String arrayName = consume(TokenType.IDENTIFIER).value;
    consume(TokenType.LBRACKET);
    ExpressionNode index = parseExpression();
    consume(TokenType.RBRACKET);
    consume(TokenType.BE);
    ExpressionNode value = parseExpression();
    return new ArrayAssignNode(arrayName, index, value);
}

    private ASTNode parseStop(){
        return new StopNode();
    }
   

    private ArrayDecNode parseArrayDeclaration(){
        String name = consume(TokenType.IDENTIFIER).value;
        if(isBuiltInFunction(name)){
            throw new RuntimeException("cannot use built-in function name'" + name + "'as a variable");
        }
        consume(TokenType.BE); // assign
        consume(TokenType.LBRACKET); // '['

        List<ExpressionNode> elements = new ArrayList<>();
        if(!check(TokenType.RBRACKET)){
            elements.add(parseExpression());
            while (match(TokenType.COMMA)){
                elements.add(parseExpression());
            }
        }
        consume(TokenType.RBRACKET); // ']'
        return new ArrayDecNode(name, elements);
    }
    private ForNode parseFor(){
        String id = consume(TokenType.IDENTIFIER).value;
        consume(TokenType.BE);
        ExpressionNode start = parseExpression();
        consume(TokenType.TO);
        ExpressionNode end = parseExpression();
        List<ASTNode> body = parseBlock();

        return new ForNode(id, start, end, body);
    }

   private ASTNode parseAssignment() {
    boolean isFloat = match(TokenType.FLOAT);
    if (!isFloat) match(TokenType.LET);

    String id = consume(TokenType.IDENTIFIER).value;
    if(isBuiltInFunction(id)){
        throw new RuntimeException("Cannot use built-in function name'" + id + "'as a variable");
    }

    // NEW: Handle array element assignment
    if (match(TokenType.LBRACKET)) {
        ExpressionNode index = parseExpression();
        consume(TokenType.RBRACKET);
        consume(TokenType.BE);
        ExpressionNode value = parseExpression();
        return new ArrayAssignNode(id, index, value);
    }

    // Default: Regular variable assignment
    consume(TokenType.BE);
    ExpressionNode expr = parseExpression();
    return new AssignmentNode(id, expr, isFloat);
}


    private PrintNode parsePrint() {
    List<ExpressionNode> parts = new ArrayList<>();

    while (true) {
        if (match(TokenType.STRING)) {
            parts.add(new StringLiteral(previous().value));
        } else if (check(TokenType.NUMBER) || check(TokenType.IDENTIFIER)) {
            parts.add(parseExpression());
        } else {
            // If next token is a keyword or structural token, we assume print has ended
            if (checkAny(
                TokenType.EOF, TokenType.LET, TokenType.IF, TokenType.PRINT,
                TokenType.OTHERWISE, TokenType.THEN, TokenType.RBRACE, TokenType.TIMES
            )) {
                break;
            }
            // If not a valid expression, break (failsafe)
            break;
        }
    }

    return new PrintNode(parts);
}

private IfNode parseIf() {
        Condition condition = parseCondition();
        consume(TokenType.THEN);
        BlockNode thenBlock = new BlockNode(parseBlock());

        BlockNode elseBlock = null;

        if(match(TokenType.OTHERWISE)){
            elseBlock = new BlockNode(parseBlock());
        }
        
        
        return new IfNode(condition, thenBlock, elseBlock);
    }
    private List<ASTNode> parseBlock(){
        consume(TokenType.LBRACE);
        List<ASTNode> statements = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !check(TokenType.EOF)){
            statements.add(parseStatement());
        }
        consume(TokenType.RBRACE);

        return statements;
    }

    private RepeatNode parseRepeat() {
        ExpressionNode expr = parseExpression();
        if (!(expr instanceof NumberLiteral)) {
            throw new RuntimeException("Repeat count must be a number literal.");
        }
        double val = ((NumberLiteral) expr).value;
         if (val != Math.floor(val)) {
        throw new RuntimeException("Repeat count must be a whole number.");
}
         int times = (int) val;

        consume(TokenType.TIMES);
        List<ASTNode> body = parseBlock();
        return new RepeatNode(times, body);
    }

    private WhileNode parseWhile() {
        Condition condition = parseCondition();
        List<ASTNode> body = parseBlock();
        return new WhileNode(condition, body);
    }

    private FunctionDeclNode parseFunction() {
    String name = consume(TokenType.IDENTIFIER).value;
    if(isBuiltInFunction(name)){
        throw new RuntimeException("Cannot use built-in function name'" + name + "' as a variable"); 
    }

    List<String> parameters = new ArrayList<>();
    consume(TokenType.LPAREN); // Consume '(' to start parameters

    if (!check(TokenType.RPAREN)) { // If not empty parameter list
        do {
            parameters.add(consume(TokenType.IDENTIFIER).value);
        } while (match(TokenType.COMMA));
    }
    consume(TokenType.RPAREN); // Consume ')'

    // Now parse the function body, typically a block starting with '{'
    List<ASTNode> body = parseBlock();

    return new FunctionDeclNode(name, parameters, body);
}


    private FunctionCallNode parseFunctionCall(String functionName) {
        List<ExpressionNode> arguments = new ArrayList<>();

        if(match(TokenType.LPAREN)){
            if(!check(TokenType.RPAREN)){
                do {
                    arguments.add(parseExpression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RPAREN);
        }
        return new FunctionCallNode(functionName, arguments);
    }

    

  private Condition parseCondition() {
    ExpressionNode expr = parseExpression(); // can now include comparisons and logical ops
    return new Condition(expr);
}
private ASTNode parseReturn(){
    if (check(TokenType.SEMICOLON) ||
        check(TokenType.RBRACE) ||
        check(TokenType.EOF) ||
        check(TokenType.IF) ||
        check(TokenType.RETURN) ||
        check(TokenType.FUNCTION) ||
        check(TokenType.LET) ||
        check(TokenType.FLOAT) ||
        check(TokenType.ARRAY) ||
        check(TokenType.PRINT)
    ) {
        return new ReturnNode(null);
    }
    ExpressionNode returnValue = parseExpression();
    return new ReturnNode(returnValue);
}
private ExpressionNode parseExpression() {
    return parseLogicalOr();
}

private ExpressionNode parseLogicalOr() {
    ExpressionNode left = parseLogicalAnd();
    while (match(TokenType.OR)) {
        ExpressionNode right = parseLogicalAnd();
        left = new BinaryExpr(left, "or", right);
    }
    return left;
}

private ExpressionNode parseLogicalAnd() {
    ExpressionNode left = parseEquality();
    while (match(TokenType.AND)) {
        ExpressionNode right = parseEquality();
        left = new BinaryExpr(left, "and", right);
    }
    return left;
}

private ExpressionNode parseEquality() {
    ExpressionNode left = parseRelational();
    while (match(TokenType.IS_EQUAL_TO) || match(TokenType.NOT_EQUAL_TO)) {
        String op = previous().type == TokenType.IS_EQUAL_TO ? "is equal to" : "not equal to";
        ExpressionNode right = parseRelational();
        left = new BinaryExpr(left, op, right);
    }
    return left;
}

private ExpressionNode parseRelational() {
    ExpressionNode left = parseAdditive();
    while (match(TokenType.IS_GREATER_THAN) || match(TokenType.IS_LESS_THAN) || 
           match(TokenType.IS_GREATER_THAN_OR_EQUAL_TO) || match(TokenType.IS_LESS_THAN_OR_EQUAL_TO)) {
        String op = switch (previous().type) {
            case IS_GREATER_THAN -> "is greater than";
            case IS_LESS_THAN -> "is less than";
            case IS_GREATER_THAN_OR_EQUAL_TO -> "is greater than or equal to";
            case IS_LESS_THAN_OR_EQUAL_TO -> "is less than or equal to";
            case NOT_EQUAL_TO ->   "not equal to";     
            default -> throw new RuntimeException("Unexpected operator");
        };
        ExpressionNode right = parseAdditive();
        left = new BinaryExpr(left, op, right);
    }
    return left;
}

private ExpressionNode parseAdditive() {
    ExpressionNode left = parseMultiplicative();
    while (match(TokenType.ADD) || match(TokenType.SUBTRACT)) {
        String op = previous().type == TokenType.ADD ? "add" : "subtract";
        ExpressionNode right = parseMultiplicative();
        left = new BinaryExpr(left, op, right);
    }
    return left;
}

private ExpressionNode parseMultiplicative() {
    ExpressionNode left = parseUnary();
  while (match(TokenType.MULTIPLY) || match(TokenType.DIVIDE) || match(TokenType.MOD)) {
    String op = switch (previous().type) {
        case MULTIPLY -> "multiply";
        case DIVIDE -> "divide";
        case MOD -> "mod";
        default -> throw new RuntimeException("Unexpected multiplicative operator");
    };
    ExpressionNode right = parseUnary();
    left = new BinaryExpr(left, op, right);
}

    return left;
}

private ExpressionNode parseUnary() {
    if (match(TokenType.MINUS)) {
        ExpressionNode expr = parseUnary();
        return new UnaryExpr("negate", expr);
    }
    if (match(TokenType.NOT)) {
        ExpressionNode expr = parseUnary();
        return new UnaryExpr("not", expr);
    }
    return parseFactor();
}

private ExpressionNode parseFactor() {
    
    if (match(TokenType.TRUE)) {
        return new BooleanLiteral(true);
    } 
    if (match(TokenType.FALSE)) {
        return new BooleanLiteral(false);
    }
    if (match(TokenType.NUMBER)) {
        return new NumberLiteral(Double.parseDouble(previous().value));
    } if(match(TokenType.STRING)){
        return new StringLiteral(previous().value);
    }
     if (match(TokenType.CALL)){
        String functionName = consume(TokenType.IDENTIFIER).value;
        return parseFunctionCall(functionName);
    }
    if (match(TokenType.IDENTIFIER)) {
        String name = previous().value;
        
        if (match(TokenType.LBRACKET)) { // Array access
            ExpressionNode index = parseExpression();
            consume(TokenType.RBRACKET);
            return new ArrayAccessNode(name, index);
        }
        
        if (match(TokenType.LPAREN)) { // Function call
            List<ExpressionNode> arguments = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                do {
                    arguments.add(parseExpression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RPAREN);
            return new FunctionCallNode(name, arguments);
        }
        
        // Just a variable
        return new VariableRef(name);
    }
    
    if (match(TokenType.LPAREN)) {
        ExpressionNode expr = parseExpression();
        consume(TokenType.RPAREN);
        return expr;
    }
    
   Token current = peek();
  throw new RuntimeException("Expected expression at line " + current.line + ", column " + current.column + ": found " + current.type);

}

private boolean isBuiltInFunction(String name){
        return Set.of("length", "sum", "max", "min", "sqrt", "abs", "pow", "floor", "ceil", "reverse").contains(name);
    }

    // Utility methods
    private boolean match(TokenType type) {
        if (check(type)) {
            pos++;
            return true;
        }
        return false;
    }

    private Token consume(TokenType type) {
    if (check(type)) return tokens.get(pos++);
    throw new RuntimeException("Expected token: " + type + ", but found: " + peek().type + " at position " + pos);
}


    private boolean check(TokenType type) {
        return pos < tokens.size() && tokens.get(pos).type == type;
    }

    private boolean checkAny(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) return true;
        }
        return false;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }
}
