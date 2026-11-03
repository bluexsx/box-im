package com.bx.implatform.service.impl;

import com.alibaba.fastjson.JSON;
import com.bx.imcommon.enums.IMTerminalType;
import com.bx.imcommon.util.JwtUtil;
import com.bx.implatform.config.props.JwtProperties;
import com.bx.implatform.entity.User;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplRefreshTokenTest {

    private JwtProperties properties;
    private UserServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setAccessTokenSecret("test-access-secret");
        properties.setRefreshTokenSecret("test-refresh-secret");
        properties.setAccessTokenExpireIn(60);
        properties.setRefreshTokenExpireIn(600);
        service = spy(new UserServiceImpl(null, null, null, properties, null, null,
            mock(RedisTemplate.class)));
        user = new User();
        user.setId(1L);
        user.setUserName("current-user-name");
        user.setNickName("new-nickname");
        user.setIsBanned(false);
        doReturn(user).when(service).getById(1L);
    }

    @ParameterizedTest
    @EnumSource(value = IMTerminalType.class, names = {"WEB", "APP", "PC"})
    void refreshUsesCurrentProfileAndPreservesTerminal(IMTerminalType terminal) {
        String original = oldToken(terminal.code(), properties.getRefreshTokenSecret(), 600);
        LoginVO first = service.refreshToken(original);
        assertTokens(first, terminal.code(), "new-nickname");

        user.setNickName("newer-nickname");
        LoginVO second = service.refreshToken(first.getRefreshToken());
        assertTokens(second, terminal.code(), "newer-nickname");
    }

    @Test
    void invalidSignatureIsRejectedBeforeUserLookup() {
        String token = oldToken(0, "wrong-secret", 600);
        assertThrows(GlobalException.class, () -> service.refreshToken(token));
        verify(service, never()).getById(any());
    }

    @Test
    void expiredTokenIsRejectedBeforeUserLookup() {
        String token = oldToken(0, properties.getRefreshTokenSecret(), -60);
        assertThrows(GlobalException.class, () -> service.refreshToken(token));
        verify(service, never()).getById(any());
    }

    @Test
    void missingUserIsRejected() {
        doReturn(null).when(service).getById(1L);
        String token = oldToken(0, properties.getRefreshTokenSecret(), 600);
        assertThrows(GlobalException.class, () -> service.refreshToken(token));
    }

    @Test
    void bannedUserIsRejected() {
        user.setIsBanned(true);
        user.setReason("test");
        String token = oldToken(0, properties.getRefreshTokenSecret(), 600);
        assertThrows(GlobalException.class, () -> service.refreshToken(token));
    }

    private String oldToken(Integer terminal, String secret, long expiry) {
        UserSession oldSession = new UserSession();
        oldSession.setUserId(1L);
        oldSession.setUserName("old-user-name");
        oldSession.setNickName("old-nickname");
        oldSession.setTerminal(terminal);
        return JwtUtil.sign(1L, JSON.toJSONString(oldSession), expiry, secret);
    }

    private void assertTokens(LoginVO result, Integer terminal, String nickname) {
        assertEquals(properties.getAccessTokenExpireIn(), result.getAccessTokenExpiresIn());
        assertEquals(properties.getRefreshTokenExpireIn(), result.getRefreshTokenExpiresIn());
        assertToken(result.getAccessToken(), properties.getAccessTokenSecret(), terminal, nickname);
        assertToken(result.getRefreshToken(), properties.getRefreshTokenSecret(), terminal, nickname);
    }

    private void assertToken(String token, String secret, Integer terminal, String nickname) {
        assertTrue(JwtUtil.checkSign(token, secret));
        assertEquals(1L, JwtUtil.getUserId(token));
        UserSession info = JSON.parseObject(JwtUtil.getInfo(token), UserSession.class);
        assertEquals(1L, info.getUserId());
        assertEquals(nickname, info.getNickName());
        assertEquals("current-user-name", info.getUserName());
        assertEquals(terminal, info.getTerminal());
    }
}
