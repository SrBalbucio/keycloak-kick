package balbucio.keycloak.kick;

import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class KickIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public static final String DEFAULT_AUTH_URL = "https://id.kick.com/oauth/authorize";
    public static final String DEFAULT_TOKEN_URL = "https://id.kick.com/oauth/token";
    public static final String DEFAULT_USER_INFO_URL = "https://api.kick.com/public/v1/users";
    public static final String DEFAULT_SCOPE = "user:read";
    public static final String USER_INFO_URL_KEY = "userInfoUrl";

    public KickIdentityProviderConfig() {
        setAuthorizationUrl(DEFAULT_AUTH_URL);
        setTokenUrl(DEFAULT_TOKEN_URL);
        setUserInfoUrl(DEFAULT_USER_INFO_URL);
        setDefaultScope(DEFAULT_SCOPE);
        setStoreToken(true);
        setPkceEnabled(true);
        setPkceMethod(OAuth2Constants.PKCE_METHOD_S256);
    }

    public KickIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getUserInfoUrl() {
        return getConfig().getOrDefault(USER_INFO_URL_KEY, DEFAULT_USER_INFO_URL);
    }

    public void setUserInfoUrl(String userInfoUrl) {
        getConfig().put(USER_INFO_URL_KEY, userInfoUrl);
    }
}
