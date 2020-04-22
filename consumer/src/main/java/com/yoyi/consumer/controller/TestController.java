package com.yoyi.consumer.controller;

import com.yoyi.consumer.crossserver.ProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author hongfei.shen
 * @date 2020/04/21
 */
@Slf4j
@RestController
public class TestController {
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ProviderService providerService;

    /*
     * RestTemplate 请求
     */
    @GetMapping("test")
    public String test() {
        // 通过spring cloud common中的负载均衡接口选取服务提供节点实现接口调用
        ServiceInstance serviceInstance = loadBalancerClient.choose("provider");
        String url = serviceInstance.getUri() + "/hello?name=" + "didi";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        return "Invoke : " + url + ", return : " + result;
    }

    /*
     *
     *RestTemplate增强请求
     */
    @GetMapping("restTest")
    public String restTest() {
        String result = restTemplate.getForObject("http://provider/hello?name=didi",
                String.class);
        return "Return : " + result;
    }

    /*
     * Feign请求
     */
    @GetMapping("/feignTest")
    public String feignTest() {
        String result = providerService.hello("didi");
        return "Return : " + result;
    }
}
