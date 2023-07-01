package com.example.intento1;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {
    @GetMapping("/inicio")
    public String vistaprinci(){
        return "inicio";
    }
    //hacerlo solo con href
    @GetMapping("/mensajeria")
    public String mensajeria(Model model, @RequestParam("username") String username){
        model.addAttribute("nombre",username);
        return "index";
    }
}
