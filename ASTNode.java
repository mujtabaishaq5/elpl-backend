package com.syed.elpl_backend;

import java.util.*;

// Base class for all AST nodes
public abstract class ASTNode {}

// Root of the program
class Program extends ASTNode {
    List<ASTNode> statements;
    Program(List<ASTNode> statements) {
        this.statements = statements;
    }
}

// let x be 5
class AssignmentNode extends ASTNode {
    String identifier;
    ExpressionNode expression;
    boolean isFloat;
    AssignmentNode(String identifier, ExpressionNode expression, boolean isFloat) {
        this.identifier = identifier;
        this.expression = expression;
        this.isFloat = isFloat;
    }
}

// print "Hello" x
class PrintNode extends ASTNode {
    List<ExpressionNode> expressions;
    PrintNode(List<ExpressionNode> expressions) {
        this.expressions = expressions;
    }
}

// "Hello world"
class StringLiteral extends ExpressionNode {
    public final String value;
    StringLiteral(String value) {
        this.value = value;
    }
}
// block handling
class BlockNode extends ASTNode{
    List<ASTNode>  statements;
    BlockNode(List<ASTNode> statements){
        this.statements = statements;
    }
}

// if x is greater than y then { ... } otherwise { ... }
class IfNode extends ASTNode {
    Condition condition;
    BlockNode thenBlock;
    BlockNode elseBlock;
    IfNode(Condition condition,  BlockNode thenBlock,  BlockNode elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
}
// stop for break statement
class StopNode extends ASTNode{}
// Array implementation Array nums be [1, 2, 3]
class ArrayDecNode extends ASTNode{
    public final String name;
    public final List<ExpressionNode> elements;

    public ArrayDecNode(String name, List<ExpressionNode> elements){
        this.name = name;
        this.elements = elements;

    }
}
// Array index
class ArrayAccessNode extends ExpressionNode {
    public String arrayName;
    public ExpressionNode index;

    public ArrayAccessNode(String arrayName, ExpressionNode index) {
        this.arrayName = arrayName;
        this.index = index;
        
    }
}
class ArrayAssignNode extends ASTNode {
    public final String arrayName;
    public final ExpressionNode index;
    public final ExpressionNode value;
    

    public ArrayAssignNode(String arrayName, ExpressionNode index, ExpressionNode value) {
        this.arrayName = arrayName;
        this.index = index;
        this.value = value;
    }
}


// for loop for i be 0 to 10 {print i}
class ForNode extends ASTNode {
    public final String iterator;
    public final ExpressionNode start;
    public final ExpressionNode end;
    public final List<ASTNode> body;

    public ForNode(String iterator, ExpressionNode start, ExpressionNode end, List<ASTNode> body) {
        this.iterator = iterator;
        this.start = start;
        this.end = end;
        this.body = body;
    }
}

// repeat 5 times { ... }
class RepeatNode extends ASTNode {
    int times;
    List<ASTNode> body;
    RepeatNode(int times, List<ASTNode> body) {
        this.times = times;
        this.body = body;
    }
}

// while x is less than 10 { ... }
class WhileNode extends ASTNode {
    Condition condition;
    List<ASTNode> body;
    WhileNode(Condition condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }
}

// function greet { print "hi" }
class FunctionDeclNode extends ASTNode {
   public final String name;
   public final List<String> parameters;
    public final List<ASTNode> body;
    FunctionDeclNode(String name, List<String> parameters, List<ASTNode> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}
// Return statement
class ReturnNode extends ASTNode{
    public final ExpressionNode value;

    public ReturnNode(ExpressionNode value){
        this.value = value;
    }
}

// call greet
class FunctionCallNode extends ExpressionNode {
    public final String name;
    public final List<ExpressionNode> arguments;
    public FunctionCallNode(String name, List<ExpressionNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}

// 5 + 3, x * 4
class BinaryExpr extends ExpressionNode {
    ExpressionNode left;
    String op;
    ExpressionNode right;
    BinaryExpr(ExpressionNode left, String op, ExpressionNode right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }
}

// Boolean support
class BooleanLiteral extends ExpressionNode {
    boolean value;
    BooleanLiteral(boolean value) {
        this.value = value;
    }
}
// 'Not' support
class UnaryExpr extends ExpressionNode {
    String op;
    ExpressionNode expr;

    public UnaryExpr(String op, ExpressionNode expr) {
        this.op = op;
        this.expr = expr;
    }
}


// 42
class NumberLiteral extends ExpressionNode {
    public final double value;
    public NumberLiteral(double value) {
        this.value = value;
    }
}

// x
class VariableRef extends ExpressionNode {
    String name;
    VariableRef(String name) {
        this.name = name;
    }
}

// x is greater than y
class Condition extends ASTNode {
    public final ExpressionNode left;
    public final ExpressionNode right;
    public final String comparator;

    public Condition(ExpressionNode left, String comp, ExpressionNode right) {
        this.left = left;
        this.comparator = comp;
        this.right = right;
    }
    public Condition(ExpressionNode booleanExpr){
        this.left = booleanExpr;
        this.comparator = null;
        this.right = null;
    }
}

// Expression base class
abstract class ExpressionNode extends ASTNode {}
