package com.syed.elpl_backend;

import java.util.*;


public class Interpreter {
    private boolean insideFunction = false;
    private int recursionDepth = 0;
     private static final int MAX_RECURSION_DEPTH = 10000; // or tune as needed


    public Interpreter(){
        variableStack.push(new HashMap<>());
    }

    // Variable storage
    private final Deque<Map<String, Object>> variableStack = new ArrayDeque<>();

    // Function definitions
    private final Map<String, FunctionDeclNode> functions = new HashMap<>();

    private final StringBuilder output = new StringBuilder();

    public static class StopException extends RuntimeException{
        public StopException(){
            super();
        }
    }

    public static class ReturnException extends RuntimeException{
        public final Object value;
        public ReturnException(Object value){
            this.value = value;
        }
    }

    // Entry point for interpreting any AST node
public String interpret(ASTNode node) {
       if (node == null){
           output.append("Error: cannot compile null node.\n");
           return output.toString();
       }
    try {
        if (node instanceof Program) {
            for (ASTNode stmt : ((Program) node).statements) {
                interpret(stmt);
            }

        } else if (node instanceof BlockNode block){
            for(ASTNode stmt : block.statements){
                interpret(stmt);
            }

        }
         else if (node instanceof AssignmentNode) {
            AssignmentNode assign = (AssignmentNode) node;
            Object value = evaluate(assign.expression);
            if (!assign.isFloat && value instanceof Double){
                double val = (Double) value;
                if(val != Math.floor(val)){
                    throw new RuntimeException("Cannot assign non-integer to 'let' variable: " + val);
                }
                value = (int) val;
            }
            
                currentScope().put(assign.identifier, value);
            
            
           

        } else if (node instanceof PrintNode) {
          PrintNode print = (PrintNode) node;

        for (int i = 0; i < print.expressions.size(); i++) {
        ExpressionNode expr = print.expressions.get(i);
        if (expr instanceof StringLiteral) {
            output.append(((StringLiteral) expr).value);
        } else {
            Object val = evaluate(expr);

            if (val instanceof List) {
            List<?> list = (List<?>) val;
             output.append("[");
             for (int j = 0; j < list.size(); j++) {
            Object item = list.get(j);
             if (item instanceof Double && ((Double) item) % 1 == 0) {
               output.append(((Double) item).intValue());
        } else {
            output.append(item);
        }
        if (j < list.size() - 1) {
            output.append(", ");
        }
    }
    output.append("]");
} else if (val instanceof Double && ((Double) val) % 1 == 0) {
    output.append(((Double) val).intValue());
} else {
    output.append(val);
}
        }

        // Only append space if it's not the last item
        if (i < print.expressions.size() - 1) {
            output.append(" ");
        }
    }

    output.append("\n"); // ✅ Final newline after entire print statement
}
 else if(node instanceof ReturnNode returnNode){
            Object returnValue = evaluate(returnNode.value);
            System.out.println("Returning from function with: " + returnValue);
            throw new ReturnException(returnValue);
        }
        
         else if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            boolean result = evaluateCondition(ifNode.condition);
            if(result){
                interpret(ifNode.thenBlock);
            } else if(ifNode.elseBlock != null) {
                interpret(ifNode.elseBlock);
            } 

        }  else if (node instanceof RepeatNode) {
    RepeatNode repeat = (RepeatNode) node;
    for (int i = 0; i < repeat.times; i++) {
        try {
            for (ASTNode stmt : repeat.body) {
                interpret(stmt);
            }
        } catch (StopException e) {
            break;
        }
    }
}

    else if (node instanceof WhileNode) {
    WhileNode whileNode = (WhileNode) node;
    while (evaluateCondition(whileNode.condition)) {
        try {
            for (ASTNode stmt : whileNode.body) {
                interpret(stmt);
            }
        } catch (StopException e) {
            break;
        }
    }
}
else if (node instanceof ArrayDecNode){
            ArrayDecNode arrayDec = (ArrayDecNode) node;
            List<Object> values = new ArrayList<>();
            for(ExpressionNode e : arrayDec.elements){
                values.add(evaluate(e));
            }
            currentScope().put(arrayDec.name, values);
        }
         else if(node instanceof StopNode){
             System.out.println("StopNode encountered, throwing StopException");
            throw new StopException();
        } 
        else if (node instanceof ForNode) {
    ForNode forNode = (ForNode) node;
    int start = (int) toDouble(evaluate(forNode.start));
    int end = (int) toDouble(evaluate(forNode.end));

    if (start <= end) {
    for (int i = start; i <= end; i++) {
        currentScope().put(forNode.iterator, i);
        try {
            for (ASTNode stmt : forNode.body) {
                interpret(stmt);
            }
        } catch (StopException e) {
            break;
        }
    }
} else {
    for (int i = start; i >= end; i--) {
        currentScope().put(forNode.iterator, i);
        try {
            for (ASTNode stmt : forNode.body) {
                interpret(stmt);
            }
        } catch (StopException e) {
            break;
        }
    }
   }
}

else if (node instanceof ArrayAssignNode assign) {
    Object val = currentScope().get(assign.arrayName);
    if (!(val instanceof List)) {
        throw new RuntimeException("Variable '" + assign.arrayName + "' is not an array");
    }

    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>) val;

    int i = (int) toDouble(evaluate(assign.index));
    Object value = evaluate(assign.value);

    if (i < 0 || i > list.size()) {
        throw new RuntimeException("Index " + i + " out of bounds for length " + list.size());
    }

    if (i == list.size()) {
        // Append at the end
        list.add(value);
    } else {
        // Replace existing
        list.set(i, value);
    } if (i > list.size()){
        throw new RuntimeException("Cannot assign to index" + i + "- only upto " + list.size());
    }
}






       

else if (node instanceof FunctionDeclNode) {
            FunctionDeclNode func = (FunctionDeclNode) node;
            functions.put(func.name, func); // Store function body

        } else if (node instanceof FunctionCallNode call) {
            Object result = evaluate(call);
            if(result != null){
                if(result instanceof Double && ((Double) result) % 1 == 0){
                    output.append(((Double) result).intValue());
                } else {
                    output.append(result);
                }
                output.append("\n");
            }
            
          } else {
            throw new RuntimeException("Unsupported AST node: " + node.getClass().getSimpleName());
        }
        
    } catch (StopException e) {
        System.out.println("Program stopped early via 'stop'");
        throw e;
        
    } catch (RuntimeException e){
        if(e instanceof ReturnException){
            throw e;
        }
        output.append("Runtime Error: ")
        .append((e.getMessage() != null) ? e.getMessage() : "Unknown error")
        .append("\n");
        
    }

    return output.toString();
}
public String run(ASTNode node){
    try{
        interpret(node);
    } catch( StopException e){
        System.out.println("Execution halted by 'stop'");
    } catch(ReturnException e){
        if(!insideFunction)
        output.append("Runtime Error: 'return' used outside of a function.\n");
    }
    return output.toString();
}
private Map<String, Object> currentScope(){
    return variableStack.peek();
}


    // Evaluates expressions like numbers, variables, arithmetic
     private Object evaluate(ExpressionNode expr) {
        if (expr == null){
            throw new RuntimeException("Null Expression encountered");
        }
    if (expr instanceof NumberLiteral) {
        return ((NumberLiteral) expr).value;

    } else if (expr instanceof BooleanLiteral) {
        return ((BooleanLiteral) expr).value;

    } else if (expr instanceof VariableRef) {
        String name = ((VariableRef) expr).name;
        if (!currentScope().containsKey(name)) {
            throw new RuntimeException("Undefined variable: " + name);
        }
        return currentScope().get(name);

    } else if (expr instanceof BinaryExpr) {
        BinaryExpr bin = (BinaryExpr) expr;
        Object left = evaluate(bin.left);
        Object right = evaluate(bin.right);

        switch (bin.op) {
            case "add":
                return toDouble(left) +  toDouble(right);
            case "subtract":
                return toDouble(left) - toDouble(right);
            case "multiply":
                return toDouble(left) * toDouble(right);
            case "divide":
                double rVal = toDouble(right);
                if (rVal == 0.0) throw new RuntimeException("Division by zero");
                return toDouble(left) / rVal;
            case "mod":
                double modRight = toDouble(right);
                if(modRight == 0.0) throw new RuntimeException("Illegal modulo use: modulo by zero");
                return toDouble(left) % modRight;

            case "and":
              if (!(left instanceof Boolean) || !(right instanceof Boolean))
             throw new RuntimeException("'and' requires boolean operands");
             return (boolean) left && (boolean) evaluate(bin.right);

            case "or":
                return (boolean) left || (boolean) evaluate(bin.right);
            case "is equal to":
              if (left instanceof Number && right instanceof Number) {
                 return toDouble(left) == toDouble(right);
                 }
                
                return left.equals(right);
            
            case "is greater than":
                return toDouble(left) > toDouble(right);
            case "is less than":
                return toDouble(left) < toDouble(right);
            case "is greater than or equal to":
               return toDouble(left) >= toDouble(right);
            case "is less than or equal to":
               return toDouble(left) <= toDouble(right);
            case "is not equal to":   
            case "not equal to":
                 if (left == null || right == null) {
                      return left != right; // null-safe inequality
                 }
                   return !left.equals(right);   

            default:
                throw new RuntimeException("Unsupported operator: " + bin.op);
        }

    } 
      else if (expr instanceof ArrayAccessNode access) {
        Object arrayVal = currentScope().get(access.arrayName);
        if (!(arrayVal instanceof List<?> list)) {
            throw new RuntimeException("Variable '" + access.arrayName + "' is not an array");
        }

        Object indexVal = evaluate(access.index);
        if (!(indexVal instanceof Number)) {
            throw new RuntimeException("Array index must be a number");
        }

        int idx = ((Number) indexVal).intValue();
        if (idx < 0 || idx >= list.size()) {
            throw new RuntimeException("Array index out of bounds");
        }

        return list.get(idx);
    }

   else if (expr instanceof FunctionCallNode callExpr) {
    String name = callExpr.name;

    if (isBuiltInFunction(name)) {
        return evaluateBuiltIn(name, callExpr.arguments);
    }

    FunctionDeclNode func = functions.get(name);
    if (func == null) {
        throw new RuntimeException("Undefined function: " + name);
    }

    if (callExpr.arguments.size() != func.parameters.size()) {
        throw new RuntimeException("Function '" + name + "' expects " +
                func.parameters.size() + " arguments, got " + callExpr.arguments.size());
    }

    // ✅ Step 1: Evaluate arguments BEFORE increasing depth
    List<Object> argValues = new ArrayList<>();
    for (ExpressionNode arg : callExpr.arguments) {
        argValues.add(evaluate(arg));
    }

    // ✅ Step 2: Increase recursionDepth only when we’re actually entering
    if (++recursionDepth > MAX_RECURSION_DEPTH) {
        throw new RuntimeException("Maximum recursion depth exceeded");
    }

    // ✅ Step 3: Prepare and push scope
    Map<String, Object> localScope = new HashMap<>(currentScope());
    variableStack.push(localScope);

    for (int i = 0; i < func.parameters.size(); i++) {
        currentScope().put(func.parameters.get(i), argValues.get(i));
    }

    insideFunction = true;
    Object returnValue = null;

    try {
        for (ASTNode stmt : func.body) {
            interpret(stmt);
        }
    } catch (ReturnException e) {
        returnValue = e.value;
    } finally {
        insideFunction = false;
        variableStack.pop();
        recursionDepth--;
    }

    return returnValue;
}

else if (expr instanceof StringLiteral) {
        return ((StringLiteral) expr).value;

    } // unary expression here 
    else if (expr instanceof UnaryExpr) {
    UnaryExpr unary = (UnaryExpr) expr;
    Object value = evaluate(unary.expr);

    return switch (unary.op) {
        case "not" -> {
            if (!(value instanceof Boolean)) {
                throw new RuntimeException("'not' can only be applied to booleans");
            }
            yield !(Boolean) value;
        }
        case "negate" -> {
            if (!(value instanceof Double)) {
                throw new RuntimeException("Unary '-' can only be applied to numbers");
            }
            yield -(Double) value;
        }
        default -> throw new RuntimeException("Unsupported unary operator: " + unary.op);
    };
}
   throw new RuntimeException("Unknown expression node: " + expr.getClass().getSimpleName());
}
private boolean isBuiltInFunction(String name) {
    return Set.of("length", "sum", "max", "min", "sqrt", "abs", "pow", "floor", "ceil", "reverse").contains(name);
}

private Object evaluateBuiltIn(String name, List<ExpressionNode> args) {
    switch (name) {
        case "length" -> {
            if (args.size() != 1) throw new RuntimeException("length() takes 1 argument");
            Object val = evaluate(args.get(0));
            if (!(val instanceof List<?> list)) throw new RuntimeException("length() expects an array");
            return (double) list.size();
        }
        case "sum" -> {
            if (args.size() != 1) throw new RuntimeException("sum() takes 1 array");
            Object val = evaluate(args.get(0));
            if (!(val instanceof List<?> list)) throw new RuntimeException("sum() expects an array");
            double sum = 0;
            for (Object o : list) {
                if (o instanceof Double d) sum += d;
                else throw new RuntimeException("sum() supports numeric arrays only");
            }
            return sum;
        }
        case "max" -> {
            if (args.size() != 1) throw new RuntimeException("max() takes 1 array");
            Object val = evaluate(args.get(0));
            if (!(val instanceof List<?> list)) throw new RuntimeException("max() expects an array");
            if (list.isEmpty()) throw new RuntimeException("max() on empty array");
            double max = Double.NEGATIVE_INFINITY;
            for (Object o : list) {
                if (o instanceof Double d) max = Math.max(max, d);
                else throw new RuntimeException("max() supports numeric arrays only");
            }
            return max;
        }
        case "min" -> {
            if (args.size() != 1) throw new RuntimeException("min() takes 1 array");
            Object val = evaluate(args.get(0));
            if (!(val instanceof List<?> list)) throw new RuntimeException("min() expects an array");
            if (list.isEmpty()) throw new RuntimeException("min() on empty array");
            double min = Double.POSITIVE_INFINITY;
            for (Object o : list) {
                if (o instanceof Double d) min = Math.min(min, d);
                else throw new RuntimeException("min() supports numeric arrays only");
            }
            return min;
        }
        case "sqrt" -> {
            if (args.size() != 1) throw new RuntimeException("sqrt() takes 1 number");
            Object val = evaluate(args.get(0));
            if (!(val instanceof Double d)) throw new RuntimeException("sqrt() expects a number");
            return Math.sqrt(d);
        }
        case "abs" -> {
            if (args.size() != 1) throw new RuntimeException("abs() takes 1 number");
            Object val = evaluate(args.get(0));
            if (!(val instanceof Double d)) throw new RuntimeException("abs() expects a number");
            return Math.abs(d);
        }
        case "pow" -> {
            if (args.size() != 2) throw new RuntimeException("pow() takes 2 numbers");
            Object base = evaluate(args.get(0));
            Object exp = evaluate(args.get(1));
            if (!(base instanceof Double b) || !(exp instanceof Double e))
                throw new RuntimeException("pow() expects numeric arguments");
            return Math.pow(b, e);
        }
        case "floor" -> {
            if (args.size() != 1) throw new RuntimeException("floor() takes 1 number");
            Object val = evaluate(args.get(0));
            if (!(val instanceof Double d)) throw new RuntimeException("floor() expects a number");
            return Math.floor(d);
        }
        case "ceil" -> {
            if (args.size() != 1) throw new RuntimeException("ceil() takes 1 number");
            Object val = evaluate(args.get(0));
            if (!(val instanceof Double d)) throw new RuntimeException("ceil() expects a number");
            return Math.ceil(d);
        } 
        case "reverse" -> {
             if (args.size() != 1) throw new RuntimeException("reverse() takes exactly 1 array");
             Object val = evaluate(args.get(0));
              if (!(val instanceof List)){ throw new RuntimeException("reverse() expects an array");}

              // In-place reverse
              @SuppressWarnings("unchecked")
              List<Object> list = (List<Object>) val;
             int left = 0;
             int right = list.size() - 1;
             while (left < right) {
              Object temp = list.get(left);
              list.set(left, list.get(right));
              list.set(right, temp);
              left++;
              right--;
    }

    return list; // Reverse returns nothing
}
        default -> throw new RuntimeException("Unknown built-in function: " + name);
    }
}



private double toDouble(Object value) {
    if (value instanceof Integer) return ((Integer) value).doubleValue();
    if (value instanceof Double) return (Double) value;
    throw new RuntimeException("Expected numeric value but got: " + value);
}



    // Evaluates conditions for if/while
    private boolean evaluateCondition(Condition cond) {
    // Handle pure boolean condition (no comparator)
    if (cond.comparator == null) {
        Object result = evaluate(cond.left);
        if (!(result instanceof Boolean)) {
            throw new RuntimeException("Expected boolean expression in condition");
        }
        return (Boolean) result;
    }

    // Handle comparison conditions
    Object left = evaluate(cond.left);
    Object right = evaluate(cond.right);

    System.out.println("Condition: " + left + " " + cond.comparator + " " + right);

    switch (cond.comparator) {
        case "is equal to":
            if (left instanceof Number && right instanceof Number) {
                return toDouble(left) == toDouble(right);
            }
            return Objects.equals(left, right);
        case "is greater than":
            return toDouble(left) > toDouble(right);
        case "is less than":
            return toDouble(left) < toDouble(right);
        case "is greater than or equal to":
         return toDouble(left) >= toDouble(right);
        case "is less than or equal to":
         return toDouble(left) <= toDouble(right);

        default:
            throw new RuntimeException("Unknown comparator: " + cond.comparator);
    }
}

}


