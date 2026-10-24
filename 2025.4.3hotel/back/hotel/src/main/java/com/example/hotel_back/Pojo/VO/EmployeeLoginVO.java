package com.example.hotel_back.Pojo.VO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EmployeeLoginVO implements Serializable {


    private Long id;


    private String username="帅气小子";


    private String realName;


    private String token;

    private Long roleId;

}