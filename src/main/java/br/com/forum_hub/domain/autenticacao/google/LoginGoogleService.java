package br.com.forum_hub.domain.autenticacao.google;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginGoogleService {

    private final RestClient restClient = RestClient.builder().build();

    @Value("${google.oauth.client.id:}")
    private String clientId;

    @Value("${google.oauth.client.secret:}")
    private String clientSecret;

    private final String redirectUri = "http://localhost:8080/login/google/autorizado";
    private final String redirectUriRegistro = "http://localhost:8080/login/google/registro-autorizado";

    public String gerarUrl() {
        return "https://accounts.google.com/o/oauth2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "&response_type=code" +
                "&access_type=offline";
    }

    private Map<?, ?> obterToken(String code, String id, String uri) {
        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code,
                        "client_id", id,
                        "client_secret", clientSecret,
                        "redirect_uri", uri,
                        "grant_type", "authorization_code"))
                .retrieve()
                .body(Map.class);
    }

    public String obterEmail(String code) {
        Map<?, ?> tokens = obterToken(code, clientId, redirectUri);

        String token = tokens.get("id_token").toString();

        String refreshToken = tokens.get("refresh_token").toString();
        if (refreshToken != null) {
            System.out.println("Refresh token: " + refreshToken);
        }

        DecodedJWT decodedJWT = JWT.decode(token);
        System.out.println(decodedJWT.getClaims());

        return decodedJWT.getClaim("email").toString();
    }


    public String renovarAccessToken(String refreshToken) {
        Map<?, ?> resposta = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "refresh_token", refreshToken,
                        "grant_type", "refresh_token"
                ))
                .retrieve()
                .body(Map.class);
        return Optional.ofNullable(resposta).orElseThrow().get("access_token").toString();
    }

    public String gerarUrlRegistro() {
        return "https://accounts.google.com/o/oauth2/v2/auth"+
                "?client_id="+clientId +
                "&redirect_uri="+redirectUriRegistro +
                "&scope=https://www.googleapis.com/auth/userinfo.email" +
                "%20https://www.googleapis.com/auth/userinfo.profile" +
                "&response_type=code";

    }

    public DadosCadastroUsuario obterDadosOAuth(String code) {

        Map<?, ?> tokens = obterToken(code, clientId, redirectUri);
        String token = tokens.get("access_token").toString();

        System.out.println("Token: " + token);

        DecodedJWT decodedJWT = JWT.decode(token);
        String email = decodedJWT.getClaim("email").asString();
        String senha = UUID.randomUUID().toString();
        String nomeCompleto = decodedJWT.getClaim("name").asString();
        String nomeUsuario = email.split("@")[0];

        return new DadosCadastroUsuario(email, senha, nomeCompleto, nomeUsuario, null, null);
    }
}