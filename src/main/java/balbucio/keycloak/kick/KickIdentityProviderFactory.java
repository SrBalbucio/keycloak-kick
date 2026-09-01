package balbucio.keycloak.kick;

import com.google.auto.service.AutoService;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
@AutoService(SocialIdentityProviderFactory.class)
public class KickIdentityProviderFactory extends AbstractIdentityProviderFactory<KickIdentityProvider>
        implements SocialIdentityProviderFactory<KickIdentityProvider> {

    public static final String PROVIDER_ID = "kick";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        ProviderConfigProperty userInfoUrl = new ProviderConfigProperty();
        userInfoUrl.setName(KickIdentityProviderConfig.USER_INFO_URL_KEY);
        userInfoUrl.setLabel("User Info URL");
        userInfoUrl.setType(ProviderConfigProperty.STRING_TYPE);
        userInfoUrl.setDefaultValue(KickIdentityProviderConfig.DEFAULT_USER_INFO_URL);
        userInfoUrl.setHelpText("Endpoint da Kick API usado para carregar o perfil do usuario (https://api.kick.com/public/v1/users).");
        CONFIG_PROPERTIES.add(userInfoUrl);
    }

    @Override
    public String getName() {
        return "Kick";
    }

    @Override
    public KickIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        KickIdentityProviderConfig config = new KickIdentityProviderConfig(model);
        config.setAuthorizationUrl(KickIdentityProviderConfig.DEFAULT_AUTH_URL);
        config.setTokenUrl(KickIdentityProviderConfig.DEFAULT_TOKEN_URL);
        if (isBlank(config.getDefaultScope())) {
            config.setDefaultScope(KickIdentityProviderConfig.DEFAULT_SCOPE);
        }
        if (isBlank(config.getUserInfoUrl())) {
            config.setUserInfoUrl(KickIdentityProviderConfig.DEFAULT_USER_INFO_URL);
        }
        // Kick OAuth 2.1 exige PKCE S256 obrigatoriamente
        if (!config.isPkceEnabled()) {
            config.setPkceEnabled(true);
        }
        if (isBlank(config.getPkceMethod())) {
            config.setPkceMethod(OAuth2Constants.PKCE_METHOD_S256);
        }
        return new KickIdentityProvider(session, config);
    }

    @Override
    public KickIdentityProviderConfig createConfig() {
        return new KickIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
