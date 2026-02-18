package com.sunbox.sdpadmin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/v2")
public class AdminIndexController {

    @RequestMapping
    public String index(Model model) {

        return "/login";
    }
}
