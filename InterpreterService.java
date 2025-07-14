package com.syed.elpl_backend;

import java.util.List;
import org.springframework.stereotype.Service; 

@Service
public class InterpreterService {

    public String run(String code) {
        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            Interpreter interpreter = new Interpreter();
            return interpreter.interpret(ast); // Now returns output string
        } catch (Exception e) {
            return "Compiler Error: " + e.getMessage();
        }
    }
}
