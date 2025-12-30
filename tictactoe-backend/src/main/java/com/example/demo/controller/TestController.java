package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/secure")
    public String secureEndpoint() {
        return "✅ Token hợp lệ! Bạn đã truy cập được API bảo vệ.";
    }
}
