package kuku.OS.model.request;

import lombok.Getter;

@Getter
public class ValidateTokenRequestModel extends BaseRequestModel {
    private String token;

    public ValidateTokenRequestModel(String action, String token) {
        super(action);
        this.token = token;
    }
}
