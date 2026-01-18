package com.bingbaihanji.bfxt.test;

import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Aero Snap 功能测试示例
 *
 * <p>
 * 此示例展示了如何正确使用 enableAeroSnap 和 extendFrameIntoClientArea 方法。
 * </p>
 *
 * <p><b>测试说明：</b></p>
 * <ul>
 *   <li>方案1: 使用标准窗口样式（显示系统标题栏）- 立即生效</li>
 *   <li>方案2: 使用无边框样式（自定义标题栏）- 需要额外配置</li>
 *   <li>方案3: 完全沉浸式（扩展到标题栏）- 需要透明背景</li>
 * </ul>
 *
 * @author bingbaihanji
 * @date 2026-01-14
 */
public class AeroSnapTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #F0F0F0;");

        // 标题
        Label titleLabel = new Label("Aero Snap 功能测试");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 说明
        Label descLabel = new Label("请尝试将窗口拖动到屏幕边缘，测试 Aero Snap 功能：");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        Label instructionLabel = new Label(
                "• 拖动到左边缘：窗口应该占据左半屏幕\n" +
                "• 拖动到右边缘：窗口应该占据右半屏幕\n" +
                "• 拖动到顶部：窗口应该最大化\n" +
                "• Win + 左/右箭头：也可以使用快捷键测试"
        );
        instructionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        // 方案1: 标准窗口样式（最简单）
        Button standardButton = new Button("方案1: 启用标准窗口 Aero Snap");
        standardButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px; -fx-pref-height: 40px;");
        standardButton.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                FXNativeWindowsTools.enableAeroSnap(hwnd);
                showInfo("已启用标准窗口 Aero Snap\n窗口将显示 Windows 标题栏和边框");
            }
        });

        // 方案2: 无边框窗口样式（适合自定义标题栏）
        Button borderlessButton = new Button("方案2: 启用无边框 Aero Snap");
        borderlessButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px; -fx-pref-height: 40px;");
        borderlessButton.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                FXNativeWindowsTools.enableAeroSnapForBorderless(hwnd);
                showInfo("已启用无边框 Aero Snap\n窗口将保持无边框外观，但支持贴边吸附");
            }
        });

        // 方案3: 完全沉浸式（扩展到标题栏）
        Button immersiveButton = new Button("方案3: 启用沉浸式窗口");
        immersiveButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 300px; -fx-pref-height: 40px;");
        immersiveButton.setOnAction(e -> {
            WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
            if (hwnd != null) {
                // 步骤1: 启用无边框 Aero Snap（添加必要的窗口样式）
                FXNativeWindowsTools.enableAeroSnapForBorderless(hwnd);

                // 步骤2: 扩展框架到客户区
                FXNativeWindowsTools.extendFrameIntoClientArea(hwnd);

                // 步骤3: 设置透明背景（可选，但建议使用）
                Scene scene = primaryStage.getScene();
                if (scene != null) {
                    scene.setFill(null);
                }

                showInfo("已启用沉浸式窗口\n" +
                        "窗口框架已扩展到客户区\n" +
                        "Scene 背景已设置为透明");
            }
        });

        // 恢复按钮
        Button resetButton = new Button("恢复默认窗口样式");
        resetButton.setStyle("-fx-font-size: 12px; -fx-pref-width: 200px; -fx-text-fill: #666;");
        resetButton.setOnAction(e -> {
            // 关闭当前窗口，重新启动
            primaryStage.close();
            Platform.runLater(() -> {
                try {
                    start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        // 状态标签
        Label statusLabel = new Label("状态: 等待操作");
        statusLabel.setId("statusLabel");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #007AFF; -fx-padding: 10px;");

        root.getChildren().addAll(
                titleLabel,
                descLabel,
                instructionLabel,
                standardButton,
                borderlessButton,
                immersiveButton,
                resetButton,
                statusLabel
        );

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setTitle("Aero Snap 测试");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showInfo(String message) {
        // 在控制台打印信息
        System.out.println("=".repeat(50));
        System.out.println(message);
        System.out.println("=".repeat(50));

        // 可选: 更新UI上的状态标签
        Platform.runLater(() -> {
            Stage stage = (Stage) Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage && window.isShowing())
                    .findFirst()
                    .orElse(null);
            if (stage != null && stage.getScene() != null) {
                Label statusLabel = (Label) stage.getScene().lookup("#statusLabel");
                if (statusLabel != null) {
                    statusLabel.setText("状态: " + message.split("\n")[0]);
                }
            }
        });
    }
}
