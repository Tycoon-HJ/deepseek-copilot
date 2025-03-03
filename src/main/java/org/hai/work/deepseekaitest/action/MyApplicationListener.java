package org.hai.work.deepseekaitest.action;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.wm.IdeFrame;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import java.awt.*;

/**
 * @author yinha
 */
public class MyApplicationListener implements ApplicationActivationListener {
    private final KeyEventDispatcher dispatcher = new MyKeyEventDispatcher();

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    }

    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }
}