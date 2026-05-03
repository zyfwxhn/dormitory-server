package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerInfoVO implements Serializable {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private String skills;
    private Integer isAvailable;
}
