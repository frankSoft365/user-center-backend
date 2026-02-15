package com.microsoft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microsoft.commen.ErrorCode;
import com.microsoft.commen.UserRoleEnum;
import com.microsoft.exception.BusinessException;
import com.microsoft.mapper.UserMapper;
import com.microsoft.model.entity.User;
import com.microsoft.model.dto.user.UserImportRequest;
import com.microsoft.model.vo.UserImportVO;
import com.microsoft.model.vo.UserLoginVO;
import com.microsoft.model.vo.UserVO;
import com.microsoft.service.UserService;
import com.microsoft.utils.ExcelParseUtil;
import com.microsoft.utils.JwtUtils;
import com.microsoft.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

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
        if (!RegexUtils.isValidUserAccount(userAccount)) {
            // 参数不合法
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名包含字符不合法");
        }
        if (!RegexUtils.isValidPassword(password)) {
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
    public UserLoginVO userLogin(String userAccount, String password) {
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
        if (!RegexUtils.isValidUserAccount(userAccount)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户名包含字符不合法");
        }
        if (!RegexUtils.isValidPassword(password)) {
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
        return new UserLoginVO(user.getId(), user.getUserAccount(), token);
    }

    @Override
    public UserImportVO verifyAndBatchImportUser(MultipartFile file) {
        // 拿到经过解析封装后的用户列表 在插入数据库之前进行校验
        // 校验无误 isSuccess : true
        // 校验有误 isSuccess : false 展示具体的所务信息
        List<UserImportRequest> userImportRequestList = ExcelParseUtil.parseExcel(file, UserImportRequest.class);

        if (CollectionUtils.isEmpty(userImportRequestList)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传的表格为空");
        }

        int total = userImportRequestList.size();

        List<String> errorMessageList = new ArrayList<>();
        List<User> succesList = new ArrayList<>();

        // 获取数据库中与这些将导入数据中账户名相同的用户信息（查询是否有重复）
        List<String> importUserAccount = userImportRequestList.stream()
                .map(UserImportRequest::getUserAccount)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        List<String> importPhone = userImportRequestList.stream()
                .map(UserImportRequest::getPhone)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        List<String> importEmail = userImportRequestList.stream()
                .map(UserImportRequest::getEmail)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (!CollectionUtils.isEmpty(importUserAccount)) {
            wrapper.or().in(User::getUserAccount, importUserAccount);
        }
        if (!CollectionUtils.isEmpty(importPhone)) {
            wrapper.or().in(User::getPhone, importPhone);
        }
        if (!CollectionUtils.isEmpty(importEmail)) {
            wrapper.or().in(User::getEmail, importEmail);
        }
        wrapper.select(User::getUserAccount, User::getPhone, User::getEmail);

        List<User> existList = userMapper.selectList(wrapper);

        Set<String> existUserAccount = existList.stream().map(User::getUserAccount).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> existPhone = existList.stream().map(User::getPhone).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        Set<String> existEmail = existList.stream().map(User::getEmail).filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        Set<String> seenUserAccount = new HashSet<>();
        Set<String> seenPhone = new HashSet<>();
        Set<String> seenEmail = new HashSet<>();

        for (int index = 0; index < userImportRequestList.size(); index++) {
            // 拿到一条用户待上传的数据
            UserImportRequest userImport = userImportRequestList.get(index);
            // 拿到对应表格中的行号
            int rowNum = index + 2;
            // 错误信息
            StringBuilder errorMessage = new StringBuilder();

            String password = userImport.getPassword();
            String userAccount = userImport.getUserAccount();
            String phone = userImport.getPhone();
            String email = userImport.getEmail();

            // 校验必填项-非空
            // 密码不能为空
            if (StringUtils.isAllBlank(password)) {
                errorMessage.append("密码不能为空；");
            }
            // 账户名不能为空
            if (StringUtils.isAllBlank(userAccount)) {
                errorMessage.append("账户名不能为空；");
            }

            // 批量上传中的唯一校验
            if (userAccount != null && seenUserAccount.contains(userAccount)) {
                errorMessage.append("账户名在批量上传列表中重复：").append(userAccount).append("；");
            }
            if (phone != null && seenPhone.contains(phone)) {
                errorMessage.append("电话在批量上传列表中重复：").append(phone).append("；");
            }
            if (email != null && seenEmail.contains(email)) {
                errorMessage.append("邮箱在批量上传列表中重复：").append(email).append("；");
            }

            // 校验格式、字符串符合枚举限制中
            // 密码格式是6-20位 只能包含常见特殊字符、字母、数字
            if (StringUtils.isNotBlank(password) && (password.length() < 6 || password.length() > 20)) {
                errorMessage.append("密码长度必须限制在6-20位；");
            }
            if (StringUtils.isNotBlank(password) && !RegexUtils.isValidPassword(password)) {
                errorMessage.append("密码包含的字符不合法；");
            }
            // 账户格式
            if (StringUtils.isNotBlank(userAccount) && (userAccount.length() < 6 || userAccount.length() > 20)) {
                errorMessage.append("账户名长度必须限制在6-20位；");
            }
            if (StringUtils.isNotBlank(userAccount) && !RegexUtils.isValidUserAccount(userAccount)) {
                errorMessage.append("账户名包含的字符不合法；");
            }
            // 用户昵称最多20个字符
            if (StringUtils.isNotBlank(userImport.getUsername()) && userImport.getUsername().length() > 20) {
                errorMessage.append("用户昵称长度超过20位；");
            }
            // 性别如果不为空，只能是男或女
            if (StringUtils.isNotBlank(userImport.getGender()) && !"男".equals(userImport.getGender()) && !"女".equals(userImport.getGender())) {
                errorMessage.append("性别输入不合法；");
            }
            // 如果手机号不为空 就要符合格式
            if (StringUtils.isNotBlank(phone) && !RegexUtils.isValidPhone(phone)) {
                errorMessage.append("手机号格式不合法；");
            }
            if (StringUtils.isNotBlank(userImport.getRole()) && UserRoleEnum.getEnumByName(userImport.getRole()) == null) {
                errorMessage.append("权限只能是").append((Arrays.stream(UserRoleEnum.values()).map(UserRoleEnum::getName)).toList()).append("之一；");
            }
            if (StringUtils.isNotBlank(email) && !RegexUtils.isValidEmail(email)) {
                errorMessage.append("邮箱格式不合法；");
            }

            // 校验唯一性
            // 账户名唯一
            if (existUserAccount.contains(userAccount)) {
                errorMessage.append("账户名已存在；");
            }
            if (existPhone.contains(phone)) {
                errorMessage.append("电话号码已存在；");
            }
            if (existEmail.contains(email)) {
                errorMessage.append("邮箱已存在；");
            }
            if (!errorMessage.isEmpty()) {
                errorMessageList.add("第" + rowNum + "行：" + errorMessage);
            } else {
                if (userAccount != null) {
                    seenUserAccount.add(userAccount);
                }
                if (phone != null) {
                    seenPhone.add(phone);
                }
                if (email != null) {
                    seenEmail.add(email);
                }
                succesList.add(UserImportRequest.convertToUser(userImport));
            }
        }
        if (errorMessageList.isEmpty()) {
            List<User> list = succesList.stream().map(user -> {
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                user.setPassword(DigestUtils.md5Hex(user.getPassword()));
                return user;
            }).toList();
            List<BatchResult> insert = userMapper.insert(list);
            if (insert.isEmpty()) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "导入失败");
            }
            List<UserVO> userVOList = getUserVO(list);
            return UserImportVO.success(total, userVOList);
        }
        return UserImportVO.error(total, errorMessageList.size(), errorMessageList);
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).toList();
    }

}
