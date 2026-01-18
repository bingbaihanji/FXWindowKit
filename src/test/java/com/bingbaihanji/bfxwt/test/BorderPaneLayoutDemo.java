package com.bingbaihanji.bfxwt.test;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class BorderPaneLayoutDemo {

    // 测试主方法
    public static void main(String[] args) {
        javafx.application.Application.launch(MyApp.class);
    }

    protected Pane createUI() {
        // 创建根布局 BorderPane（模拟 AbstractCustomWindow 的 root）
        BorderPane root = new BorderPane();

        // 1. 顶部区域 - 红色背景（模拟标题栏，固定高度）
        VBox topBox = new VBox();
        topBox.setStyle("-fx-background-color: lightcoral;");
        topBox.setMinHeight(32);
        topBox.setMaxHeight(32);
        topBox.setAlignment(Pos.CENTER);
        topBox.getChildren().add(new Label("顶部标题栏 (固定32px)"));
        root.setTop(topBox);

        // 2. 中间区域 - 创建一个 Pane 容器（模拟 createContent 返回的内容）
        Pane contentPane = new Pane();
        contentPane.setStyle("-fx-background-color: lightgray;");

        // 关键：设置最小尺寸为 0（模拟 DefaultWindowsApp.createContent 的设置）
        contentPane.setMinWidth(0);
        contentPane.setMinHeight(0);

        // 在 contentPane 内部创建一个 BorderPane，包含五个区域
        BorderPane innerPane = new BorderPane();

        // 内部 BorderPane 的五个区域
        VBox innerTop = createColoredVBox("顶部区域 (Top)", Color.LIGHTCORAL);
        VBox innerBottom = createColoredVBox("底部区域 (Bottom)", Color.LIGHTBLUE);
        VBox innerLeft = createColoredVBox("左侧区域 (Left)", Color.LIGHTGREEN);
        VBox innerRight = createColoredVBox("右侧区域 (Right)", Color.LIGHTYELLOW);
        VBox innerCenter = createColoredVBox("中间区域 (Center) - 向下缩放窗口观察", Color.LAVENDER);

        innerPane.setTop(innerTop);
        innerPane.setBottom(innerBottom);
        innerPane.setLeft(innerLeft);
        innerPane.setRight(innerRight);
        innerPane.setCenter(innerCenter);

        // 将 innerPane 添加到 contentPane
        contentPane.getChildren().add(innerPane);

        // 绑定 innerPane 的尺寸到 contentPane
        innerPane.prefWidthProperty().bind(contentPane.widthProperty());
        innerPane.prefHeightProperty().bind(contentPane.heightProperty());

        // 将 contentPane 设置为 root 的 center
        root.setCenter(contentPane);

        return root;
    }

    /**
     * 封装创建带背景色和标签的 VBox 的工具方法
     *
     * @param labelText 标签显示的文字
     * @param bgColor   VBox 的背景色
     * @return 配置好的 VBox 组件
     */
    private VBox createColoredVBox(String labelText, Color bgColor) {
        // 创建 VBox 并设置对齐方式（让 Label 居中）
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        // 设置 VBox 的最小尺寸，确保能清晰看到每个区域
        vbox.setMinSize(100, 100);

        // 创建标签并设置样式
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // 设置 VBox 背景色（无边角、无内边距）
        BackgroundFill backgroundFill = new BackgroundFill(bgColor, CornerRadii.EMPTY, javafx.geometry.Insets.EMPTY);
        vbox.setBackground(new Background(backgroundFill));

        // 将标签添加到 VBox 中
        vbox.getChildren().add(label);

        return vbox;
    }

    // 内部类：JavaFX 应用入口
    public static class MyApp extends javafx.application.Application {
        @Override
        public void start(javafx.stage.Stage primaryStage) {
            BorderPaneLayoutDemo demo = new BorderPaneLayoutDemo();
            Pane root = demo.createUI();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 400);
            primaryStage.setTitle("BorderPane 五区域布局 - 向下缩放窗口，观察紫色中部区域是否会进入红色顶部");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("=== 操作提示 ===");
            System.out.println("向下拖动窗口边框缩小窗口，观察紫色的中部区域是否会超出边界进入红色的顶部区域");
        }
    }
}