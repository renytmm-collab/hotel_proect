package com.example.hotel_back.Pojo.DAO;

import lombok.Data;

import java.io.Serializable;

@Data
public class RoleAddDTO implements Serializable {
    private String roleName;
    private String description;
}