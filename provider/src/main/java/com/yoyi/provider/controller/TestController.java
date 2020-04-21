package com.yoyi.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hongfei.shen
 * @date 2020/04/21
 */
@Slf4j
@RestController
public class TestController {

    @GetMapping("hello")
    public String hello(@RequestParam String name) {
        log.info("invoked name=" + name);
        return "Hello " + name;
    }
}
