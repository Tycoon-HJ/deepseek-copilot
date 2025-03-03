package org.hai.work.deepseekaitest;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.hai.work.deepseekaitest.data.DeepSeekUserData;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * @author yinha
 */
public class CrateTestCodeMain extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前选中的虚拟文件
        VirtualFile selectedFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (!selectedFile.getCanonicalFile().getName().endsWith(".java")) {
            Messages.showMessageDialog("仅支持生成Java文件的单元测试", "DeepSeekTestCode", Messages.getWarningIcon());
            return;
        }
        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);

        if (deepSeekUserData == null || Objects.isNull(deepSeekUserData.getBaseUrl()) || Objects.isNull(deepSeekUserData.getApiKey()) || Objects.isNull(deepSeekUserData.getAiModel()) || Objects.isNull(deepSeekUserData.getTestFramework())) {
            Messages.showMessageDialog("未加载到AI相关配置，请先在设置中进行AI参数配置！", "DeepSeekTestCode", Messages.getWarningIcon());
            return;
        }

        OpenAiApi openAiApi = new OpenAiApi(deepSeekUserData.getBaseUrl(), deepSeekUserData.getApiKey());
        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(deepSeekUserData.getAiModel()).temperature(0.2).maxTokens(200000).build();
        String prompt = """
                你是一个非常优秀的Java单元测试专家，请你根据输入的Java代码，注释掉的代码不需要生成单元测试,其他的代码请覆盖所有的分支，并使用""" + deepSeekUserData.getTestFramework() + "框架进行返回对应的单元测试，只返回代码即可";
        ChatModel chatModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
        try {
            // 获取文件对应的文档
            Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
            if (document != null) {
                // 获取文档内容
                String content = document.getText();
                // 创建Test文件夹和对应的测试类
                String srcPath = selectedFile.getPath();
                String targetPath = srcPath.replace("src/main/java", "src/test/java");
                String srcName = Objects.requireNonNull(selectedFile.getCanonicalFile()).getName();
                String targetFile = targetPath.replace(srcName, srcName.split("\\.")[0] + "Test.java");
                File file = new File(targetFile);
                if (!file.exists()) {
                    File fileParent = file.getParentFile();
                    if (!fileParent.exists()) {
                        fileParent.mkdirs();
                    }
                    file.createNewFile();
                }
                // 开启追加模式
                try (FileWriter fw = new FileWriter(file.getAbsoluteFile(), false)) {
                    // 模拟生成单元测试
                    String result = chatModel.call(prompt + content);
                    String testCode = result.split("```java")[1].split("```")[0];
                    fw.write(testCode);
                    Notifications.Bus.notify(new Notification("Print", "", "已成功生成测试代码！！！", NotificationType.INFORMATION), e.getProject());
                } catch (Exception ex) {
                    Messages.showMessageDialog("生成代码错误错误，错误信息为" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
                }
            } else {
                // 如果无法获取文档，则尝试直接读取文件内容
                byte[] bytes = selectedFile.contentsToByteArray();
                String content = new String(bytes);
                System.out.println("文件内容：\n" + content);
            }
        } catch (IOException ex) {
            Messages.showMessageDialog("生成代码错误错误，错误信息为" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
        }
    }
}
