package com.example.inutri.core;

import androidx.annotation.Nullable;

/** Wrapper leve de resultado (sucesso/erro) para fluxos simples. */
public class Result<T> {

    public final boolean isSuccess;
    @Nullable public final T data;
    @Nullable public final String error;

    private Result(boolean isSuccess, @Nullable T data, @Nullable String error) {
        this.isSuccess = isSuccess;
        this.data = data;
        this.error = error;
    }

    public static <T> Result<T> ok(@Nullable T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> error(@Nullable String message) {
        return new Result<>(false, null, message);
    }
}
