package br.com.bibliotecaviva.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leitor")
public class LeitorController {

    @GetMapping("/painel")
    public Map<String, String> painel() {
        return Map.of("mensagem", "Acesso de leitor autorizado");
    }
}
