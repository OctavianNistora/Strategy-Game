package com.example.strategygame_api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StrategyGameController {
    @GetMapping("/test")
    public String test() {
        return "Test";
    }
}
