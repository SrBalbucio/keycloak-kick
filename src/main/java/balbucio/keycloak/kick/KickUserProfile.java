package balbucio.keycloak.kick;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KickUserProfile {

    @JsonProperty("user_id")
    private Long userId;

    private String name;

    private String email;

    @JsonProperty("profile_picture")
    private String profilePicture;
}
