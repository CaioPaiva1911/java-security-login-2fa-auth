package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.RedirecionamentoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirecionamentosController {

    private final RedirecionamentoService redirecionamentoService;

    public RedirecionamentosController(RedirecionamentoService redirecionamentoService) {
        this.redirecionamentoService = redirecionamentoService;
    }

    @GetMapping("/login/facebook")
    public ResponseEntity<Void> redirectToFacebook() {
        String url = redirecionamentoService.gerarUrlFacebook();

        HttpHeaders headers = new HttpHeaders();

        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
    @GetMapping("/login/microsoft")
    public ResponseEntity<Void> redirectToMicrosoft() {
        String url = redirecionamentoService.gerarUrlMicrosoft();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(url));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}
