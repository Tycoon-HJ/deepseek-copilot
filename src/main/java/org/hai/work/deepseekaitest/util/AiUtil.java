package org.hai.work.deepseekaitest.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import org.hai.work.deepseekaitest.data.DeepSeekUserData;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

/**
 * AI模型封装工具类
 */
public class AiUtil {
    public final static String SYSTEM_MESSAGE = """
            请生成 Java 代码。
            输出格式：只包含 Java 方法定义，每个方法前必须有 Javadoc 注释。
            限制：
            - 只输出方法签名、方法体和对应的 Javadoc 注释。
            - **不要**包含任何类定义（例如：`public class MyClass { ... }`）。
            - **不要**包含任何 import 语句（例如：`import java.util.*;`）。
            - **不要**包含任何 package 声明（例如：`package com.example;`）。
            - **不要**包含任何额外的文字说明、解释或 Markdown 格式。
            - 输出内容必须**仅是**方法及其 Javadoc 的序列。
            - 只返回一个方法，必须使用代码块返回（例如：```java...```）
            """;

    public final static String TEST_PROME_MESSAGE = """
            为以下 Java 代码生成 TestNG 单元测试。
            要求：
            1.  生成一个完整的 TestNG Java 测试类。
            2.  包含 TestNG 和被测代码所需的所有 import 语句。
            3.  编写测试方法 (`@Test`)，覆盖所提供 Java 代码中方法的所有逻辑分支。
            4.  **不要**为所提供 Java 输入中被注释掉的代码生成单元测试。
            5.  确保所有生成的测试都是正确且可使用 TestNG 执行的。
            
            输出：
            **只提供** TestNG 测试类的 Java 代码。
            **不要**包含任何解释性文字、描述、引言、结论或 Markdown 格式。
            响应内容必须**仅是**测试类的纯 Java 代码。
            必须使用代码块返回（例如：```java...```）
            待测试的 Java 代码：
            """;

    public final static String COMMON = """
            只输出注释部分，不要返回原代码
            使用多行注释格式 /** ... */
            自动根据代码的缩进生成相同缩进的注释
            第一行简要说明代码整体功能（不超过两句）
            函数若有入参，使用 @param 描述每个参数的含义
            函数若有返回值，使用 @return 说明返回值
            若函数可能抛出异常，请使用 @throws 说明原因
            语言简洁、专业，保持 JavaDoc 风格
            """;
    private static OllamaChatModel ollamaChatModel;
    private static OpenAiChatModel openAiChatModel;
    private static DeepSeekChatModel deepSeekChatModel;

    public static boolean checkAiIsAlready() {
        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
        if (deepSeekUserData == null || Objects.isNull(deepSeekUserData.getBaseUrl()) || Objects.isNull(deepSeekUserData.getApiKey())
                || Objects.isNull(deepSeekUserData.getAiModel()) || Objects.isNull(deepSeekUserData.getTestFramework())) {
            ApplicationManager.getApplication().invokeLater(() ->
                    Messages.showMessageDialog("AI-related configurations have not been loaded. Please configure the AI parameters in the settings first", "DeepSeek Copilot", Messages.getWarningIcon()));
            return true;
        }
        return false;
    }


    public static void initAiChatModel() {
        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
        if (deepSeekChatModel == null || openAiChatModel == null) {
            try {
                ChatModel chatModel = AiUtil.initChatModel(deepSeekUserData.getBaseUrl(), deepSeekUserData.getApiKey(), deepSeekUserData.getAiModel());
                chatModel.call("hello");
            } catch (Exception ex) {
                AiUtil.destroyAllAi();
                Messages.showMessageDialog("Error!!! Please replace the AI configuration parameters!!!", "Test AI Connection", Messages.getErrorIcon());
            }
        }
    }

    public static ChatModel initChatModel(String baseUrl, String apiKey, String model) {
        if (model.contains("deepseek")) {
            if (deepSeekChatModel == null) {
                DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                        .apiKey(apiKey)
                        .baseUrl(baseUrl)
                        .build();
                DeepSeekChatOptions deepSeekChatOptions = DeepSeekChatOptions.builder()
                        .model(model)
                        .temperature(0.2)
                        .maxTokens(8192)
                        .build();
                deepSeekChatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(deepSeekChatOptions).build();
            }
            return deepSeekChatModel;
        }
        if (openAiChatModel == null) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .build();
            OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                    .model(model)
                    .temperature(0.2)
                    .maxTokens(8192)
                    .build();
            openAiChatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();
        }
        return openAiChatModel;
    }

    public static void destroyAllAi() {
        openAiChatModel = null;
        deepSeekChatModel = null;
    }

    public static Flux<String> generateCodeStream(String code) {
        return Flux.just(getAiResult(code))
                .subscribeOn(Schedulers.boundedElastic()); // 在一个后台线程池中生成数据
    }

    public static String generateCodeStr(String code) {
        return getAiResult(code);
    }

    private static String getAiResult(String code) {
        SystemMessage systemMessage = new SystemMessage(AiUtil.SYSTEM_MESSAGE);
        Prompt prompt = new Prompt(systemMessage, new UserMessage(code));
        return getResult(Objects.requireNonNullElse(openAiChatModel, deepSeekChatModel).call(prompt));
    }

    /**
     * 生产代码注释
     *
     * @param code
     * @return
     */
    public static String getCodeComment(String code) {
        SystemMessage systemMessage = new SystemMessage(AiUtil.COMMON);
        Prompt prompt = new Prompt(systemMessage, new UserMessage(code));
        ChatModel chatModel = Objects.requireNonNullElse(openAiChatModel, deepSeekChatModel);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    public static String getAiTestResult(String code, String testFramework) {
        SystemMessage systemMessage = new SystemMessage(AiUtil.TEST_PROME_MESSAGE.replace("TestNG", testFramework));
        Prompt prompt = new Prompt(systemMessage, new UserMessage(code));
        return getResult(Objects.requireNonNullElse(openAiChatModel, deepSeekChatModel).call(prompt));
    }


    private static String getResult(ChatResponse chatModel) {
        return chatModel.getResult().toString().split("```java")[1].split("```")[0];
    }

    /**
     * 获取Ollama实例--本地测试使用
     *
     * @return
     */
    public static OllamaChatModel gainOllamaChatModelInstance() {
        if (Objects.isNull(ollamaChatModel)) {
            OllamaApi ollamaApi = OllamaApi.builder().baseUrl("http://localhost:11434").build();
            OllamaChatOptions ollamaOptions = new OllamaChatOptions();
            ollamaOptions.setModel("deepseek-coder-v2:16b");
            ollamaChatModel = OllamaChatModel.builder().ollamaApi(ollamaApi).defaultOptions(ollamaOptions).build();
        }
        return ollamaChatModel;
    }


    private static String getAiResultTest(String textBeforeCrate) {
        OllamaChatModel ollamaChatModel = gainOllamaChatModelInstance();
        SystemMessage systemMessage = new SystemMessage(AiUtil.SYSTEM_MESSAGE);
        Prompt prompt = new Prompt(systemMessage, new UserMessage(textBeforeCrate));
        return getResult(ollamaChatModel.call(prompt));
    }
}
