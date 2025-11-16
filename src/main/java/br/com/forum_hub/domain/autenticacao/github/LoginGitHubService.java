package br.com.forum_hub.domain.autenticacao.github;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
public class LoginGitHubService {

    private final String redirectUri = "http://localhost:8080/login/github/autorizado";
    private final RestClient restClient;

    @Value("${github.client-id:}")
    private String clientId;

    @Value("${github.client-secret:}")
    private String clientSecret;

    public LoginGitHubService(RestClient.Builder restClient) {
        this.restClient = restClient.build();
    }

    public String gerarUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user:email,public_repo";
    }

    private String obterToken(String code, String id, String uri) {
        Map<?, ?> response = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code,
                        "client_id", id,
                        "client_secret", clientSecret,
                        "redirect_uri", uri))
                .retrieve()
                .body(Map.class);
        return response.get("access_token").toString();
    }

    public String obterEmail(String code) {
        String token = obterToken(code, clientId, redirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        String repositorios = restClient.get()
                .uri("https://api.github.com/user/repos")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        System.out.println(repositorios);

        String emailPrincipal = enviarRequisicaoEmail(headers);

        return emailPrincipal;
    }

    public String gerarUrlRegistro() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=read:user,user:email";
    }

    public DadosCadastroUsuario obterDadosOAuth(String code) {

        String accessToken = obterToken(code, clientId, redirectUri);
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String email = enviarRequisicaoEmail(headers);

        Map resposta = restClient.get()
                .uri("https://api.github.com/user")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);


        String nomeCompleto = resposta.get("name").toString();
        String nomeUsuario = resposta.get("login").toString();
        String senha = UUID.randomUUID().toString();

        return new DadosCadastroUsuario(email, senha, nomeCompleto, nomeUsuario, null, null);
    }

    private String enviarRequisicaoEmail(HttpHeaders headers) {

        DadosEmail[] resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);

        return Arrays.stream(resposta)
                .findFirst()
                .filter(d -> d.primary() && d.verified()).
                orElse(null).email();
    }
}
