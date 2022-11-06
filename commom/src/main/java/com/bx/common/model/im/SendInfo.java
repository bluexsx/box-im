package com.bx.common.model.im;

import lombok.Data;

@Data
public class SendInfo<T> {

        private Integer cmd;
        private T data;

}
