package com.microsoft.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microsoft.model.entity.User;
import com.microsoft.model.vo.UserImportVO;
import com.microsoft.model.vo.UserLoginVO;
import com.microsoft.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册 校验账户名和密码 将用户存入数据库 返回用户的id
     */
    Long userRegister(String userAccount, String password, String checkPassword);

    /**
     * 用户登录 校验账户名 密码是否合法 根据账户名查询用户密码比对 相同则成功
     */
    UserLoginVO userLogin(String userAccount, String password);

    /**
     * 批量导入的用户信息
     */
    UserImportVO verifyAndBatchImportUser(MultipartFile file);

    /**
     * 获取单个用户VO
     */
    UserVO getUserVO(User user);

    /**
     * 获取集合的用户VO
     */
    List<UserVO> getUserVO(List<User> userList);
}
