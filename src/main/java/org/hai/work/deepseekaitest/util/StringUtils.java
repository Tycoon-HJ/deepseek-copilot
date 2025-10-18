package org.hai.work.deepseekaitest.util;

import java.util.stream.Collectors;

public class StringUtils {

    public static String addIndentation(String text) {
        return text.lines()
                .map(line -> "    " + line) // 每行前添加 4 个空格
                .collect(Collectors.joining("\n"));
    }
}
