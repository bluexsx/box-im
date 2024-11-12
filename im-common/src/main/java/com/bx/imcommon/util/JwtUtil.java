package com.bx.imcommon.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;

public final class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 生成jwt字符串  JWT(json web token)
     *
     * @param userId   用户id
     * @param info     用户细腻系
     * @param expireIn 过期时间
     * @param secret   秘钥
     * @return token
     */
    public static String sign(Long userId, String info, long expireIn, String secret) {
        try {
            Date date = new Date(System.currentTimeMillis() + expireIn * 1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    //将userId保存到token里面
                    .withAudience(userId.toString())
                    //存放自定义数据
                    .withClaim("info", info)
                    //过期时间
                    .withExpiresAt(date)
                    //token的密钥
                    .sign(algorithm);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据token获取userId
     *
     * @param token 登录token
     * @return 用户id
     */
    public static Long getUserId(String token) {
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            return Long.parseLong(userId);
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据token获取用户数据
     *
     * @param token 用户登录token
     * @return 用户数据
     */
    public static String getInfo(String token) {
        try {
            return JWT.decode(token).getClaim("info").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验token
     *
     * @param token  用户登录token
     * @param secret 秘钥
     * @return true/false
     */
    public static Boolean checkSign(String token, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
}
