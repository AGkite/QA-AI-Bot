package com.newone.ai.robot.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.newone.ai.robot.model.AIResponse;

import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * 阿里云百炼
 */
@RestController
@RequestMapping("/v6/ai")
@Slf4j
public class AliyunBailianController {

    @Resource
    private OpenAiChatModel chatModel;

    /**
     * 普通对话
     */
    @GetMapping("/generate")
    public String generate(@RequestParam(value = "message", defaultValue = "你是谁？") String message) {
        return chatModel.call(message);
    }

    /**
     * 流式对话
     */
    @GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AIResponse> generateStream(@RequestParam(value = "message", defaultValue = "你是谁？") String message) {
        // 1. 打印入参日志
        log.info("开始流式对话请求，入参 message: [{}]", message);
        // 构建提示词
        Prompt prompt = new Prompt(new UserMessage(message));


        // 流式输出
        return chatModel.stream(prompt)
                .doOnNext(response -> log.info("原始响应数据：{}", response))
                .mapNotNull(chatResponse -> {
                    Generation generation = chatResponse.getResult();
                    String content = Objects.nonNull(generation) ? generation.getOutput().getText() : null;

                    log.debug("流式输出分片内容：{}", content);

                    return AIResponse.builder().v(content).build();
                })
                // 当流结束时触发
                .doOnComplete(() -> log.info("流式对话请求结束, message: [{}]", message))
                // 当发生异常时触发
                .doOnError(e -> log.error("流式对话请求发生异常，错误信息：{}", e.getMessage()));

    }
}
