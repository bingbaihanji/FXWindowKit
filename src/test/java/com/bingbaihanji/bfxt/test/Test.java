package com.bingbaihanji.bfxt.test;//package com.bingbaihanji.bfxt.test;
//
//import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
//import com.bingbaihanji.bfxt.tools.FxTools;
//import com.sun.jna.platform.win32.WinDef;
//import javafx.application.Application;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.image.Image;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.HeaderBar;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//
///**
// *
// * @author bingbaihanji
// * @date 2026-01-12 19:23:32
// * @description 科技感半透明浅蓝色窗口测试
// */
//public class Test extends Application {
//
//    static void main(String[] args) {
//        Application.launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        // 设置透明窗口样式
//        primaryStage.initStyle(StageStyle.EXTENDED);
//
//
//        var headerBar = new HeaderBar();
//        HBox hBox = new HBox();
//        hBox.setAlignment(Pos.CENTER);
//
//        hBox.setStyle("""
//
//                                -fx-background-color:  TRANSPARENT;
//
//                """);   Button button2 = new Button("222");
//        Button button3 = new Button("333");
//        Button button4 = new Button("444");
//
//        button2.setOnAction(run ->{
//            System.out.println("222");
//            WinDef.HWND hWnd = FXNativeWindowsTools.getHWnd();
//            System.out.println(hWnd);
//        });
//
//        button3.setOnAction(run ->{
//            System.out.println("333");
//
//        });
//
//        button4.setOnAction(run ->{
//            System.out.println("444");
//        });
//
//        hBox.getChildren().addAll(
//              button2,button3,button4
//        );
//
//        FxTools.enableDrag(hBox, primaryStage);
//        headerBar.setCenter(hBox);
//
//        var root = new BorderPane();
//        root.setTop(headerBar);
//
//
//
//        applyCyberStyle(root);
//        Scene scene = new Scene(root, 800, 600);
//
//        scene.setFill(Color.TRANSPARENT);
//        primaryStage.setScene(scene);
//        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app.png")));
//        primaryStage.setTitle("demo");
//        primaryStage.show();
//        WinDef.HWND hWnd = FXNativeWindowsTools.getHWnd();
//        System.out.println(hWnd);
//
//    }
//
//    private void applyCyberStyle(Node root) {
//        String style = """
//                -fx-background-color: rgba(15, 25, 45, 0.75);
//                -fx-border-color: rgba(100, 200, 255, 0.6);
//                -fx-border-width: 2px;
//                -fx-border-radius: 10px;
//                -fx-background-radius: 10px;
//                -fx-effect: dropshadow(gaussian, rgba(100, 200, 255, 0.5), 20, 0.3, 0, 0);
//                """;
//        root.setStyle(style);
//    }
//}
