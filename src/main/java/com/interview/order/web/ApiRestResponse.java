package com.interview.order.web;

public class ApiRestResponse<T> {

    private Integer code;

    private String msg;

    private T data;

    public static final int OK_CODE = 10000;
    public static final String OK_MSG = "Success";
    public static final int NOT_FOUND_CODE = 40004;
    public static final String NOT_FOUND_MSG = "NOT FOUND";
    public static final int ERROR_CODE= 50000;
    public static final String ERROR_MSG = "Something Wrong";

    // Specific to update endpoint
    public static final int NOT_ALLOWED_CODE = 20001;
    public static final String NOT_ALLOWED_MSG = "NOT ALLOWED";
    public static final String UPDATE_FAILED_MSG = "Update failed";


    public ApiRestResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiRestResponse() {
        this(OK_CODE, OK_MSG);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ApiRestResponse<T> success() {
        return new ApiRestResponse<>();
    }

    public static <T> ApiRestResponse<T> success(T data) {
        ApiRestResponse<T> response = new ApiRestResponse<>();
        response.setData(data);
        return response;
    }

    public static <T> ApiRestResponse<T> error() {
        return new ApiRestResponse<>(ERROR_CODE, ERROR_MSG);
    }

    public static <T> ApiRestResponse<T> error(Integer code, String msg) {
        return new ApiRestResponse<>(code, msg);
    }
}