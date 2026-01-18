# FXWindowEmbedTools - JavaFX Custom Window Style Tool

A Windows-specific custom window styling tool built with **JavaFX + JNA**, designed exclusively for older JavaFX
versions. (Newer JavaFX versions include the built-in `HeaderBar` class and related APIs, which support direct window
status bar styling.)

## Project Introduction

# FXWindowEmbedTools - JavaFX Custom Window Style Tool

A Windows-specific custom window styling tool built with **JavaFX + JNA**, designed exclusively for older JavaFX
versions. (For newer JavaFX versions, the built-in `HeaderBar` class and related APIs are available to style the window
status bar directly.)

## Usage

There are two ways to use this tool, depending on your styling needs:

1. Inherit the `AbstractCustomWindow` class and implement the abstract method:
   `protected abstract Parent createContent();`

2. Inherit the `DefaultLayout` class (with pre-defined default styles) and implement the abstract method:
   `protected abstract Node createNode();`

## Project Introduction

This tool leverages JNA to call Windows Native APIs, breaking through the styling limitations of JavaFX's default
windows. It enables modern window visual effects for applications developed with older JavaFX versions.

## Core Features

The core class `FXWindowEmbedTools` provides the following key capabilities:

- Set window dark theme (compatible with Windows system dark mode)

- Implement Acrylic semi-transparent frosted glass effect

- Customize window rounded corner styles

- Other custom configurations for Windows native window states/styles

## Applicable Scenarios

- Desktop applications developed with **older JavaFX versions**

- Scenarios requiring custom Windows window visual styles (instead of default native styles)

- JavaFX projects needing adaptation to system dark themes and Acrylic effects

## Notes

1. Windows-only support (relies on Windows Native APIs)

2. For newer JavaFX versions, it is recommended to use official native APIs like `HeaderBar` first, without relying on
   this tool

3. Ensure JNA-related dependencies are included in the project before use

中文简介:
javafx + jna 自定义窗口样式
适合低版本javafx使用(高版本有 HeaderBar 类及其相关接口 可以直接设置窗口状态栏的样式)

使用方法:
继承 AbstractCustomWindow 类实现 protected abstract Parent createContent(); 方法即可
或者 继承 DefaultLayout 类(有默认样式) 实现 protected abstract Node createNode(); 方法即可

FXWindowEmbedTools 类有提供通过jna调用windows的native层的api设置窗口状态的相关方法
比如设置暗色主题,亚克力样式,圆角设计等