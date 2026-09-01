package balbucio.keycloak.kick;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KickUsersResponse {

    private List<KickUserProfile> data;

    private String message;
}
