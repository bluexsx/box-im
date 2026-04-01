package com.bx.imserver.netty.processor;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMForceLogoutInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.constant.ChannelAttrKey;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Blue
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForceLogoutProcessor extends AbstractMessageProcessor<IMForceLogoutInfo> {

    @Override
    public void process(IMForceLogoutInfo info) {
        ChannelHandlerContext context = UserChannelCtxMap.getChannelCtx(info.getUserId(), info.getTerminal());
        AttributeKey<String> devIdAttr = AttributeKey.valueOf(ChannelAttrKey.DEVICE_ID);
        String devId = context.channel().attr(devIdAttr).get();
        if (StrUtil.isEmpty(info.getDevId()) || !info.getDevId().equals(devId)) {
            // 不允许多地登录,强制下线
            IMSendInfo<Object> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGOUT.code());
            sendInfo.setData("您已在其他地方登录，将被强制下线");
            context.channel().writeAndFlush(sendInfo);
            log.info("异地登录，强制下线,userId:{},终端:{}", info.getUserId(), info.getTerminal());
        }
    }
}
