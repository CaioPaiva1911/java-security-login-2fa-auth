package br.com.forum_hub.controller;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.autenticacao.github.LoginGitHubService;
import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/github")
public class LoginGitHubController {

    private final LoginGitHubService loginGitHubService;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public LoginGitHubController(LoginGitHubService loginGitHubService, TokenService tokenService, UsuarioService usuarioService) {
        this.loginGitHubService = loginGitHubService;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<Void> redirecionarGitHub() {
        String url = loginGitHubService.gerarUrl();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(url));

        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @GetMapping("/autorizado")
    public ResponseEntity<DadosToken> autenticarUsuarioOAuth(@RequestParam String code) {
        String email = loginGitHubService.obterEmail(code);

        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, false));
    }

    @GetMapping("/registro")
    public ResponseEntity<Void> redirecionarRegistroGithub() {
        String url = loginGitHubService.gerarUrlRegistro();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(url));

        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @GetMapping("/registro-autorizado")
    public ResponseEntity<DadosToken> registrarOAuth(@RequestParam String code) {
        DadosCadastroUsuario dadosUsuario = loginGitHubService.obterDadosOAuth(code);

        Usuario usuario = usuarioService.cadastrarVerificado(dadosUsuario);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken, false));

    }

}
