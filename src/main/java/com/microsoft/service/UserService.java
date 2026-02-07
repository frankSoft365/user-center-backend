package com.microsoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microsoft.model.domain.User;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    /**
     * 用户注册 校验账户名和密码 将用户存入数据库 返回用户的id
     */
    Long userRegister(String userAccount, String password, String checkPassword);

    /**
     * 用户登录 校验账户名 密码是否合法 根据账户名查询用户密码比对 相同则成功
     */
    User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户脱敏
     */
    User getMaskedUser(User originUser);

    /**
     * 用户注销
     */
    void userOutLogin(HttpServletRequest request);
}
