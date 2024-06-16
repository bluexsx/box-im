package com.bx.implatform.controller;

import com.bx.implatform.config.WebrtcConfig;
import com.bx.implatform.dto.PrivateMessageDTO;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.vo.SystemConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



/**
 * @author: blue
 * @date: 2024-06-10
 * @version: 1.0
 */
@Api(tags = "系统相关")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final WebrtcConfig webrtcConfig;

    @GetMapping("/config")
    @ApiOperation(value = "加载系统配置", notes = "加载系统配置")
    public Result<SystemConfigVO> loadConfig() {
        return ResultUtils.success(new SystemConfigVO(webrtcConfig));
    }


}
