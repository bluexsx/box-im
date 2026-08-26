package com.bx.imserver.netty.processor;

import cn.hutool.core.util.StrUtil;
import com.bx.imcommon.enums.IMCmdType;
import com.bx.imcommon.model.IMForceLogoutData;
import com.bx.imcommon.model.IMForceLogoutInfo;
import com.bx.imcommon.model.IMSendInfo;
import com.bx.imserver.constant.ChannelAttrKey;
import com.bx.imserver.netty.UserChannelCtxMap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        if (Objects.isNull(context)) {
            return;
        }
        AttributeKey<String> devIdAttr = AttributeKey.valueOf(ChannelAttrKey.DEVICE_ID);
        String devId = context.channel().attr(devIdAttr).get();
        if (StrUtil.isEmpty(info.getDevId()) || !info.getDevId().equals(devId)) {
            IMForceLogoutData data = new IMForceLogoutData();
            data.setType(info.getType());
            data.setReason(info.getReason());
            IMSendInfo<IMForceLogoutData> sendInfo = new IMSendInfo<>();
            sendInfo.setCmd(IMCmdType.FORCE_LOGOUT.code());
            sendInfo.setData(data);
            context.channel().writeAndFlush(sendInfo).addListener(ChannelFutureListener.CLOSE);
            log.info("强制下线,userId:{},终端:{},type:{}", info.getUserId(), info.getTerminal(), info.getType());
        }
    }
}
