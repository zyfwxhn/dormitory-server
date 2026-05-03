package com.dormitory.dormitoryserver.interceptor;

import com.dormitory.dormitoryserver.constant.JwtClaimsConstant;
import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.properties.JwtProperties;
import com.dormitory.dormitoryserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 维修员端 JWT 令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenWorkerInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getWorkerTokenName());

        try {
            log.info("维修员 JWT 校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getWorkerSecretKey(), token);

            // 【重构点】使用常量替换魔法字符串 "workerId"
            Long workerId = Long.valueOf(claims.get(JwtClaimsConstant.WORKER_ID).toString());
            log.info("当前登录的维修员ID: {}", workerId);

            BaseContext.setCurrentId(workerId);
            return true;
        } catch (Exception ex) {
            log.error("维修员 JWT 解析失败或已过期：{}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}