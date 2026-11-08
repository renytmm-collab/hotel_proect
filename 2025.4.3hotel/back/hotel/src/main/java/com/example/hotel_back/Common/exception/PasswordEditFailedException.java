package com.example.hotel_back.Common.exception;

/**
 * 密码修改失败异常
 */
public class PasswordEditFailedException extends BaseException{

    public PasswordEditFailedException(String msg){
        super(msg);
    }

}
