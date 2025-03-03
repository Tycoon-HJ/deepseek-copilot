package org.hai.work.deepseekaitest.codecompletion;

import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class CodeCompletionInlayRenderer implements InlayPresentation {


    private final String hintText;
    private final Editor editor;

    public CodeCompletionInlayRenderer(String hintText, Editor editor) {
        this.hintText = hintText;
        this.editor = editor;
    }

    @Override
    public int getWidth() {
        return editor.getContentComponent().getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, editor.getColorsScheme().getEditorFontSize())).stringWidth(hintText);
    }

    @Override
    public int getHeight() {
        return editor.getContentComponent().getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, editor.getColorsScheme().getEditorFontSize())).getHeight();

    }

    @Override
    public void paint(@NotNull Graphics2D graphics2D, @NotNull TextAttributes textAttributes) {
        graphics2D.setColor(new JBColor(new Color(128, 128, 128, 128), new Color(128, 128, 128, 128)));
        graphics2D.setFont(new Font(Font.MONOSPACED, Font.PLAIN, editor.getColorsScheme().getEditorFontSize()));
        graphics2D.drawString(hintText, 0, graphics2D.getFontMetrics().getAscent());
    }

    @Override
    public void fireSizeChanged(@NotNull Dimension dimension, @NotNull Dimension dimension1) {

    }

    @Override
    public void fireContentChanged(@NotNull Rectangle rectangle) {

    }

    @Override
    public void addListener(@NotNull PresentationListener presentationListener) {

    }

    @Override
    public void removeListener(@NotNull PresentationListener presentationListener) {

    }
}
