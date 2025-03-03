package org.hai.work.deepseekaitest;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.hai.work.deepseekaitest.data.DeepSeekUserData;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import javax.swing.*;


@State(name = "DeepseekTestTool", storages = @Storage("DeepseekTestTool.xml"))
public class DeepseekTestTool extends BaseConfigurable implements SearchableConfigurable, PersistentStateComponent<DeepseekTestTool> {

    DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
    private JTextField textField1;
    private JTextField textField2;
    private JComboBox<String> comboBox1;
    private JButton jButton;
    private JPanel jpanel;
    private JButton netTestPing;
    private JComboBox<String> comboBox2;
    private String basUrl;
    private String apiKey;
    private String testFramework;
    private String aiModel;

    public DeepseekTestTool() {

        comboBox1.addItem("TestNG");
        comboBox1.addItem("Junit4");
        comboBox1.addItem("Junit5");
        comboBox1.addItem("SpringBootTest");

        comboBox2.addItem("gpt-4o-mini");
        comboBox2.addItem("deepseek-r1");


        // 添加监听事件
        jButton.addActionListener(e -> {
            deepSeekUserData.setBaseUrl(textField1.getText());
            deepSeekUserData.setApiKey(textField2.getText());
            deepSeekUserData.setTestFramework((String) comboBox1.getSelectedItem());
            deepSeekUserData.setAiModel((String) comboBox2.getSelectedItem());
        });

        netTestPing.addActionListener(e -> {

            try {
                OpenAiApi openAiApi = new OpenAiApi(textField1.getText(), textField2.getText());
                OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder()
                        .model("gpt-4o-mini")
                        .temperature(0.2)
                        .maxTokens(200000)
                        .build();
                String prompt = """
                        hello
                        """;
                ChatModel chatModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
                String call = chatModel.call(prompt);
                Messages.showMessageDialog("恭喜连接成功！！！", "测试AI网络", Messages.getInformationIcon());
            } catch (Exception ex) {
                Messages.showMessageDialog("错误！！！请更换AI配置参数！！！", "测试AI网络", Messages.getErrorIcon());
            }
        });
    }


    @Override
    public @Nullable DeepseekTestTool getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull DeepseekTestTool state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "DeepSeekTestTool";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "DeepSeekTestTool";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return jpanel;
    }

    @Override
    public void apply() throws ConfigurationException {
        deepSeekUserData.setApiKey(this.apiKey);
        deepSeekUserData.setBaseUrl(this.basUrl);
        deepSeekUserData.setTestFramework(this.testFramework);
        deepSeekUserData.setAiModel(this.aiModel);
    }

    @Override
    public void reset() {
        textField1.setText(deepSeekUserData.getBaseUrl());
        textField2.setText(deepSeekUserData.getApiKey());
        comboBox1.setSelectedItem(deepSeekUserData.getTestFramework());
        comboBox2.setSelectedItem(deepSeekUserData.getAiModel());
    }
}
