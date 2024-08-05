package com.bx.implatform.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("邀请好友进群请求VO")
public class GroupInviteVO {

    @NotNull(message = "群id不可为空")
    @ApiModelProperty(value = "群id")
    private Long groupId;

    @NotEmpty(message = "群id不可为空")
    @ApiModelProperty(value = "好友id列表不可为空")
    private List<Long> friendIds;
}
