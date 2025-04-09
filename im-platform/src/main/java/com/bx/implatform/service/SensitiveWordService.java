package com.bx.implatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    /**
     * 查询所有开启的敏感词
     * @return
     */
    List<String> findAllEnabledWords();
}
