package org.hai.work.deepseekaitest.codecompletion;

import com.intellij.codeInsight.hints.ChangeListener;
import com.intellij.codeInsight.hints.ImmediateConfigurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class CodeCompletionImmediateConfigurable implements ImmediateConfigurable {
    @Override
    public @NotNull JComponent createComponent(@NotNull ChangeListener changeListener) {
        return new JComponent() {
            @Override
            public void setInheritsPopupMenu(boolean value) {
                super.setInheritsPopupMenu(value);
            }
        };
    }

    @Override
    public @NotNull String getMainCheckboxText() {
        return ImmediateConfigurable.super.getMainCheckboxText();
    }

    @Override
    public @NotNull List<Case> getCases() {
        return ImmediateConfigurable.super.getCases();
    }

    @Override
    public void reset() {
        ImmediateConfigurable.super.reset();
    }
}
