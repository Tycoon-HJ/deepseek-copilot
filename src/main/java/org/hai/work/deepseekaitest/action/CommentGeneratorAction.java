package org.hai.work.deepseekaitest.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.hai.work.deepseekaitest.util.AiUtil;
import org.hai.work.deepseekaitest.util.EditorWriteExample;
import org.hai.work.deepseekaitest.util.StringUtils;
import org.jetbrains.annotations.NotNull;

public class CommentGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 选中整个方法，在方法上生成注释
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            return; // 没有项目或编辑器，无法执行
        }
        if (AiUtil.checkAiIsAlready()) {
            return;
        }
        AiUtil.initOpenAiChatModel();
        SelectionModel selectionModel = editor.getSelectionModel();
        // 检查是否有选中的文本
        if (selectionModel.hasSelection()) {
            // 获取选中的文本
            String selectedText = selectionModel.getSelectedText();
            if (selectedText != null) {
                // 获取选中区域的起始偏移
                int startOffset = selectionModel.getSelectionStart();
                // 获取文档对象
                Document document = editor.getDocument();
                // 根据偏移量获取对应的行号（0-based）
                int lineNumber = document.getLineNumber(startOffset);

                String textToInsert = AiUtil.getCodeComment(selectedText);
                EditorWriteExample.insertTextBetweenLines(project, editor, lineNumber - 1, StringUtils.addIndentation(textToInsert));
            }
        } else {
            Messages.showInfoMessage("请选中要询问的问题🙏", "暂无找到问题");
        }
    }
}