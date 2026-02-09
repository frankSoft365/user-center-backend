package com.microsoft.interceptor;

import com.microsoft.utils.CurrentHold;
import com.microsoft.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {
    // 请求拦截器 拦截所有的向后端的请求
    // 请求头中的字段值：token 尝试解析token
    // 解析失败（token无效 token过期 token为空）抛出业务异常无权限用户未登录
    // 解析成功 保存token中的用户信息在线程存储中 放行该请求
    // 配置拦截器不拦截的请求路径 登录 注册
    // 将controller层的登录态判断代码删除
    // 在登录成功后 生成token 返回给前端
    // 生成token的工具类 解析token 生成token 两个方法
    // 前端补充 请求拦截器 如果浏览器存储有token 就把token添加到请求头中发给后端
    // 登录后将后端传过来的token存到浏览器存储中
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentHold.removeId();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获得token
        String authHeader = request.getHeader("Authorization");
        // 如果是空 返回401
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null || token.isEmpty()) {
            log.info("没有token，拒绝访问接口");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 拿到token 进行解析
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long id = Long.valueOf(claims.get("id").toString());
            CurrentHold.setCurrentId(id);
        } catch (Exception e) {
            log.info("token无效，拒绝访问接口");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 解析无报错 放行
        log.info("token有效，允许访问接口");
        return true;
    }
}
