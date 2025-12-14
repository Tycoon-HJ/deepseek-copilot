package org.hai.work.deepseekaitest.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.hai.work.deepseekaitest.data.DeepSeekUserData;
import org.hai.work.deepseekaitest.util.AiUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * @author yinha
 */
public class CrateTestCodeAction extends AnAction {
    private static final Logger log = LoggerFactory.getLogger(CrateTestCodeAction.class);

    private static @NotNull File getFile(VirtualFile selectedFile) throws IOException {
        String srcPath = selectedFile.getPath();
        String targetPath = srcPath.replace("src/main/java", "src/test/java");
        String srcName = Objects.requireNonNull(selectedFile.getCanonicalFile()).getName();
        String targetFile = targetPath.replace(srcName, srcName.split("\\.")[0] + "Test.java");
        File file = new File(targetFile);
        if (!file.exists()) {
            File fileParent = file.getParentFile();
            if (!fileParent.exists()) {
                boolean mkDirs = fileParent.mkdirs();
                log.info("创建目录结果：{}", mkDirs);
            }
            boolean newFile = file.createNewFile();
            log.info("创建文件结果：{}", newFile);
        }
        return file;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 获取当前选中的虚拟文件
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selectedFile != null && !Objects.requireNonNull(selectedFile.getCanonicalFile()).getName().endsWith(".java")) {
            Messages.showMessageDialog("Only supports generating unit tests for Java files", "DeepSeek Copilot", Messages.getWarningIcon());
            return;
        }
        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
        if (AiUtil.checkAiIsAlready()) {
            return;
        }
        AiUtil.initAiChatModel();
        try {
            if (selectedFile != null) {
                // 获取文件对应的文档
                Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
                if (document != null) {
                    // 获取文档内容
                    String content = document.getText();
                    // 创建Test文件夹和对应的测试类
                    File file = getFile(selectedFile);
                    // 开启追加模式
                    try (FileWriter fw = new FileWriter(file.getAbsoluteFile(), false)) {
                        // 模拟生成单元测试
                        String testCode = AiUtil.getAiTestResult(content, deepSeekUserData.getTestFramework());
                        System.out.println(testCode);
                        fw.write(testCode);
                        Notifications.Bus.notify(new Notification("Print", "", "Test code has been successfully generated～", NotificationType.INFORMATION), e.getProject());
                    } catch (Exception ex) {
                        Messages.showMessageDialog("Code generation error, error message is" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
                    }
                } else {
                    // 如果无法获取文档，则尝试直接读取文件内容
                    byte[] bytes = selectedFile.contentsToByteArray();
                    String content = new String(bytes);
                    System.out.println("文件内容：\n" + content);
                }
            }
        } catch (IOException ex) {
            Messages.showMessageDialog("生成代码错误错误，错误信息为" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
        }
    }
}
