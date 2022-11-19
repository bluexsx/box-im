package com.bx.imcommon.model;

import lombok.Data;

@Data
public class IMSendInfo<T> {

        private Integer cmd;
        private T data;

}
