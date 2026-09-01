package balbucio.keycloak.kick;

import org.junit.jupiter.api.Test;
import org.keycloak.util.JsonSerialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class KickUserProfileTest {

    @Test
    void shouldDeserializeKickUsersPayload() throws Exception {
        String payload = "{"
                + "\"data\":[{"
                + "\"user_id\":12345,"
                + "\"name\":\"kickuser\","
                + "\"email\":\"kickuser@example.com\","
                + "\"profile_picture\":\"https://kick.com/avatar.png\""
                + "}],"
                + "\"message\":\"OK\""
                + "}";

        KickUsersResponse response = JsonSerialization.readValue(payload, KickUsersResponse.class);

        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
        assertEquals("OK", response.getMessage());

        KickUserProfile profile = response.getData().get(0);
        assertEquals(12345L, profile.getUserId());
        assertEquals("kickuser", profile.getName());
        assertEquals("kickuser@example.com", profile.getEmail());
        assertEquals("https://kick.com/avatar.png", profile.getProfilePicture());
    }

    @Test
    void shouldHandleEmptyDataArray() throws Exception {
        String payload = "{\"data\":[],\"message\":\"OK\"}";

        KickUsersResponse response = JsonSerialization.readValue(payload, KickUsersResponse.class);

        assertNotNull(response.getData());
        assertEquals(0, response.getData().size());
    }

    @Test
    void shouldHandleMissingEmailWhenScopeAbsent() throws Exception {
        String payload = "{\"data\":[{\"user_id\":1,\"name\":\"user\"}],\"message\":\"OK\"}";

        KickUsersResponse response = JsonSerialization.readValue(payload, KickUsersResponse.class);

        KickUserProfile profile = response.getData().get(0);
        assertEquals(1L, profile.getUserId());
        assertEquals("user", profile.getName());
        assertNull(profile.getEmail());
        assertNull(profile.getProfilePicture());
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        String payload = "{\"data\":[{\"user_id\":1,\"name\":\"user\",\"unexpected_field\":\"value\",\"nested\":{\"a\":1}}],\"message\":\"OK\"}";

        KickUsersResponse response = JsonSerialization.readValue(payload, KickUsersResponse.class);

        assertEquals(1L, response.getData().get(0).getUserId());
    }

    @Test
    void shouldHandleProfilePictureMapping() throws Exception {
        String payload = "{\"data\":[{\"user_id\":999,\"name\":\"streamer\",\"email\":\"s@kick.com\",\"profile_picture\":\"https://files.kick.com/images/user/999/avatar.jpg\"}],\"message\":\"OK\"}";

        KickUsersResponse response = JsonSerialization.readValue(payload, KickUsersResponse.class);
        KickUserProfile profile = response.getData().get(0);

        assertEquals("https://files.kick.com/images/user/999/avatar.jpg", profile.getProfilePicture());
    }
}
