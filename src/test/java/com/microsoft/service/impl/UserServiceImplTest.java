package com.microsoft.service.impl;

import com.microsoft.model.entity.User;
import com.microsoft.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    /**
     * 测试新增一个用户
     */
    @Test
    public void testSave() {
        User user = new User();
        user.setUsername("frank");
        user.setGender(1);
        user.setPhone("12");
        user.setPassword("12");
        user.setAvatar("12");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setEmail("12");
        user.setUserAccount("frank");
        boolean save = userService.save(user);
        assertTrue(save);
        System.out.println(save);
        System.out.println("id : " + user.getId());
    }

    /**
     * 测试正确的用户名密码注册用户
     */
    @Test
    void testUserRegister() {
        // 测试1：账户名为空
        Long result1 = userService.userRegister("", "123456Abc!", "123456Abc!");
        Assertions.assertEquals(-1L, result1);

        // 测试2：密码为空
        Long result2 = userService.userRegister("test001", "", "123456Abc!");
        Assertions.assertEquals(-1L, result2);

        // 测试3：校验密码为空
        Long result3 = userService.userRegister("test001", "123456Abc!", "");
        Assertions.assertEquals(-1L, result3);

        // 测试4：全参数为空
        Long result4 = userService.userRegister("", "", "");
        Assertions.assertEquals(-1L, result4);
        // 测试1：账户名长度<6（5位）
        Long result5 = userService.userRegister("test1", "123456Abc!", "123456Abc!");
        Assertions.assertEquals(-1L, result5);

        // 测试2：账户名长度>20（21位）
        String longUserAccount = "test00000000000000000001"; // 21位
        Long result6 = userService.userRegister(longUserAccount, "123456Abc!", "123456Abc!");
        Assertions.assertEquals(-1L, result6);

        // 测试3：密码长度<6（5位）
        Long result7 = userService.userRegister("test002", "12345", "12345");
        Assertions.assertEquals(-1L, result7);

        // 测试4：密码长度>20（21位）
        String longPassword = "123456789012345678901"; // 21位
        Long result8 = userService.userRegister("test002", longPassword, longPassword);
        Assertions.assertEquals(-1L, result8);
        // 测试1：账户名含特殊字符@（非法）
        Long result9 = userService.userRegister("test@003", "123456Abc!", "123456Abc!");
        Assertions.assertEquals(-1L, result9);

        // 测试2：账户名含中文（非法）
        Long result10 = userService.userRegister("测试003", "123456Abc!", "123456Abc!");
        Assertions.assertEquals(-1L, result10);

        // 测试3：密码含非法字符（如空格）
        Long result11 = userService.userRegister("test003", "123456 Abc!", "123456 Abc!");
        Assertions.assertEquals(-1L, result11);

        // 测试4：密码含非法字符（如~`之外的特殊字符）
        Long result12 = userService.userRegister("test003", "123456Abc~`", "123456Abc~`");
        Assertions.assertEquals(-1L, result12);
        // 测试：密码和校验密码不同
        Long result13 = userService.userRegister("test004", "123456Abc!", "123456Abc");
        Assertions.assertEquals(-1L, result13);

        // 步骤1：先注册一个测试账户
        String testAccount = "test005";
        String testPassword = "123456Abc!";
        userService.userRegister(testAccount, testPassword, testPassword);

        // 步骤2：再次注册相同账户名（预期重复）
        Long result14 = userService.userRegister(testAccount, testPassword, testPassword);
        Assertions.assertEquals(-1L, result14);

        // 测试：合法参数，预期注册成功（返回非-1）
        String testAccount1 = "test006_";
        String testPassword1 = "123456Abc!";
        Long result = userService.userRegister(testAccount1, testPassword1, testPassword1);

        // 断言：注册成功（返回用户ID>0，或根据你的返回值调整）
        Assertions.assertNotEquals(-1L, result);
        Assertions.assertTrue(result > 0); // 若返回用户ID，此断言生效
    }

    @Test
    void doLogin() {
        // 先注册
        Long id = userService.userRegister("franksoft", "123456", "123456");
        // 用户账户名不合法
        // 密码不合法
        // 用户账户名不存在
        // 密码错误
        // 登录成功

    }
}