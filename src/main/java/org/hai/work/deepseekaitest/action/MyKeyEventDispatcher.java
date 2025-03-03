package org.hai.work.deepseekaitest.action;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.apache.commons.lang3.StringUtils;
import org.hai.work.deepseekaitest.codecompletion.CodeCompletionInlayHintsProvider;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * 键盘事件
 *
 * @author yinha
 */
public class MyKeyEventDispatcher implements KeyEventDispatcher {


    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int id = e.getID();

        if (id == KeyEvent.KEY_PRESSED) {
            if (keyCode == KeyEvent.VK_TAB && StringUtils.isNoneBlank(CodeCompletionInlayHintsProvider.code)) {
                Editor editor = CodeCompletionInlayHintsProvider.currenteditor;
                WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
                    // 获取编辑器的文档
                    Document document = editor.getDocument();
                    // 获取当前光标位置
                    int offset = editor.getCaretModel().getOffset();
                    // 在光标位置插入文字
                    document.insertString(offset, CodeCompletionInlayHintsProvider.code);
                });
            }
        }
        if (id == KeyEvent.KEY_RELEASED) {
            if (keyCode == KeyEvent.VK_TAB) {
                CodeCompletionInlayHintsProvider.code = "";
            }
        }
        return false;
    }
}