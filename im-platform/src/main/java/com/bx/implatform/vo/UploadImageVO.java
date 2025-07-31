package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图片上传VO")
public class UploadImageVO {

    @Schema(description = "原图")
    private String originUrl;

    @Schema(description = "缩略图")
    private String thumbUrl;

    @Schema(description = "图片宽度")
    private int width;

    @Schema(description = "图片高度")
    private int height;
}
