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
import org.hai.work.deepseekaitest.util.AiUtil;
import org.jetbrains.annotations.NotNull;

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
            Messages.showMessageDialog("仅支持生成Java文件的单元测试", "DeepSeek Copilot", Messages.getWarningIcon());
            return;
        }
        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
        if (!AiUtil.checkAiIsAlready()) {
            return;
        }
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
                    String testCode = AiUtil.getAiTestResult(content, deepSeekUserData.getTestFramework());
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
