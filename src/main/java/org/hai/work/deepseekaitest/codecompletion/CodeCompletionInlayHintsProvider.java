package org.hai.work.deepseekaitest.codecompletion;

import com.intellij.codeInsight.hints.*;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.hai.work.deepseekaitest.util.AiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.ollama.OllamaChatModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CodeCompletionInlayHintsProvider implements InlayHintsProvider {

    private static final SettingsKey<NoSettings> KEY = new SettingsKey<>("code.completion.inlay");
    public static String code = "";
    public static Editor currenteditor;
    // 创建一个单线程的执行器,否则会开启10个线程进程调AI模型，造成性能浪费
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private String lastGenerationCode = "";

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getName() {
        return "code completion inlay hints";
    }

    @Override
    public @Nullable InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull Object o, @NotNull InlayHintsSink inlayHintsSink) {

        return new InlayHintsCollector() {
            private String lastGenerationCode = "";
            private int lastGenerationOffset = -1;

            @Override
            public boolean collect(@NotNull PsiElement psiElement, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
                Future<Boolean> submit = singleThreadExecutor.submit(() -> {
                    if (StringUtils.isNotBlank(lastGenerationCode)) {
                        return false;
                    }

                    Document document = editor.getDocument();
                    int offset = editor.getCaretModel().getOffset();
                    String srcText = document.getText(new TextRange(0, offset));

                    if (!srcText.endsWith("--ai")) {
                        lastGenerationCode = "";
                        lastGenerationOffset = -1;
                        return false;
                    }

                    String generateCode = generateCode(srcText);
                    if (generateCode.equals(lastGenerationCode) && offset == lastGenerationOffset) {
                        return false;
                    }
                    code = generateCode;
                    currenteditor = editor;
                    lastGenerationCode = generateCode;
                    lastGenerationOffset = offset;
                    editor.getInlayModel().getInlineElementsInRange(0, document.getTextLength()).forEach(Inlay::dispose);

                    if (!generateCode.isEmpty()) {
                        inlayHintsSink.addInlineElement(offset, false, new CodeCompletionInlayRenderer(generateCode, editor), false);
                    }
                    return false;
                });
                try {
                    return submit.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private String generateCode(String textBeforeCrate) {
        if (textBeforeCrate.endsWith("--ai")) {
            OllamaChatModel ollamaChatModel = AiUtil.gainOllamaChatModelInstance();
            String prompt = textBeforeCrate + "，需要一个简洁、易于理解且注释详细的Java函数，只返回代码即可";
            String call = ollamaChatModel.call(prompt + textBeforeCrate);
            System.out.println(Thread.currentThread().getName() + "==========" + call);
            return call.split("```java")[1].split("```")[0];
        }
        return "";
    }

    @Override
    public @NotNull Object createSettings() {
        return new NoSettings();
    }

    @Override
    public @NotNull SettingsKey getKey() {
        return KEY;
    }

    @Override
    public @Nullable String getPreviewText() {
        return "fun xxxxxxxx";
    }

    @Override
    public @NotNull ImmediateConfigurable createConfigurable(@NotNull Object o) {
        return null;
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return true;
    }

}
