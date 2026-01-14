package com.bingbaihanji.bfxt.test;


import com.bingbaihanji.bfxt.tools.FxTools;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author oyzh
 * @since 2025-08-18
 */
public class CustomTitleBarTest2 extends Application {


    private Stage stage;

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        System.setProperty("javafx.suppressPreviewWarning", "true");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.initStyle(StageStyle.EXTENDED);
        MenuBar menu = createMenu();

        HeaderBar.setAlignment(menu, Pos.CENTER_LEFT);
        HeaderBar.setMargin(menu, new Insets(5));

        var headerBar = new HeaderBar();
        headerBar.setCenter(menu);

        Button button = new Button("error");
        headerBar.setLeading(button);

        button.setOnAction(
                run -> {

                    try {
                        int i = 1 / 0;
                    } catch (Exception e) {
                        FxTools.showErrorAlert("失败", e.getMessage(), e);
                    }

                }
        );

        var root = new BorderPane();
        root.setTop(headerBar);

        stage.setScene(new Scene(root,800,600));
        stage.initStyle(StageStyle.EXTENDED);
        stage.show();
    }


    private MenuBar createMenu() {


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
        MenuItem zoomInItem = new MenuItem("放大");
        MenuItem zoomOutItem = new MenuItem("缩小");
        MenuItem fullScreenItem = new MenuItem("全屏");
        viewMenu.getItems().addAll(zoomInItem, zoomOutItem, fullScreenItem);

        // 创建"主题"菜单
        Menu themeMenu = new Menu("主题");
        MenuItem lightThemeItem = new MenuItem("亮色主题");
        lightThemeItem.setOnAction(e -> {
            System.out.println("亮色");
        });
        MenuItem darkThemeItem = new MenuItem("暗色主题");
        darkThemeItem.setOnAction(e -> {
            System.out.println("暗色");
        });
        themeMenu.getItems().addAll(lightThemeItem, darkThemeItem);

        var menuBar = new MenuBar();

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, themeMenu);

        // 拖动
        FxTools.enableDrag(menuBar, stage);


        return menuBar;
    }


    public static class CustomTitleBar2Starter {
        public static void main(String[] args) {
            CustomTitleBarTest2.main(args);
        }
    }
}
