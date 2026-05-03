package com.dormitory.dormitoryserver.exception;

/**
 * 业务异常基类
 * 系统中所有我们主动抛出的业务错误，都应该继承这个类或直接抛出这个类
 */
public class BaseException extends RuntimeException {

    public BaseException() {
    }

    public BaseException(String msg) {
        super(msg);
    }
}