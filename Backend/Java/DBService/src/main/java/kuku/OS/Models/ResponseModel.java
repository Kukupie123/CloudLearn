package kuku.OS.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseModel<T> {
    private String msg;
    private T data;
}
