package kuku.OS.model;

import com.google.gson.Gson;
import lombok.Getter;

@Getter
public class ResponseModel<T> {
    private String msg;
    private T data;

    private ResponseModel(String msg, T data) {
        this.data = data;
        this.msg = msg;
    }

    public static <TT> String jsonResponseModel(String msg, TT data) {
        var model = new ResponseModel<TT>(msg, data);
        return new Gson().toJson(model);
    }
}
