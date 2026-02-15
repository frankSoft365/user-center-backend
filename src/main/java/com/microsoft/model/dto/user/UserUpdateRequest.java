package com.microsoft.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    // 只能编辑 用户名 性别 电话 头像 邮箱
    private Integer id;
    private String username;
    private Integer gender;
    private String phone;
    private String avatar;
    private String email;
}
