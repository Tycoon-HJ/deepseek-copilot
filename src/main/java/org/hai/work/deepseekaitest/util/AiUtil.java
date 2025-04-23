package org.hai.work.deepseekaitest.util;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.Objects;

/**
 * AI模型封装工具类
 */
public class AiUtil {

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
}
