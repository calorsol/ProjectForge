package com.example.ppm.common;

public class R<T> {
    private final int code;
    private final String message;
    private final T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok(T data) { return new R<>(0, "ok", data); }
    public static R<Void> ok() { return new R<>(0, "ok", null); }
    public static <T> R<T> fail(int code, String message) { return new R<>(code, message, null); }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
