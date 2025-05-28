package org.hai.work.deepseekaitest.stream;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * 负责将AI生成的文本Flux流动态写入到IntelliJ IDEA编辑器的Document中。
 * 遵循在EDT上和Write Action中修改Document的标准实践。
 */
public class AiEditorStreamWriter {

    @Nullable
    private Disposable currentSubscription; // 使用 @Nullable 注解，表示可能为 null

    private Project project;
    private Editor editor;
    private Document document;
    private CaretModel caretModel;

    // 记录AI输出开始插入时的Document offset
    private int startOffset;

    // 使用AtomicInteger来跟踪AI已插入的总文本长度
    // 它是线程安全的，因为Flux的回调可能来自不同的线程（虽然我们会切到EDT）
    // 但AtomicInteger确保了更新总长度时的原子性
    private final AtomicInteger currentTotalLength = new AtomicInteger(0);

    /**
     * 初始化写入器。
     *
     * @param project 当前的IDEA项目
     * @param editor  要写入的编辑器
     */
    public AiEditorStreamWriter(@NotNull Project project, @NotNull Editor editor) {
        this.project = project;
        this.editor = editor;
        this.document = editor.getDocument();
        this.caretModel = editor.getCaretModel();
    }

    /**
     * 开始订阅AI输出的Flux流，并将其内容写入编辑器。
     * 如果已经有一个流在进行，旧的流将被取消。
     *
     * @param aiOutputFlux AI输出的文本块流
     */
    public void startStreaming(@NotNull Flux<String> aiOutputFlux) {
        stopStreaming(); // 停止任何正在进行的流
        // 记录开始插入时的Document offset，这是AI输出的起始位置
        this.startOffset = caretModel.getOffset();
        this.currentTotalLength.set(0); // 重置总长度计数器
        // 订阅Flux流，并在EDT上处理每个文本块
        currentSubscription = aiOutputFlux
                // 确保后续的操作 (onNext, onError, onComplete) 在IntelliJ的EDT上执行
                // 这是因为Document修改必须在EDT上进行
                .publishOn(Schedulers.fromExecutor(AppExecutorUtil.getAppExecutorService()))
                .subscribe(
                        // onNext: 接收到新的文本块
                        chunk -> {
                            // 在EDT上，执行Write Action来修改Document
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                // 确保editor和document仍然有效 (例如，编辑器可能已经被关闭)
                                if (editor.isDisposed() || project.isDisposed()) {
                                    stopStreaming(); // 停止流，因为环境不再有效
                                    return;
                                }
                                try {
                                    // 计算当前插入位置：AI输出的起始位置 + 已经插入的总长度
                                    int currentOffset = startOffset + currentTotalLength.get();
                                    // 安全检查：确保插入位置在Document的有效范围内
                                    if (currentOffset < 0 || currentOffset > document.getTextLength()) {
                                        System.err.println("Attempted to insert at invalid offset: " + currentOffset + ". Document length: " + document.getTextLength());
                                        stopStreaming(); // 停止流以防意外
                                        return;
                                    }
                                    // 将文本块插入到Document
                                    document.insertString(currentOffset, chunk);
                                    // 更新已插入的总长度
                                    currentTotalLength.addAndGet(chunk.length());
                                    // 将光标移动到新插入文本的末尾，并滚动到可见区域
                                    // 这样做能让用户实时看到AI正在写入的内容
                                    int newCaretOffset = startOffset + currentTotalLength.get();
                                    caretModel.moveToOffset(newCaretOffset);
                                    editor.getScrollingModel().scrollToCaret(ScrollType.CENTER_DOWN);
                                } catch (Exception e) {
                                    // 插入过程中发生的异常处理 (例如，Document被外部修改导致offset无效等)
                                    System.err.println("Error inserting text chunk: " + e.getMessage());
                                    e.printStackTrace();
                                    // 停止流以防止进一步的问题
                                    stopStreaming();
                                    // 考虑在UI上显示错误信息给用户
                                }
                            });
                        },
                        // onError: 处理流中的错误
                        error -> {
                            System.err.println("AI streaming encountered an error: " + error.getMessage());
                            error.printStackTrace();
                            // 可以在UI上显示错误信息给用户，或者回滚已插入的部分文本（需要在Write Action中）
                            currentSubscription = null; // 错误发生，订阅结束
                        },
                        // onComplete: 流完成，没有更多文本块发出
                        () -> {
                            System.out.println("AI streaming finished successfully.");
                            // 可以在这里进行一些收尾工作，例如通知用户完成
                            currentSubscription = null; // 流完成，订阅结束
                        }
                );
    }

    /**
     * 取消当前的AI输出流订阅。
     * 如果流正在进行，这会停止接收新的文本块并写入编辑器。
     * 应该在编辑器关闭、插件卸载或用户取消等事件时调用此方法进行清理。
     */
    public void stopStreaming() {
        if (currentSubscription != null && !currentSubscription.isDisposed()) {
            currentSubscription.dispose();
            System.out.println("AI streaming subscription disposed.");
        }
        currentSubscription = null; // 确保引用被清除
    }

    /**
     * 检查是否有流正在进行。
     *
     * @return true 如果当前有活动的AI流写入器
     */
    public boolean isStreaming() {
        return currentSubscription != null && !currentSubscription.isDisposed();
    }
}