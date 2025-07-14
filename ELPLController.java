package com.syed.elpl_backend;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ELPLController {
    
    private final InterpreterService interpreterService;

    @Autowired
    public ELPLController(InterpreterService interpreterService) {
        this.interpreterService = interpreterService;
    }

    @PostMapping("/run")
    public String run(@RequestBody Map<String, String> payload) {
        String code = payload.get("program");
        if (code == null) {
            return "Error: no 'program' field in request";
        }
        return interpreterService.run(code);
    }
}
