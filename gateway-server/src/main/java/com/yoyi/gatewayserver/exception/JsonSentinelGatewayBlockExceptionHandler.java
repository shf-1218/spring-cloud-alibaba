package com.yoyi.gatewayserver.exception;


import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.function.Supplier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author hongfei.shen
 * @date 2020/05/29
 */
@Slf4j
@Component
public class JsonSentinelGatewayBlockExceptionHandler implements WebExceptionHandler {
    @Autowired
    private ObjectMapper objectMapper;
    private List<ViewResolver> viewResolvers;
    private List<HttpMessageWriter<?>> messageWriters;
    private final Supplier<ServerResponse.Context> contextSupplier = () -> {
        return new ServerResponse.Context() {
            @Override
            public List<HttpMessageWriter<?>> messageWriters() {
                return JsonSentinelGatewayBlockExceptionHandler.this.messageWriters;
            }

            @Override
            public List<ViewResolver> viewResolvers() {
                return JsonSentinelGatewayBlockExceptionHandler.this.viewResolvers;
            }
        };
    };

    public JsonSentinelGatewayBlockExceptionHandler(List<ViewResolver> viewResolvers,
                                                    ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolvers;
        this.messageWriters = serverCodecConfigurer.getWriters();
    }

    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        } else {
            return !BlockException.isBlockException(ex) ? Mono.error(ex) :
                    this.handleBlockedRequest(exchange, ex).flatMap((response) -> {
                        return this.writeResponse(response, exchange);
                    });
        }
    }

    private Mono<ServerResponse> handleBlockedRequest(ServerWebExchange exchange, Throwable throwable) {
        return GatewayCallbackManager.getBlockHandler().handleRequest(exchange, throwable);
    }

    /**
     * 只需要修改此方法
     */
    public Mono<Void> writeResponse(ServerResponse serverWebExchange, ServerWebExchange exchange) {
        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        return unAuth(serverHttpResponse, "访问的人太多了，请稍后再试！");
    }

    private Mono<Void> unAuth(ServerHttpResponse resp, String msg) {
        resp.setStatusCode(HttpStatus.FORBIDDEN);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String result = "";
        try {
            result = objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        DataBuffer buffer = resp.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }
}
