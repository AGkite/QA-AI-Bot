package com.newone.ai.robot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话响应类
 * 
 * @author liuwh
 * @date 2026/01/12 17:14:32
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIResponse {
    // 流式响应内容
    private String v;
}
