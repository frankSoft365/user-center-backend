package com.microsoft.model.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`user`")
public class User {
    private Long id;// 主键id
    private String username;// 用户名
    private Integer gender;// 性别 1 男 2 女
    private String phone;// 手机号
    private String password;// 密码
    private String avatar;// 头像的url
    private LocalDateTime createTime;// 创建时间
    private LocalDateTime updateTime;// 修改时间
    private Integer isDelete;// 是否删除 0 未被删除 1 被删除 默认不被删除
    private Integer userStatus;// 用户状态 1 正常
    private String email;// 电子邮件
    private String userAccount;// 用户账号
    private Integer role;// 用户权限 0 普通用户 1 管理员
}
