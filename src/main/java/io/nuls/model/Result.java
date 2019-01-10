/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.constant.ErrorCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author vivi
 */
public class Result<T> implements Serializable {

    private boolean success;

    private String msg;

    private String errorCode;

    private T data;


    public Result() {
        this(true, ErrorCode.SUCCESS, null);
    }

    public Result(boolean success) {
        this.success = success;
        this.errorCode = ErrorCode.SUCCESS;
    }

    public Result(boolean success, String errorCode, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.data = data;
    }

    public Result(boolean success, String errorCode) {
        this.success = success;
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    @JsonIgnore
    public boolean isFailed() {
        return !success;
    }

    public Result<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }


    public static Result getFailed() {
        return getFailed(ErrorCode.FAILED);
    }

    public static Result getFailed(String msg) {
        return new Result(false, ErrorCode.FAILED, msg);
    }

    public static Result getSuccess() {
        return new Result(true);
    }


    public static Result getFailed(String errorCode, String msg) {
        Result result = new Result(false, errorCode, msg);
        return result;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

}
