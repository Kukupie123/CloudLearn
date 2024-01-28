package kuku.OS.model.request;

import lombok.Getter;

import java.util.Map;

@Getter
public class GenerateTokenRequestModel extends BaseRequestModel {

    private final Map<String, String> claims;

    public GenerateTokenRequestModel(String action, Map<String, String> claims) {
        super(action);
        this.claims = claims;
    }
}
