# 🌌 DeepSeek Copilot for IntelliJ IDEA

> 让 AI 成为你的编程副驾驶，极速生成单元测试 & 代码注释驱动开发！

---

## 🚀 简介

**DeepSeek Copilot** 是一款为 IntelliJ IDEA 打造的 AI 编程助手插件，它能够智能分析你的代码逻辑，自动生成高质量的单元测试，并支持根据注释生成完整函数代码，全面提升开发效率和代码质量。

无论是测试覆盖率不足，还是复杂逻辑难以起笔，从现在开始，都交给 DeepSeek Copilot —— 让你把精力专注于更有创造力的开发任务！

---

## ✨ 插件亮点

### 🔍 AI 单元测试生成器
- 一键生成 单元测试代码
- 自动识别函数依赖和输入输出
- 支持复杂逻辑的智能推理与断言构建
- 支持 Mockito 等主流 Mock 框架

### 🧠 注释驱动代码生成
- 根据自然语言注释智能生成 Java 函数体
- 理解上下文和类结构，生成符合逻辑的实现代码
- 支持参数、返回值、异常处理的精确推理

### 🌊 实时流式反馈体验
- 在 Inlay Hint 中逐字展示 AI 提示内容
- 沉浸式 AI 交互，无需跳转视图

### 🛠️ 原生集成 IDEA 开发环境
- 无缝嵌入 IntelliJ IDEA
- 右键菜单 + 快捷键操作快速调用
- 快捷键Mac用户`Control + Command (⌘) + /` Win用户`Ctrl + Alt + /` 或者选择注释点击右键

---

## 📦 安装方式

1. 打开 IntelliJ IDEA，进入 `Settings > Plugins`
2. 搜索 **DeepSeek Copilot**
3. 点击安装，重启 IDEA 即可开始使用！

或从 [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/) 下载最新版本。

---

## 🧪 使用方式

### 1. 生成单元测试

- 在任意 Java 函数上右键，选择 `Generate Unit Test with DeepSeek Copilot`
- 选择目标测试框架（JUnit 4/5、Mockito 等）
- AI 将自动分析方法逻辑并生成测试代码，展示于 Inlay 提示或新文件中

### 2. 根据注释生成代码

- 在函数定义处编写注释（如：`// 计算两个日期之间的天数`）
- 在注释上右键选择 `Generate Code from Comment`
- AI 将自动生成合理的函数实现，插入到目标位置

---

## 🧠 技术支持

本插件基于 [Spring AI](https://docs.spring.io/spring-ai/reference/) 构建，使用现代 Reactive 技术（如 Flux 流式流）实现自然流畅的内容生成体验。后端接入强大的 LLM 引擎（如 DeepSeek、OpenAI、Ollama 等），并支持用户自定义模型或 API Key。

---

## 🛡️ 隐私与安全

我们 **绝不会上传或存储用户代码**，所有请求默认本地预处理并通过 HTTPS 加密发送至 LLM 服务，确保企业与个人代码的绝对安全。

---

## 💬 加入社区

- 🤝 GitHub: [https://github.com/Tycoon-HJ/deepseek-copilot](https://github.com/your-org/deepseek-copilot)
- 📢 Issue 反馈或功能建议欢迎随时提出
- 🌐 后续版本将支持 Prompt 模板、Context Menu 自定义、类图理解等高级功能！

---

## ❤️ 开发者的话

我们相信，AI 不应该只是冷冰冰的代码补全器，而是你创作过程中的灵感共鸣者。  
**DeepSeek Copilot** 是你探索代码宇宙的得力副驾驶，未来版本将不断进化，与你一同探索无限可能。
