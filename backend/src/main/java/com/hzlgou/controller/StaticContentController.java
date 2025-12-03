package com.hzlgou.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StaticContentController {
    
    @RequestMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
