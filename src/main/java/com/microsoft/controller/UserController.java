package com.microsoft.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.microsoft.commen.ErrorCode;
import com.microsoft.commen.Result;
import com.microsoft.exception.BusinessException;
import com.microsoft.model.domain.User;
import com.microsoft.model.request.UserLoginRequest;
import com.microsoft.model.request.UserRegisterRequest;
import com.microsoft.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.constant.UserConstant.DEFAULT_ROLE;
import static com.microsoft.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // 参数为空错误
            throw new BusinessException(ErrorCode.PARAM_ERROR, "前端传过来的参数为null");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAllBlank(userAccount, password, checkPassword)) {
            // 参数为空串
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名或密码或确认码为空");
        }
        Long id = userService.userRegister(userAccount, password, checkPassword);
        return Result.success(id);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            // 参数为空错误
            throw new BusinessException(ErrorCode.PARAM_ERROR, "前端传过来的参数为null");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String password = userLoginRequest.getPassword();
        if (StringUtils.isAllBlank(userAccount, password)) {
            // 参数为空串
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名或密码为空");
        }
        User user = userService.userLogin(userAccount, password, request);
        return Result.success(user);
    }

    /**
     * 用户注销
     */
    @PostMapping("/outLogin")
    public Result<Void> userOutLogin(HttpServletRequest request) {
        // 只有登录的用户才能注销
        User userInfo = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userInfo == null) {
            log.info("用户未登录");
            // 无权限
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }
        userService.userOutLogin(request);
        log.info("用户注销了");
        return Result.success();
    }

    /**
     * 用户编辑个人信息 只能编辑 用户名 性别 电话 头像 邮箱
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody User userInfoToUpdate, HttpServletRequest request) {
        // 只有登录的用户才能更改用户信息 且只能更改自己的用户信息
        User userInfo = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userInfo == null) {
            // 无权限
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }
        if (userInfoToUpdate == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无法获取更新后用户信息");
        }
        if (userInfo.getId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "未指定用户无法更新");
        }
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id", userInfo.getId());
        userUpdateWrapper.set("username", userInfoToUpdate.getUsername());
        userUpdateWrapper.set("gender", userInfoToUpdate.getGender());
        userUpdateWrapper.set("phone", userInfoToUpdate.getPhone());
        userUpdateWrapper.set("avatar", userInfoToUpdate.getAvatar());
        userUpdateWrapper.set("email", userInfoToUpdate.getEmail());
        boolean update = userService.update(userUpdateWrapper);
        if (!update) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新用户信息失败");
        }
        log.info("编辑个人信息");
        return Result.success();
    }

    /**
     * 根据用户名模糊查询用户列表
     */
    @GetMapping("/search")
    public Result<List<User>> searchUsers(String username, HttpServletRequest httpServletRequest) {
        if (!isAdmain(httpServletRequest)) {
            log.info("用户没有权限访问查询用户列表");
            // 无管理员权限
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isAllBlank(username)) {
            log.info("模糊查询用户名：{}", username);
            queryWrapper.like("username", username);
        }
        // 用户信息脱敏
        List<User> list = userService.list(queryWrapper);
        List<User> collect = list.stream().map(user -> userService.getMaskedUser(user)).collect(Collectors.toList());
        log.info("管理员获取用户列表，或筛选结果");
        return Result.success(collect);
    }

    /**
     * 根据用户的登录态获取用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        // currentUser为空 用户未登录
        if (currentUser == null) {
            log.info("用户未登录");
            // 无登录态
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }
        // 返回用户信息
        log.info("获取当前登录用户的信息");
        User user = userService.getById(currentUser.getId());
        User maskedUser = userService.getMaskedUser(user);
        return Result.success(maskedUser);
    }

    /**
     * 删除用户 只有管理员可以发起删除请求
     */
    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestBody Long id, HttpServletRequest httpServletRequest) {
        if (!isAdmain(httpServletRequest)) {
            // 无管理员权限
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无管理员权限");
        }
        if (id <= 0) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "前端传来的参数id不合法");
        }
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 判断用户是否是管理员
     */
    private boolean isAdmain(HttpServletRequest httpServletRequest) {
        User userInfo = (User) httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        if (userInfo == null) {
            log.info("用户未登录");
            return false;
        }
        Integer role = userInfo.getRole();
        if (role.equals(DEFAULT_ROLE)) {
            return false;
        }
        return true;
    }
}
