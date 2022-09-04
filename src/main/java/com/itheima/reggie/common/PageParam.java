package com.itheima.reggie.common;

import lombok.Data;

@Data
public class PageParam {
    private int page;
    private int pageSize;
    private String name;


    private String beginTime;


    private String endTime;
}
