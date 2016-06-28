package com.inauth.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by gavnunns on 6/23/16.
 */
@Controller
public class IndexController {
    @RequestMapping("/")
    String index() {
        return "index";
    }
}
