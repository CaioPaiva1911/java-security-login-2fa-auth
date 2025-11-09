package br.com.forum_hub.domain.autenticacao;

import org.springframework.stereotype.Service;

@Service
public class RedirecionamentoService {

    private static final String CLIENT_ID_FACEBOOK = "";
    private static final String REDIRECT_URI_FACEBOOK = "";
    private static final String CLIENT_ID_MICROSOFT = "";
    private static final String REDIRECT_URI_MICROSOFT = "";

    public String gerarUrlFacebook() {
        return "https://www.facebook.com/v17.0/dialog/oauth"
                + "?client_id=" + CLIENT_ID_FACEBOOK
                + "&redirect_uri=" + REDIRECT_URI_FACEBOOK
                + "&scope=email,public_profile";

    }


    public String gerarUrlMicrosoft() {
        return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                + "?client_id=" + CLIENT_ID_MICROSOFT
                + "&redirect_uri=" + REDIRECT_URI_MICROSOFT
                + "&response_type=code"
                + "&scope=openid email profile";

    }
}
