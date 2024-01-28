package kuku.OS.model;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseModel<T> {
    private String msg;
    private T data;

    public static <TT> String jsonResponseModel(String msg, TT data) {
        var model = new ResponseModel<TT>(msg, data);
        return new Gson().toJson(model);
    }
}
