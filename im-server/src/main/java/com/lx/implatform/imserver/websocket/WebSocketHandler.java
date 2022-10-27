package com.lx.implatform.imserver.websocket;

import com.lx.common.contant.RedisKey;
import com.lx.common.enums.WSCmdEnum;
import com.lx.common.model.im.SendInfo;
import com.lx.common.util.SpringContextHolder;
import com.lx.implatform.imserver.websocket.processor.MessageProcessor;
import com.lx.implatform.imserver.websocket.processor.ProcessorFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * WebSocket 长连接下 文本帧的处理器
 * 实现浏览器发送文本回写
 * 浏览器连接状态监控
 */
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<SendInfo> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SendInfo sendInfo) throws Exception {
        // 创建处理器进行处理
        MessageProcessor processor = ProcessorFactory.createProcessor(WSCmdEnum.fromCode(sendInfo.getCmd()));
        processor.process(ctx,processor.transform(sendInfo.getData()));
    }

    /**
     * 出现异常的处理 打印报错日志
     *
     * @param ctx   the ctx
     * @param cause the cause
     * @throws Exception the Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        //关闭上下文
        //ctx.close();
    }

    /**
     * 监控浏览器上线
     *
     * @param ctx the ctx
     * @throws Exception the Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info(ctx.channel().id().asLongText() + "连接");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        AttributeKey<Long> attr = AttributeKey.valueOf("USER_ID");
        Long userId = ctx.channel().attr(attr).get();
        // 移除channel
        WebsocketChannelCtxHloder.removeChannelCtx(userId);
        // 用户下线
        RedisTemplate redisTemplate = SpringContextHolder.getBean("redisTemplate");
        String key = RedisKey.IM_USER_SERVER_ID + userId;
        redisTemplate.delete(key);
        log.info(ctx.channel().id().asLongText() + "断开连接");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }

    }
}