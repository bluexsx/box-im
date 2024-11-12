package com.bx.implatform.controller;

import com.bx.implatform.config.WebrtcConfig;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.vo.SystemConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @author: blue
 * @date: 2024-06-10
 * @version: 1.0
 */
@Tag(name = "系统相关")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final WebrtcConfig webrtcConfig;

    @GetMapping("/config")
    @Operation(summary = "加载系统配置", description = "加载系统配置")
    public Result<SystemConfigVO> loadConfig() {
        return ResultUtils.success(new SystemConfigVO(webrtcConfig));
    }


}
