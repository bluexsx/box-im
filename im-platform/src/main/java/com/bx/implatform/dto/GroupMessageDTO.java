package com.bx.implatform.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("群聊消息DTO")
public class GroupMessageDTO {

    @NotNull(message = "群聊id不可为空")
    @ApiModelProperty(value = "群聊id")
    private Long groupId;

    @Length(max = 1024, message = "发送内容长度不得大于1024")
    @NotEmpty(message = "发送内容不可为空")
    @ApiModelProperty(value = "发送内容")
    private String content;

    @NotNull(message = "消息类型不可为空")
    @ApiModelProperty(value = "消息类型")
    private Integer type;

    @Size(max = 20, message = "一次最多只能@20个小伙伴哦")
    @ApiModelProperty(value = "被@用户列表")
    private List<Long> atUserIds;
}
