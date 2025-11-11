package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.github.LoginGitHubService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/github")
public class LoginGitHubController {

    private final LoginGitHubService loginGitHubService;

    public LoginGitHubController(LoginGitHubService loginGitHubService) {
        this.loginGitHubService = loginGitHubService;
    }

    @GetMapping
    public ResponseEntity<Void> redirecionarGitHub() {
        String url = loginGitHubService.gerarUrl();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(url));

        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<String> obterToken(@RequestParam String code) {
        String token = loginGitHubService.obterEmail(code);
        return ResponseEntity.ok(token);
    }

}
