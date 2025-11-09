package br.com.forum_hub.domain.autenticacao.github;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class LoginGitHubService {

    private final String redirectUri = "http://localhost:8080/login/github/autorizado";
    private final RestClient restClient;

    @Value("github.client_id")
    private String clientId;

    @Value("github.client-secret")
    private String clientSecret;


    public LoginGitHubService(RestClient.Builder restClient) {
        this.restClient = restClient.build();
    }


    public String gerarUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=read:user,user:email";
    }

    public String obterToken(String code) {
        return restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code,
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "redirect_uri", redirectUri))
                .retrieve()
                .body(String.class);
    }
}
