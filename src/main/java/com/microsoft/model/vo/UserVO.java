package com.microsoft.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private String username;
    private Long id;
    private Integer gender;
    private String phone;
    private String avatar;
    private LocalDateTime createTime;
    private Integer userStatus;
    private String email;
    private String userAccount;
    private Integer role;
}
