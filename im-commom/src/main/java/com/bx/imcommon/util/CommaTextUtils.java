package com.bx.imcommon.util;



import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 逗号分格文本处理工具类
 *
 * @author: blue
 * @date: 2023-11-09 09:52:49
 * @version: 1.0
 */
public class CommaTextUtils {

    /**
     * 文本转列表
     *
     * @param strText 文件
     * @return 列表
     */
    public static List<String> asList(String strText) {
        if (StrUtil.isEmpty(strText)) {
            return new LinkedList<>();
        }
        return new LinkedList<>(Arrays.asList(strText.split(",")));

    }

    /**
     * 列表转字符串，并且自动清空、去重、排序
     *
     * @param texts 列表
     * @return 文本
     */
    public static <T> String asText(Collection<T> texts) {
        if (CollUtil.isEmpty(texts)) {
            return StrUtil.EMPTY;
        }
        return texts.stream().map(text -> StrUtil.toString(text)).filter(StrUtil::isNotEmpty).distinct().sorted().collect(Collectors.joining(","));
    }

    /**
     * 追加一个单词
     *
     * @param strText 文本
     * @param word    单词
     * @return 文本
     */
    public static <T> String appendWord(String strText, T word) {
        List<String> texts = asList(strText);
        texts.add(StrUtil.toString(word));
        return asText(texts);
    }

    /**
     * 删除一个单词
     *
     * @param strText 文本
     * @param word    单词
     * @return 文本
     */
    public static <T> String removeWord(String strText, T word) {
        List<String> texts = asList(strText);
        texts.remove(StrUtil.toString(word));
        return asText(texts);
    }

    /**
     * 合并
     *
     * @param strText1 文本1
     * @param strText2 文本2
     * @return 文本
     */
    public static String merge(String strText1, String strText2) {
        List<String> texts = asList(strText1);
        texts.addAll(asList(strText2));
        return asText(texts);
    }

}
