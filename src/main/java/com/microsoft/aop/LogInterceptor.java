package com.microsoft.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Controller日志生成器
 */
@Slf4j
@Aspect
@Component
public class LogInterceptor {

    @Around("execution(* com.microsoft.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 打印日志
        // 准备发起请求： 要素：计时器统计方法运行耗时 给这个请求标注一个id 这个请求的参数有哪些 这个请求来自哪里
        // 这个请求请求哪个路径
        // 发起请求结束：展示方法耗时 具体是哪个id的请求
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String uuid = UUID.randomUUID().toString();
        Object[] args = joinPoint.getArgs();
        String params = "[ " + StringUtils.join(args, ", ") + " ]";
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String url = request.getRequestURI();
        String remoteHost = request.getRemoteAddr();
        log.info("request start : uuid : {} | requestUrl : {} | hostUrl : {} | params : {}", uuid, url, remoteHost, params);
        Object result = joinPoint.proceed();
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        log.info("request end : uuid : {} | costTime : {}ms", uuid, totalTimeMillis);
        return result;
    }
}
