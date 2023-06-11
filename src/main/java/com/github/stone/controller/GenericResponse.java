package com.github.stone.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class GenericResponse<T> {

    public static final String SUCCESS_CODE = "0";
    public static final String ERROR_CODE = "10001";

    String code;
    T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String metadata;

    public GenericResponse(String code, T data) {
        this.code = code;
        this.data = data;
    }

    public GenericResponse(String code, T data, String metadata) {
        this.code = code;
        this.data = data;
        this.metadata = metadata;
    }
}
