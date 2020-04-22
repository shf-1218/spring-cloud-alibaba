package com.yoyi.consumer.crossserver;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hongfei.shen
 * @date 2020/04/22
 */
@Service
@FeignClient("provider")
public interface ProviderService {

    @GetMapping("/hello")
    String hello(@RequestParam(name = "name") String name);
}
