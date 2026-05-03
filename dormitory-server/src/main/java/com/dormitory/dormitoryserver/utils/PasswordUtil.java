package com.dormitory.dormitoryserver.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

/**
 * 密码工具类 — BCrypt + 旧 MD5 平滑迁移
 *
 * matches() 兼容旧 MD5 密码：BCrypt 验证失败时自动降级 MD5 比对，
 * 用户登录成功后可正常使用，下次修改密码时自动升级为 BCrypt。
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密明文密码 (BCrypt)
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证明文密码 — 兼容旧 MD5 数据库记录
     *
     * @return true=匹配, false=不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;

        // BCrypt hash 以 $2a$ 开头
        if (encodedPassword.startsWith("$2")) {
            return encoder.matches(rawPassword, encodedPassword);
        }

        // 兼容旧 MD5 密码 (32位十六进制)
        if (encodedPassword.length() == 32) {
            String md5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            return md5.equalsIgnoreCase(encodedPassword);
        }

        return false;
    }

    /**
     * 判断存储的密码是否为 BCrypt 格式（已迁移）
     */
    public static boolean isBCrypt(String encodedPassword) {
        return encodedPassword != null && encodedPassword.startsWith("$2");
    }
}
