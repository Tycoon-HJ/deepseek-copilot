package org.hai.work.deepseekaitest.util;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class EditorWriteExample {

    /**
     * 在指定行区间之间插入多行文本
     *
     * @param project 当前项目
     * @param editor  当前编辑器
     * @param endLine 结束行（0-based）
     * @param text    要插入的文本（可以是多行）
     */
    public static void insertTextBetweenLines(Project project, Editor editor, int endLine, String text) {
        Document document = editor.getDocument();
        int insertOffset = document.getLineEndOffset(endLine);
        WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(insertOffset, "\n" + text));
    }
}
