package com.bingbaihanji.bfxt.test;

import com.bingbaihanji.bfxt.stage.AbstractCustomWindow;
import com.bingbaihanji.bfxt.stage.WindowTheme;
import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author bingbaihanji
 * @date 2026-01-12 10:35:12
 * @description 主题切换示例
 */
public class Main extends AbstractCustomWindow {

    static void main(String[] args) {
        launch(args);
    }

    /**
     * 原来 start() 里写的「窗口标题」
     */

    @Override
    protected String appTitle() {
        return "FX窗口";
    }

    /**
     * 原来 start() 里写的「窗口图标」
     */
    @Override
    protected Image appIcon() {
        return new Image(getClass().getResourceAsStream("/icons/app.png"));
    }

    /**
     * 指定窗口主题（默认使用亮色主题）
     */
    @Override
    protected WindowTheme getTheme() {
        return WindowTheme.dark();  // 使用亮色主题，也可以用 WindowTheme.dark()
    }

    @Override
    protected boolean isAlwaysOnTopEnabled() {
        return true;
    }
    /**
     * 创建自定义菜单栏的核心方法
     * @return 包含工具、窗口、帮助菜单的 MenuBar 对象
     */
    /**
     * 写 UI 布局的方法
     */
    @Override
    protected Parent createContent() {

        BorderPane root = new BorderPane();

        // 中心内容
        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(50));


        Label descLabel = new Label("点击下方按钮可以切换窗口主题");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        // 主题切换按钮
        Button lightThemeBtn = new Button("切换到亮色主题");
        lightThemeBtn.setPrefWidth(200);
        lightThemeBtn.setPrefHeight(40);
        lightThemeBtn.setStyle("""
                -fx-background-color: #007AFF;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """);
        lightThemeBtn.setOnAction(e -> setTheme(WindowTheme.light()));

        Button darkThemeBtn = new Button("切换到暗色主题");
        darkThemeBtn.setPrefWidth(200);
        darkThemeBtn.setPrefHeight(40);
        darkThemeBtn.setStyle("""
                -fx-background-color: #34C759;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-background-radius: 6;
                -fx-cursor: hand;
                """);
        darkThemeBtn.setOnAction(e -> setTheme(WindowTheme.dark()));

        // 窗口透明度控制
        Label opacityLabel = new Label("窗口透明度控制");
        opacityLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // 透明度百分比显示
        Label opacityValueLabel = new Label("100%");
        opacityValueLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #007AFF; -fx-font-weight: bold;");

        // 透明度滑动条 (0.1-1.0，默认1.0完全不透明)
        Slider opacitySlider = new Slider(0.1, 1.0, 1.0);
        opacitySlider.setPrefWidth(300);
        opacitySlider.setShowTickLabels(true);
        opacitySlider.setShowTickMarks(true);
        opacitySlider.setMajorTickUnit(0.2);  // 每20%一个大刻度
        opacitySlider.setMinorTickCount(4);
        opacitySlider.setBlockIncrement(0.05);

        // 滑动条样式
        opacitySlider.setStyle("""
                -fx-control-inner-background: #E0E0E0;
                """);

        // 滑动条值改变时实时更新窗口透明度
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            float alpha = newVal.floatValue();
            double percentage = alpha * 100;
            opacityValueLabel.setText(String.format("%.0f%%", percentage));

            // 实时更新窗口透明度

            super.getStage().setOpacity(alpha);


        });

        // 提示信息
        Label tipLabel = new Label("拖动滑块调整窗口透明度 (0% = 完全透明, 100% = 不透明)");
        tipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999999;");

        center.getChildren().addAll(
                descLabel,
                lightThemeBtn,
                darkThemeBtn,
                opacityLabel,
                opacityValueLabel,
                opacitySlider,
                tipLabel
        );

        Platform.runLater(() -> {
            WinDef.HWND hWnd = FXNativeWindowsTools.getHWndByEnumeration();
            // 设置窗口圆角样式
            FXNativeWindowsTools.setWindowCornerPreference(hWnd, FXNativeWindowsTools.DwmWindowCornerPreference.ROUND);
        });

        BorderPane content = new BorderPane();
        content.setCenter(center);
        root.setCenter(content);

        return root;
    }

    /**
     * 标题栏创建完成后的回调 - 在此添加自定义菜单
     */
    @Override
    protected void onTitleBarReady() {
        // 创建"文件"菜单
        Menu fileMenu = new Menu("文件");
        MenuItem newItem = new MenuItem("新建");
        MenuItem openItem = new MenuItem("打开");
        MenuItem saveItem = new MenuItem("保存");
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(newItem, openItem, saveItem, exitItem);

        // 创建"编辑"菜单
        Menu editMenu = new Menu("编辑");
        MenuItem undoItem = new MenuItem("撤销");
        MenuItem redoItem = new MenuItem("重做");
        MenuItem cutItem = new MenuItem("剪切");
        MenuItem copyItem = new MenuItem("复制");
        MenuItem pasteItem = new MenuItem("粘贴");
        editMenu.getItems().addAll(undoItem, redoItem, cutItem, copyItem, pasteItem);

        // 创建"视图"菜单
        Menu viewMenu = new Menu("视图");
        MenuItem transparent = new MenuItem("透明");
        MenuItem restore = new MenuItem("恢复");


        transparent.setOnAction(e -> {
            WinDef.HWND hWnd = FXNativeWindowsTools.getHWndByEnumeration();
            System.out.println("半透明: " + hWnd);
            FXNativeWindowsTools.setWindowAlpha(hWnd, 0.5f); // 50% 透明

        });


        restore.setOnAction(e -> {
            WinDef.HWND hWnd = FXNativeWindowsTools.getHWndByEnumeration();
            System.out.println("恢复: " + hWnd);
            FXNativeWindowsTools.setWindowAlpha(hWnd, 1.0f); // 完全不透明
        });


        viewMenu.getItems().addAll(transparent, restore);

        // 创建"主题"菜单
        Menu themeMenu = new Menu("主题");
        MenuItem lightThemeItem = new MenuItem("亮色主题");
        lightThemeItem.setOnAction(e -> setTheme(WindowTheme.light()));
        MenuItem darkThemeItem = new MenuItem("暗色主题");
        darkThemeItem.setOnAction(e -> setTheme(WindowTheme.dark()));
        themeMenu.getItems().addAll(lightThemeItem, darkThemeItem);

        // 添加菜单到菜单栏
        addTitleEventTarget(fileMenu);
        addTitleEventTarget(editMenu);
        addTitleEventTarget(viewMenu);
        addTitleEventTarget(themeMenu);
    }
}
