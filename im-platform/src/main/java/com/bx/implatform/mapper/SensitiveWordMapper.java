package com.bx.implatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bx.implatform.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

/**
* 敏感词
*
* @author Blue 
* @since 1.0.0 2024-07-20
*/
@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
	
}