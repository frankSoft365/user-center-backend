package com.microsoft.model.dto.user;

import com.alibaba.excel.annotation.ExcelProperty;
import com.microsoft.model.enums.UserRoleEnum;
import com.microsoft.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对于请求的excel文件 映射其中的字段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserImportRequest {
    @ExcelProperty("用户昵称")
    private String username;
    @ExcelProperty("账户名")
    private String userAccount;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("电话")
    private String phone;
    @ExcelProperty("密码")
    private String password;
    @ExcelProperty("邮箱")
    private String email;
    @ExcelProperty("权限")
    private String role;

    public static User convertToUser(UserImportRequest importRequest) {
        User user = new User();
        user.setUsername(importRequest.getUsername());
        user.setGender(convertGender(importRequest.getGender()));
        user.setPhone(importRequest.getPhone());
        user.setPassword(importRequest.getPassword());
        user.setEmail(importRequest.getEmail());
        user.setUserAccount(importRequest.getUserAccount());
        user.setRole(UserRoleEnum.getEnumByName(importRequest.role).getValue());
        return user;
    }

    private static Integer convertGender(String gender) {
        if ("男".equals(gender)) {
            return 1;
        }
        if ("女".equals(gender)) {
            return 2;
        }
        return null;
    }
}
