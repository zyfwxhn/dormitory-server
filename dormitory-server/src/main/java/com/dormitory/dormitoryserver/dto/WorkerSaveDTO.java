package com.dormitory.dormitoryserver.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class WorkerSaveDTO implements Serializable {
    private Integer page;
    private Integer pageSize;
    private Long id;
    private String username;
    private String password;
    private String name;
    private String phone;
    private String skills;
}
