package com.bingbaihanji.bfxt.test;

import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 窗口类名验证测试
 *
 * <p>
 * 该测试用于验证 JavaFX 窗口的 Windows 类名。
 * 运行后会打印出当前进程所有窗口的详细信息，包括窗口类名。
 * </p>
 *
 * <p><b>验证 JavaFX 窗口类名的几种方法：</b></p>
 * <ol>
 *   <li>运行此程序，查看控制台输出</li>
 *   <li>使用 Windows Spy++ 工具（Visual Studio 自带）</li>
 *   <li>使用第三方工具如 WinSpy、AutoIt Window Info 等</li>
 * </ol>
 *
 * @author bingbaihanji
 * @date 2026-01-13
 */
public class WindowClassNameTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20;");

        Button printButton = new Button("打印所有窗口信息");
        printButton.setOnAction(e -> {
            FXNativeWindowsTools.printAllWindowsInfo();
        });

        root.getChildren().add(printButton);

        primaryStage.setTitle("JavaFX 窗口类名验证工具");
        primaryStage.setScene(new Scene(root, 400, 200));
        primaryStage.show();

        // 窗口显示后自动打印一次
        Platform.runLater(() -> {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║          JavaFX 窗口类名验证                               ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            FXNativeWindowsTools.printAllWindowsInfo();

            System.out.println("✓ 如你所见，JavaFX 窗口的类名确实是 \"GlassWndClass\" 开头");
            System.out.println("✓ 完整格式通常为: GlassWndClass-GlassWindowClass-N (N 是数字)\n");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
