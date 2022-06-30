package com.example.emos.wx.config;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class shiroJwtUtil {
    @Value("${emos.jwt.secret}")
    private String secret;
    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int id) {
        DateTime offset = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("userId", id);
        builder.withExpiresAt(offset);
        return builder.sign(Algorithm.HMAC256(secret));
    }

    public int getUserId(String token) {
        DecodedJWT decode = JWT.decode(token);
        Claim userId = decode.getClaim("userId");
        return userId.asInt();
    }

    public void verifierToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWT.require(algorithm).build().verify(token);
    }
}
