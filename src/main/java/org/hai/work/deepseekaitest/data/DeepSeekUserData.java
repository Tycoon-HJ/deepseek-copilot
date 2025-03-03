package org.hai.work.deepseekaitest.data;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author yinha
 */
@State(name = "DeepSeekUserData",
        storages = @Storage("DeepSeekUserData.xml"))
@Service
public final class DeepSeekUserData implements PersistentStateComponent<DeepSeekUserData> {
    private String baseUrl;
    private String apiKey;
    private String testFramework;
    private String aiModel;

    @Override
    public @NotNull DeepSeekUserData getState() {
        return this;
    }

    @Override
    public void loadState(DeepSeekUserData state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }
}