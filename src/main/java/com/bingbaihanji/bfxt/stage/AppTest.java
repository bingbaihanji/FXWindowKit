package com.bingbaihanji.bfxt.stage;

import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *
 * @author bingbaihanji
 * @date 2026-01-13 11:40:25
 * @description //TODO
 */
public class AppTest extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {

        var root = new AnchorPane();

        HBox hBox = new HBox(10);
        hBox.setStyle("""
                 -fx-background-color: #9F97B3;
                """);
        Button button = new Button("按钮");
        hBox.getChildren().add(button);
        hBox.prefWidthProperty().bind(root.widthProperty());

        root.getChildren().add(hBox);
        AnchorPane.setTopAnchor(hBox, 0.0);


        Button mode = new Button("模式切换");

        root.getChildren().add(mode);

        AnchorPane.setTopAnchor(mode, 40.0);

        root.setPrefSize(400, 300);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setOpacity(0.96); // 80% 不透明度
//        primaryStage.initStyle(StageStyle.EXTENDED);
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


//        //  设置窗口样式为亚克力
        FXNativeWindowsTools.setSystemStageStyle(hWndByEnumeration, FXNativeWindowsTools.SystemBackdropType.DWMSBT_ACRYLIC);
//        //  可拖拽的 JavaFX 节点
        FXNativeWindowsTools.enableWindowDrag(hWndByEnumeration, root);
//        // 96% 不透明
//        FXNativeWindowsTools.setWindowAlpha(hWndByEnumeration, 0.96f);
//
//        // 设置为暗色模式
        FXNativeWindowsTools.setWindowDarkMode(hWndByEnumeration, false);
//        // 设置窗口不使用圆角
        FXNativeWindowsTools.setWindowCornerPreference(hWndByEnumeration,
                FXNativeWindowsTools.DwmWindowCornerPreference.DO_NOT_ROUND);


    }
}
