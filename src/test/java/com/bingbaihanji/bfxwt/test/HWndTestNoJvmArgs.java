package com.bingbaihanji.bfxwt.test;

import com.bingbaihanji.bfxwt.tools.FXNativeWindowsTools;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 演示不使用 JVM 参数获取窗口句柄的三种方法
 *
 * <p><b>重要：</b>此测试类不需要任何 JVM 参数即可运行！</p>
 *
 * @author bingbaihanji
 * @date 2026-01-13
 */
public class HWndTestNoJvmArgs extends Application {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  JavaFX 窗口句柄获取测试 - 无需 JVM 参数版本              ║");
        System.out.println("║                                                            ║");
        System.out.println("║  本程序演示三种不需要 JVM 参数的窗口句柄获取方法：        ║");
        System.out.println("║  1. 通过窗口标题获取 (getHWndByTitle)                     ║");
        System.out.println("║  2. 通过枚举窗口获取 (getHWndByEnumeration) - 推荐        ║");
        System.out.println("║  3. 获取所有窗口 (getAllHWndByEnumeration)                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20;");

        Label titleLabel = new Label("JavaFX 窗口句柄获取测试（无需 JVM 参数）");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label hwndLabel = new Label("窗口句柄: 尚未获取");
        hwndLabel.setStyle("-fx-font-size: 14px;");

        // 方法1：通过窗口标题获取
        Button method1Button = new Button("方法1: 通过窗口标题获取句柄");
        method1Button.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByTitle(primaryStage);
            if (hwnd != null) {
                hwndLabel.setText("窗口句柄: 0x" + Long.toHexString(Pointer.nativeValue(hwnd.getPointer())));
                System.out.println("✓ 方法1成功: 通过标题 \"" + primaryStage.getTitle() + "\" 获取到句柄");
            } else {
                hwndLabel.setText("获取失败");
                System.err.println("✗ 方法1失败: 未找到窗口");
            }
        });

        // 方法2：通过枚举窗口获取
        Button method2Button = new Button("方法2: 通过枚举进程窗口获取句柄");
        method2Button.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                hwndLabel.setText("窗口句柄: 0x" + Long.toHexString(Pointer.nativeValue(hwnd.getPointer())));
                System.out.println("✓ 方法2成功: 通过枚举窗口获取到句柄");
            } else {
                hwndLabel.setText("获取失败");
                System.err.println("✗ 方法2失败: 未找到窗口");
            }
        });

        // 方法3：获取所有窗口
        Button method3Button = new Button("方法3: 获取所有 JavaFX 窗口句柄");
        method3Button.setOnAction(e -> {
            var hwnds = FXNativeWindowsTools.getAllHWndByEnumeration();
            if (!hwnds.isEmpty()) {
                StringBuilder sb = new StringBuilder("找到 " + hwnds.size() + " 个窗口:\n");
                for (int i = 0; i < hwnds.size(); i++) {
                    sb.append("窗口").append(i + 1).append(": 0x")
                            .append(Long.toHexString(Pointer.nativeValue(hwnds.get(i).getPointer())))
                            .append("\n");
                }
                hwndLabel.setText(sb.toString());
                System.out.println("✓ 方法3成功: 找到 " + hwnds.size() + " 个窗口");
            } else {
                hwndLabel.setText("未找到窗口");
                System.err.println("✗ 方法3失败: 未找到窗口");
            }
        });

        // 测试透明度功能
        Button alphaButton = new Button("设置窗口透明度 (70%)");
        alphaButton.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                // 设置透明度为 70% (0.7)
                FXNativeWindowsTools.setWindowAlpha(hwnd, 0.7f);
                System.out.println("✓ 已设置窗口透明度为 70%");
            }
        });

        Button resetAlphaButton = new Button("恢复窗口不透明");
        resetAlphaButton.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                FXNativeWindowsTools.setWindowAlpha(hwnd, 1.0f);
                System.out.println("✓ 已恢复窗口不透明");
            }
        });

        root.getChildren().addAll(
                titleLabel,
                hwndLabel,
                method1Button,
                method2Button,
                method3Button,
                alphaButton,
                resetAlphaButton
        );

        // 设置唯一的窗口标题（方法1需要）
        String uniqueTitle = "JavaFX HWND Test - " + System.currentTimeMillis();
        primaryStage.setTitle(uniqueTitle);
        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();

        // 窗口显示后自动测试方法2
        Platform.runLater(() -> {
            System.out.println("\n========== 自动测试开始 ==========");
            System.out.println("窗口标题: " + uniqueTitle);

            // 测试方法2（推荐方法，无需特殊标题）
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                long hwndValue = Pointer.nativeValue(hwnd.getPointer());
                hwndLabel.setText("窗口句柄: 0x" + Long.toHexString(hwndValue));
                System.out.println("✓ 自动获取成功！窗口句柄: 0x" + Long.toHexString(hwndValue));
            } else {
                System.err.println("✗ 自动获取失败");
            }
            System.out.println("========== 自动测试结束 ==========\n");
        });
    }
}
