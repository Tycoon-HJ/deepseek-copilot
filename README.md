# 🌌 DeepSeek Copilot for IntelliJ IDEA [中文](https://github.com/Tycoon-HJ/deepseek-copilot/blob/master/README-CN.md)/ [English](https://github.com/Tycoon-HJ/deepseek-copilot/blob/master/README.md)

> Let AI be your programming copilot – instantly generate unit tests & implement functions from comments!

---

## 🚀 Introduction

**DeepSeek Copilot** is an AI-powered assistant plugin for IntelliJ IDEA that intelligently analyzes your code and automatically generates high-quality unit tests. It also supports comment-driven code generation, greatly boosting your development efficiency and code quality.

Whether you're struggling with low test coverage or getting stuck on complex logic, DeepSeek Copilot has you covered — so you can focus on what truly matters: creative development.

---

## ✨ Key Features

### 🔍 AI-Powered Unit Test Generator
- One-click generation of unit test code
- Automatically detects function dependencies and input/output
- Supports intelligent reasoning for complex logic and assertion building
- Compatible with popular mocking frameworks like Mockito

### 🧠 Comment-Driven Code Generation
- Generate Java method bodies from natural language comments
- Understands class context and structure for logical implementation
- Accurately handles parameters, return values, and exception cases

### 🌊 Real-Time Streaming Feedback
- Displays AI suggestions word-by-word inside Inlay Hints
- Immersive interaction with AI, no context switching needed

### 🛠️ Native Integration with IntelliJ IDEA
- Seamless embedding into the IDEA environment
- Quick access via right-click menu or keyboard shortcuts
- **Shortcuts**:
    - macOS: `Control + Command (⌘) + /`
    - Windows: `Ctrl + Alt + /`
    - Or right-click on a comment to trigger actions

---

## 📦 Installation

1. Open IntelliJ IDEA and go to `Settings > Plugins`
2. Search for **DeepSeek Copilot**
3. Click "Install" and restart IDEA to start using!

Or download the latest version from the [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/).

---

## 🧪 How to Use

### 1. Generate Unit Tests

- Right-click on any Java method and select `Generate Unit Test with DeepSeek Copilot`
- Choose the desired testing framework (JUnit 4/5, Mockito, etc.)
- AI will analyze the logic and generate appropriate test code, displayed via Inlay Hints or in a new file

### 2. Generate Code from Comments

- Write a descriptive comment above a method (e.g., `// Calculate the number of days between two dates`)
- Right-click the comment and select `Generate Code from Comment`
- AI will generate the method body and insert it at the appropriate location

---

## 🧠 Technical Stack

This plugin is built on top of [Spring AI](https://docs.spring.io/spring-ai/reference/), leveraging modern reactive technologies like Flux for smooth, real-time content streaming.  
It supports large language models (LLMs) such as DeepSeek, OpenAI, Ollama, and allows custom model integration via API keys.

---

## 🛡️ Privacy & Security

We **do not upload or store any of your code**. All requests are preprocessed locally and securely transmitted over HTTPS to the AI backend. Your personal and enterprise code remains private and safe.

---

## 💬 Join the Community

- 🤝 GitHub: [https://github.com/Tycoon-HJ/deepseek-copilot](https://github.com/Tycoon-HJ/deepseek-copilot)
- 📢 Feel free to open issues or suggest features
- 🌐 Future versions will support prompt templates, custom context menus, UML understanding, and more!

---

## ❤️ From the Developer

We believe AI should be more than just a cold autocomplete tool — it should be your creative companion.  
**DeepSeek Copilot** is your trusty copilot in exploring the universe of code. Expect continuous evolution, and limitless possibilities ahead!
