package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "私聊消息DTO")
public class PrivateMessageDTO {

    @NotNull(message = "接收用户id不可为空")
    @Schema(description = "接收用户id")
    private Long recvId;


    @Length(max = 1024, message = "内容长度不得大于1024")
    @NotEmpty(message = "发送内容不可为空")
    @Schema(description = "发送内容")
    private String content;

    @NotNull(message = "消息类型不可为空")
    @Schema(description = "消息类型 0:文字 1:图片 2:文件 3:语音 4:视频")
    private Integer type;

}
