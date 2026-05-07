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
 * 管理员端 JWT 令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1、判断当前拦截到的是 Controller 的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法, 直接放行
            return true;
        }

        // 2、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 3、校验令牌
        try {
            log.info("管理员端 JWT 校验: {}", token);
            // 解析 Token, 如果过期或被篡改会抛出异常
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);

            // 使用我们刚建好的常量类获取 adminId
            Long adminId = Long.valueOf(claims.get(JwtClaimsConstant.ADMIN_ID).toString());
            log.info("当前登录的管理员ID: {}", adminId);

            // 将管理员 ID 存入 ThreadLocal
            BaseContext.setCurrentId(adminId);

            // 校验通过, 放行
            return true;
        } catch (Exception ex) {
            log.error("管理员 JWT 校验失败或已过期");
            response.setStatus(401);
            return false;
        }
    }

    /**
     * 清理 ThreadLocal, 防止内存泄漏和数据串号
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}