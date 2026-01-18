package com.bingbaihanji.bfxt.test;

import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import static com.bingbaihanji.bfxt.tools.FXNativeWindowsTools.isWindowMaximized;

/**
 *
 * @author bingbaihanji
 * @date 2026-01-13 11:40:25
 * @description //TODO
 */
public class AppTest extends Application {

    private Boolean flag = false;

    @Override
    public void start(Stage primaryStage) throws Exception {

        var root = new AnchorPane();

        Button mode = new Button("模式切换");
        Button max = new Button("最大化");

        root.getChildren().addAll(mode, max);

        AnchorPane.setTopAnchor(mode, 40.0);
        AnchorPane.setTopAnchor(max, 40.0);

        AnchorPane.setLeftAnchor(mode, 0.0);
        AnchorPane.setLeftAnchor(max, 80.0);

//        javafx.scene.layout.HeaderBar.setButtonType(max, javafx.scene.layout.HeaderButtonType.MAXIMIZE);
//        primaryStage.initStyle(javafx.stage.StageStyle.EXTENDED);

        root.setPrefSize(400, 300);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setOpacity(0.96); // 80% 不透明度
        primaryStage.show();


        WinDef.HWND hWndByEnumeration = FXNativeWindowsTools.getHWndByEnumeration();


        mode.setOnAction(event -> {
            // 判断当前窗口模式
            Boolean windowDarkMode = FXNativeWindowsTools.getWindowDarkMode(hWndByEnumeration);
            if (Boolean.TRUE.equals(windowDarkMode)) {
                System.out.println("已切换暗色模式");
            } else {
                System.out.println("已切换亮色模式");
            }
            FXNativeWindowsTools.setWindowDarkMode(hWndByEnumeration, Boolean.FALSE.equals(windowDarkMode));


        });


        max.setOnAction(e -> {
            // 判断是否最大化
            if (!isWindowMaximized(hWndByEnumeration)) {
                System.out.println("不是最大化");
                FXNativeWindowsTools.maximizeWindow(hWndByEnumeration);
            } else {
                FXNativeWindowsTools.restoreWindow(hWndByEnumeration);
            }

        });

        //  设置窗口样式为亚克力
        FXNativeWindowsTools.setSystemStageStyle(hWndByEnumeration, FXNativeWindowsTools.SystemBackdropType.DWMSBT_ACRYLIC);
        //  可拖拽的 JavaFX 节点
        FXNativeWindowsTools.enableWindowDrag(hWndByEnumeration, root);
        // 96% 不透明
        FXNativeWindowsTools.setWindowAlpha(hWndByEnumeration, 0.96f);
        // 设置为暗色模式
        FXNativeWindowsTools.setWindowDarkMode(hWndByEnumeration, true);
        // 设置窗口不使用圆角
        FXNativeWindowsTools.setWindowCornerPreference(hWndByEnumeration,
                FXNativeWindowsTools.DwmWindowCornerPreference.ROUND);


    }
}
