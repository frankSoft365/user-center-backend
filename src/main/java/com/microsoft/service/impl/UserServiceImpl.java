package com.microsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microsoft.commen.ErrorCode;
import com.microsoft.exception.BusinessException;
import com.microsoft.mapper.UserMapper;
import com.microsoft.model.domain.User;
import com.microsoft.model.response.UserLoginResponse;
import com.microsoft.service.UserService;
import com.microsoft.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    /**
     * 用户注册 校验账户名和密码 将用户存入数据库 返回用户的id
     */
    @Override
    public Long userRegister(String userAccount, String password, String checkPassword) {
        // 用户账户名 密码 校验密码不能为空或者空字符串
        if (StringUtils.isAllBlank(userAccount, password, checkPassword)) {
            // 参数为空
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码或确认码为空");
        }
        // 账户名的长度要求 6-20 密码的长度要求 6-20
        if (userAccount.length() < 6 || userAccount.length() > 20) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名长度不合法");
        }
        if (password.length() < 6 || password.length() > 20) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不合法");
        }
        // 账户名里面不能有特殊字符 是字母 数字 下划线 中划线 密码 是字母数字常见特殊字符
        String USERNAME_VALID_REGEX = "^[a-zA-Z0-9_-]{6,20}$";
        boolean isUsernameMatch = Pattern.compile(USERNAME_VALID_REGEX).matcher(userAccount).matches();
        if (!isUsernameMatch) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名包含字符不合法");
        }
        String PASSWORD_VALID_REGEX = "^[a-zA-Z0-9!@#$%^&*()_+\\-=]{6,20}$";
        boolean isPasswordMatch = Pattern.compile(PASSWORD_VALID_REGEX).matcher(password).matches();
        if (!isPasswordMatch) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码包含字符不合法");
        }
        // 校验密码和密码相同
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码与确认码不一致");
        }
        // 账户名不能有重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = this.count(queryWrapper);
        if (count > 0) {
            log.info("用户账户名已存在");
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名已存在");
        }
        // 将密码加密
        String encodePassword = DigestUtils.md5Hex(password);
        // 插入用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setPassword(encodePassword);
        // 设置创建时间与修改时间
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "添加用户失败");
        }
        log.info("用户注册成功");
        return user.getId();
    }

    /**
     * 用户登录
     */
    @Override
    public UserLoginResponse userLogin(String userAccount, String password) {
        // 校验账户名与密码是否合法
        // 用户账户名 密码 校验密码不能为空或者空字符串
        if (StringUtils.isAllBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号或密码或确认码为空");
        }
        // 账户名的长度要求 6-20 密码的长度要求 6-20
        if (userAccount.length() < 6 || userAccount.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名长度不合法");
        }
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度不合法");
        }
        // 账户名里面不能有特殊字符 是字母 数字 下划线 中划线 密码 是字母数字常见特殊字符
        String USERNAME_VALID_REGEX = "^[a-zA-Z0-9_-]{6,20}$";
        boolean isUsernameMatch = Pattern.compile(USERNAME_VALID_REGEX).matcher(userAccount).matches();
        if (!isUsernameMatch) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名包含字符不合法");
        }
        String PASSWORD_VALID_REGEX = "^[a-zA-Z0-9!@#$%^&*()_+\\-=]{6,20}$";
        boolean isPasswordMatch = Pattern.compile(PASSWORD_VALID_REGEX).matcher(password).matches();
        if (!isPasswordMatch) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码包含字符不合法");
        }
        // 根据用户名查询用户看是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名或密码错误");
        }
        // 如果存在 将将要校验的密码与数据库的密码比对看是否一致
        String md5Hex = DigestUtils.md5Hex(password);
        if (!md5Hex.equals(user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "用户名或密码错误");
        }
        // 如果一致 登录成功 发放token
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", user.getId());
        dataMap.put("userAccount", user.getUserAccount());
        String token = JwtUtils.generateToken(dataMap);
        log.info("用户登录成功");
        return new UserLoginResponse(user.getId(), user.getUserAccount(), token);
    }

    @Override
    public User getMaskedUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User maskedUser = new User();
        maskedUser.setUsername(originUser.getUsername());
        maskedUser.setId(originUser.getId());
        maskedUser.setGender(originUser.getGender());
        maskedUser.setPhone(originUser.getPhone());
        maskedUser.setAvatar(originUser.getAvatar());
        maskedUser.setCreateTime(originUser.getCreateTime());
        maskedUser.setUserStatus(originUser.getUserStatus());
        maskedUser.setEmail(originUser.getEmail());
        maskedUser.setUserAccount(originUser.getUserAccount());
        maskedUser.setRole(originUser.getRole());
        return maskedUser;
    }

}
