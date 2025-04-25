package org.hai.work.deepseekaitest.util;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

/**
 * AI模型封装工具类
 */
public class AiUtil {
    public final static String SYSTEMMESSAGE = """
            请生成 Java 代码。
            输出格式：只包含 Java 方法定义，每个方法前必须有 Javadoc 注释。
            限制：
            - 只输出方法签名、方法体和对应的 Javadoc 注释。
            - **不要**包含任何类定义（例如：`public class MyClass { ... }`）。
            - **不要**包含任何 import 语句（例如：`import java.util.*;`）。
            - **不要**包含任何 package 声明（例如：`package com.example;`）。
            - **不要**包含任何额外的文字说明、解释或 Markdown 格式（例如：```java...```）。
            - 输出内容必须**仅是**方法及其 Javadoc 的序列。
            """;
    private static OllamaChatModel ollamaChatModel;

    /**
     * 获取Ollama实例
     *
     * @return
     */
    public static OllamaChatModel gainOllamaChatModelInstance() {
        if (Objects.isNull(ollamaChatModel)) {
            OllamaApi ollamaApi = new OllamaApi("http://localhost:11434");
            OllamaOptions ollamaOptions = new OllamaOptions();
            ollamaOptions.setModel("deepseek-coder-v2:16b");
            return OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(ollamaOptions).build();
        }
        return ollamaChatModel;
    }

    public static Flux<String> generateCodeStream(String textBeforeCrate) {
        return Flux.just(getAiResult(textBeforeCrate))
                .subscribeOn(Schedulers.boundedElastic()); // 在一个后台线程池中生成数据
    }

    public static String generateCodeStr(String textBeforeCrate) {
        return getAiResult(textBeforeCrate);
    }

    private static String getAiResult(String textBeforeCrate) {
        OllamaChatModel ollamaChatModel = gainOllamaChatModelInstance();
        SystemMessage systemMessage = new SystemMessage(AiUtil.SYSTEMMESSAGE);
        Prompt prompt = new Prompt(systemMessage, new UserMessage(textBeforeCrate));
        return ollamaChatModel.call(prompt).getResult().toString().split("```java")[1].split("```")[0];
    }
}
