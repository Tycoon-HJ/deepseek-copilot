package org.hai.work.deepseekaitest.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
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
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author yinha
 */
public class CrateTestCodeAction extends AnAction {
    private static final Logger log = LoggerFactory.getLogger(CrateTestCodeAction.class);

    private static @NotNull File getFile(VirtualFile selectedFile) throws IOException {
        // ... (getFile 方法保持不变，它不涉及阻塞I/O)
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
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (selectedFile == null || !Objects.requireNonNull(selectedFile.getCanonicalFile()).getName().endsWith(".java")) {
            Messages.showMessageDialog(project, "Only supports generating unit tests for Java files", "DeepSeek Copilot", Messages.getWarningIcon());
            return;
        }

        DeepSeekUserData deepSeekUserData = ApplicationManager.getApplication().getService(DeepSeekUserData.class);
        if (AiUtil.checkAiIsAlready()) {
            return;
        }
        AiUtil.initAiChatModel();

        // **【关键修改点 1】**：准备在后台线程中使用的变量和结果容器
        // 使用 AtomicReference 存储后台操作的结果，因为它需要在两个线程间安全共享。
        final AtomicReference<String> contentRef = new AtomicReference<>();
        final AtomicReference<File> targetFileRef = new AtomicReference<>();

        Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
        if (document != null) {
            contentRef.set(document.getText());
        } else {
            // 如果无法获取文档，尝试直接读取文件内容 (这本身可能是一个小阻塞，但通常比网络I/O快得多)
            try {
                byte[] bytes = selectedFile.contentsToByteArray();
                contentRef.set(new String(bytes));
            } catch (IOException ex) {
                Messages.showMessageDialog(project, "无法读取文件内容：" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
                return;
            }
        }

        try {
            targetFileRef.set(getFile(selectedFile));
        } catch (IOException ex) {
            Messages.showMessageDialog(project, "创建测试文件错误：" + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
            return;
        }

        // ---------------------------------------------------------------------
        // **【关键修改点 2】**：使用 ProgressManager 将耗时操作移入后台
        // ---------------------------------------------------------------------

        // 存储AI生成的代码，以便在EDT中写入文件
        final AtomicReference<String> testCodeRef = new AtomicReference<>();

        // 定义需要在后台运行的任务
        Runnable backgroundTask = () -> {
            // 确保任务没有被用户取消 (ProgressManager.checkCanceled())
            ProgressManager.checkCanceled();

            String content = contentRef.get();
            if (content == null) return;

            try {
                // **【阻塞调用】**： AiUtil.getAiTestResult 现在在后台线程运行，不会冻结UI
                String testCode = AiUtil.getAiTestResult(content, deepSeekUserData.getTestFramework());
                testCodeRef.set(testCode);

            } catch (Exception ex) {
                // 如果出现错误，将错误信息传递给UI线程
                testCodeRef.set("ERROR: " + ex.getMessage());
                log.error("AI代码生成失败", ex);
            }
        };

        // 运行后台任务，并在UI上显示模态进度条
        boolean success = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                backgroundTask,
                "Generating Unit Test Code...", // 进度条标题
                true, // 可取消 (Cancellable)
                project
        );

        // ---------------------------------------------------------------------
        // **【关键修改点 3】**：处理后台任务的结果（已经回到EDT）
        // ---------------------------------------------------------------------

        // 只有当任务成功完成（没有被取消或抛出未捕获的运行时异常）时，success 才会是 true
        if (success && testCodeRef.get() != null) {
            String testCode = testCodeRef.get();
            File file = targetFileRef.get();

            if (file == null) {
                Messages.showMessageDialog("Could not locate target file.", "Generation Failed", Messages.getErrorIcon());
                return;
            }

            // 检查是否有后台错误信息
            if (testCode.startsWith("ERROR:")) {
                Messages.showMessageDialog(project, "Code generation error, error message is: " + testCode.substring(6), "DeepSeekTestCode", Messages.getErrorIcon());
                return;
            }

            // 写入文件（这个操作很快，可以留在EDT，或者也可以用ApplicationManager.getApplication().runWriteAction()包裹）
            try (FileWriter fw = new FileWriter(file.getAbsoluteFile(), false)) {
                fw.write(testCode);

                // 【通知用户】
                Messages.showMessageDialog("Test code has been successfully generated" + file.getName(), "Generation Success", Messages.getInformationIcon());
            } catch (Exception ex) {
                Messages.showMessageDialog(project, "写入文件错误，错误信息为: " + ex.getMessage(), "DeepSeekTestCode", Messages.getErrorIcon());
            }
        } else if (!success) {
            // 任务被用户取消或 ProgressManager 内部出错
            Messages.showMessageDialog("Unit test generation was cancelled by the user", "Generation Cancelled", Messages.getErrorIcon());

        }
    }
}