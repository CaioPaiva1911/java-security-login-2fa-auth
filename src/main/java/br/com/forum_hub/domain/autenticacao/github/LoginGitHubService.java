package br.com.forum_hub.domain.autenticacao.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.print.DocFlavor;
import java.util.Arrays;
import java.util.Map;

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

    private String obterToken(String code) {
        Map<?, ?> response = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri))
                .retrieve()
                .body(Map.class);
        return response.get("access_token").toString();
    }

    public String obterEmail(String code) {
        String token = obterToken(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        DadosEmail[] resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);

        String repositorios = restClient.get()
                .uri("https://api.github.com/user/repos")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        System.out.println(repositorios);

        DadosEmail emailPrincipal = Arrays.stream(resposta)
                .findFirst()
                .filter(d -> d.primary() && d.verified()).
                orElseThrow(() -> new RuntimeException("Nenhum e-mail principal verificado encontrado."));

        return emailPrincipal.email();
    }
}
