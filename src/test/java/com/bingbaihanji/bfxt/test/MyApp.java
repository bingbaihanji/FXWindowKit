package com.bingbaihanji.bfxt.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderButtonType;
import javafx.scene.layout.HeaderDragType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MyApp extends Application {
    @Override
    public void start(Stage stage) {
        HeaderBar headerBar = new HeaderBar();

        Label title = new Label("FX App");
        headerBar.setCenter(title);

        // 设置Label组件可拖动
        HeaderBar.setDragType( title, HeaderDragType.DRAGGABLE_SUBTREE);

        Button maxButton = new Button("最大化");

        headerBar.setTrailing(maxButton);
        HeaderBar.setButtonType(maxButton, HeaderButtonType.MAXIMIZE); // 赋予系统窗口最大化按钮语义

        BorderPane root = new BorderPane();
        root.setTop(headerBar);

        stage.setScene(new Scene(root, 400, 300));
        stage.initStyle(StageStyle.EXTENDED);
        stage.show();
    }
}