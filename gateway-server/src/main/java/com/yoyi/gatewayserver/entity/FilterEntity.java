package com.yoyi.gatewayserver.entity;


import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hongfei.shen
 * 过滤器实体类
 * @date 2020/05/29
 */
@Data
public class FilterEntity {

    //过滤器对应的Name
    private String name;

    //路由规则
    private Map<String, String> args = new LinkedHashMap<>();

}