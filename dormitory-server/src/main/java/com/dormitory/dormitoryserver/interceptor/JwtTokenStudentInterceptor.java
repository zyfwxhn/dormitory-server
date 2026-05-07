package com.dormitory.dormitoryserver.interceptor;

import com.dormitory.dormitoryserver.constant.JwtClaimsConstant;
import com.dormitory.dormitoryserver.context.BaseContext;
import com.dormitory.dormitoryserver.properties.JwtProperties;
import com.dormitory.dormitoryserver.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 学生端 JWT 令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenStudentInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getStudentTokenName());

        try {
            log.info("学生端 JWT 校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getStudentSecretKey(), token);

            // 将学生ID存入ThreadLocal
            Long studentId = Long.valueOf(claims.get(JwtClaimsConstant.STUDENT_ID).toString());
            log.info("当前登录的学生ID: {}", studentId);

            BaseContext.setCurrentId(studentId);
            return true;
        } catch (Exception ex) {
            log.error("学生端 JWT 校验失败或已过期");
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}