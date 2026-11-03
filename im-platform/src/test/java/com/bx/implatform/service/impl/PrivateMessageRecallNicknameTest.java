package com.bx.implatform.service.impl;

import com.alibaba.fastjson.JSON;
import com.bx.imclient.IMClient;
import com.bx.implatform.entity.PrivateMessage;
import com.bx.implatform.entity.User;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.UserMapper;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.vo.PrivateMessageVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.AopContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateMessageRecallNicknameTest {

    @Spy
    @InjectMocks
    private PrivateMessageServiceImpl service;
    @Mock
    private UserMapper userMapper;
    @Mock
    private IMClient imClient;
    private PrivateMessage original;

    @BeforeEach
    void setUp() {
        original = new PrivateMessage();
        original.setId(10L);
        original.setSendId(1L);
        original.setRecvId(2L);
        original.setConvKey("1:2");
        original.setSendTime(new Date());
        original.setStatus(MessageStatus.PENDING.code());
        doAnswer(invocation -> original).when(service).getById(10L);

        UserSession session = new UserSession();
        session.setUserId(1L);
        session.setTerminal(0);
        session.setNickName("old-nickname");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("session", session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void recallUsesCurrentNicknameEvenWithOldAccessToken() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setNickName("new-nickname");
        // On the old implementation this lookup is not used, and the assertion below fails.
        lenient().when(userMapper.selectById(1L)).thenReturn(currentUser);
        doReturn(true).when(service).updateById(any(PrivateMessage.class));
        doNothing().when(service).saveMessage(any(PrivateMessage.class));
        try (MockedStatic<AopContext> context = mockStatic(AopContext.class)) {
            context.when(AopContext::currentProxy).thenReturn(service);
            PrivateMessageVO result = service.recallMessage(10L);
            assertEquals("new-nickname 撤回了一条消息",
                JSON.parseObject(result.getContent()).getString("tip"));
            verify(imClient).sendPrivateMessage(argThat(message ->
                ((PrivateMessageVO) message.getData()).getContent().equals(result.getContent())));
        }
    }

    @Test
    void cannotRecallSomeoneElsesMessage() {
        original.setSendId(3L);
        assertThrows(GlobalException.class, () -> service.recallMessage(10L));
        verifyNoInteractions(userMapper, imClient);
    }

    @Test
    void missingUserIsRejectedBeforeChangingMessage() {
        assertThrows(GlobalException.class, () -> service.recallMessage(10L));
        assertEquals(MessageStatus.PENDING.code(), original.getStatus());
        verify(service, never()).updateById(any(PrivateMessage.class));
        verifyNoInteractions(imClient);
    }

    @Test
    void missingMessageIsRejected() {
        original = null;
        assertThrows(GlobalException.class, () -> service.recallMessage(10L));
        verifyNoInteractions(userMapper, imClient);
    }

    @Test
    void expiredMessageIsRejected() {
        original.setSendTime(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L));
        assertThrows(GlobalException.class, () -> service.recallMessage(10L));
        verifyNoInteractions(userMapper, imClient);
    }
}
