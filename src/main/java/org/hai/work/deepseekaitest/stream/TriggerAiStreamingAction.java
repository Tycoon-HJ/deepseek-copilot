package org.hai.work.deepseekaitest.stream;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.hai.work.deepseekaitest.util.AiUtil;
import reactor.core.publisher.Flux;

import javax.validation.constraints.NotNull;

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
        if (!AiUtil.checkAiIsAlready()) {
            return;
        }
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
                // 创建并初始化写入器
                currentStreamWriter = new AiEditorStreamWriter(project, editor);
                // 获取AI输出的Flux流
                Flux<String> aiOutputFlux = AiUtil.generateCodeStream(selectedText);
                // 启动流式写入
                currentStreamWriter.startStreaming(aiOutputFlux);
            }
        } else {
            Messages.showInfoMessage("请选中要询问的问题🙏", "暂无找到问题");
        }
    }
    // IMPORTANT: 在实际插件中，你需要在 Project lifecycle 或 Editor lifecycle
    // 中调用 stopStreaming() 来清理资源。
    // 例如，如果你的 AiEditorStreamWriter 实例与 Editor 关联，
    // 你可以在 Editor 监听器中检测到 Editor 关闭并调用 stopStreaming()。
    // 如果与 Project 关联，可以在 Project Service 的 dispose 方法中调用。
}