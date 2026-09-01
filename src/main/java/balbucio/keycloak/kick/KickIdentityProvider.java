package balbucio.keycloak.kick;

import lombok.extern.jbosslog.JBossLog;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@JBossLog
public class KickIdentityProvider extends AbstractOAuth2IdentityProvider<KickIdentityProviderConfig>
        implements SocialIdentityProvider<KickIdentityProviderConfig> {

    public KickIdentityProvider(KeycloakSession session, KickIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    protected String getDefaultScopes() {
        return KickIdentityProviderConfig.DEFAULT_SCOPE;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        KickUserProfile profile = fetchUserProfile(accessToken);

        if (profile == null || profile.getUserId() == null) {
            throw new IdentityBrokerException("Resposta da Kick API nao inclui o identificador do usuario.");
        }

        String userId = String.valueOf(profile.getUserId());

        BrokeredIdentityContext context = new BrokeredIdentityContext(userId, getConfig());
        context.setBrokerUserId(userId);
        // Kick API retorna apenas 'name' como username; fallback para userId se blank
        String username = isBlank(profile.getName()) ? userId : profile.getName();
        context.setUsername(username);
        context.setModelUsername(username);
        context.setEmail(profile.getEmail());
        context.setIdp(this);

        context.setUserAttribute("kick.user_id", userId);
        context.setUserAttribute("kick.name", nullToEmpty(profile.getName()));
        context.setUserAttribute("kick.email", nullToEmpty(profile.getEmail()));

        if (!isBlank(profile.getProfilePicture())) {
            context.setUserAttribute("kick.profile_picture", profile.getProfilePicture());
            context.setUserAttribute("picture", profile.getProfilePicture());
        }

        return context;
    }

    private KickUserProfile fetchUserProfile(String accessToken) {
        try {
            String body = fetchJson(getConfig().getUserInfoUrl(), accessToken);
            KickUsersResponse response = JsonSerialization.readValue(body, KickUsersResponse.class);
            List<KickUserProfile> data = response.getData();
            return (data == null || data.isEmpty()) ? null : data.get(0);
        } catch (IOException e) {
            throw new IdentityBrokerException("Nao foi possivel obter o perfil do usuario na Kick API.", e);
        }
    }

    protected String fetchJson(String url, String accessToken) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.addHeader(HttpHeaders.ACCEPT, "application/json");

        try (CloseableHttpResponse response = session.getProvider(HttpClientProvider.class)
                .getHttpClient()
                .execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            String payload = response.getEntity() == null
                    ? ""
                    : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (status != HttpStatus.SC_OK) {
                throw new IdentityBrokerException("Kick userinfo request failed with status " + status + ": " + payload);
            }
            return payload;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
