package com.microsoft.aop;

import com.microsoft.annotation.AuthCheck;
import com.microsoft.commen.ErrorCode;
import com.microsoft.commen.UserRoleEnum;
import com.microsoft.exception.BusinessException;
import com.microsoft.model.entity.User;
import com.microsoft.service.UserService;
import com.microsoft.utils.CurrentHold;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 对特定接口添加advice进行鉴权
 */
@Aspect
@Component
@Slf4j
public class AuthInterceptor {
    @Resource
    private UserService userService;
    /**
     * 进行拦截 范围：标注特定注解（注解赋值标明何种权限可以访问）的接口
     * 执行：获取发起该请求的用户的id 查询该用户的权限 对比用户权限与预设所要求的权限
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 何种权限可以访问
        int mustRole = authCheck.mustRole();
        // 获取用户的身份信息
        Long currentId = CurrentHold.getCurrentId();
        User userInfo = userService.getById(currentId);
        // 鉴定权限 只要不匹配抛异常返回
            UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(mustRole);
            if (enumByValue == null) {
                throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
            }
            // 用户的身份是：
            Integer role = userInfo.getRole();
            // 预设要求是管理员的话
            if (UserRoleEnum.ADMIN_ROLE.equals(enumByValue)) {
                // 用户的权限不符：
                if (mustRole != role) {
                    throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限：" + enumByValue.getName());
                }
            }
        // 放行原本方法体
        return joinPoint.proceed();
    }
}
