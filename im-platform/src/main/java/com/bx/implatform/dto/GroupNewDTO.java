package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "选择好友创建群聊请求")
public class GroupNewDTO {

    @Size(min = 1, max = 50, message = "一次最多选择50位好友")
    @NotEmpty(message = "请选择好友")
    @Schema(description = "好友用户id列表")
    private List<Long> userIds;
}
