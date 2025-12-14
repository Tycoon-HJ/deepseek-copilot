package org.hai.work.deepseekaitest.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.hai.work.deepseekaitest.stream.AiEditorStreamWriter;
import org.hai.work.deepseekaitest.util.AiUtil;

import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletableFuture;

public class TriggerAiStreamingAction extends AnAction {

    // 在实际应用中，你可能需要一个机制来管理 AiEditorStreamWriter 的生命周期
    // 例如，通过一个 Project Service 来持有它，并在项目关闭时清理。
    // 这里为了示例简化，直接创建。
    private AiEditorStreamWriter currentStreamWriter;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            return; // 没有项目或编辑器，无法执行
        }
        if (AiUtil.checkAiIsAlready()) {
            return;
        }
        AiUtil.initAiChatModel();
        SelectionModel selectionModel = editor.getSelectionModel();
        // 检查是否有选中的文本
        if (selectionModel.hasSelection()) {
            // 获取选中的文本
            String selectedText = selectionModel.getSelectedText();
            if (selectedText != null) {
                // 停止任何正在进行的流，防止重复启动
                if (currentStreamWriter != null && currentStreamWriter.isStreaming()) {
                    currentStreamWriter.stopStreaming();
                }
                // 获取选中区域的起始偏移
                int startOffset = selectionModel.getSelectionEnd();
                // 获取文档对象
                Document document = editor.getDocument();
                // 根据偏移量获取对应的行号（0-based）
                int lineNumber = document.getLineNumber(startOffset);
                // 创建并初始化写入器
                currentStreamWriter = new AiEditorStreamWriter(project, editor);
                CompletableFuture.supplyAsync(() -> AiUtil.generateCodeStream(selectedText))
                        .thenAccept(result -> ApplicationManager.getApplication().invokeLater(() -> {
                            // 在 EDT 中安全启动流式写入
                            currentStreamWriter.startStreaming(result, lineNumber);
                        }));
            }
        } else {
            Messages.showInfoMessage("Please select the question you wish to inquire about🙏", "No Issues Found Yet");

        }
    }
}