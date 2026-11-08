package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordUpdateDTO implements Serializable {
    private String oldPassword;
    private String newPassword;
}