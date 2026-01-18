package com.bingbaihanji.bfxt.stage;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SubScene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 *
 * @author bingbaihanji
 * @date 2026-01-12 10:35:12
 * @description 主题切换示例
 */
public abstract class DefaultLayout extends AbstractCustomWindow {

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

    @Override
    protected boolean isThemeSwitchEnabled() {
        return true;
    }
    /**
     * 创建自定义菜单栏的核心方法
     * @return 包含工具、窗口、帮助菜单的 MenuBar 对象
     */
    /**
     * 创建窗口内容区域
     * 使用 StackPane 确保子节点能够自适应填充整个区域
     * 框架会自动处理节点尺寸绑定，子类无需关心
     *
     * @return 包含自定义内容的 Parent 容器
     */
    @Override
    protected Parent createContent() {


        StackPane contentWrapper = new StackPane();
        contentWrapper.setMinSize(0, 0);

        Node node = createNode();

        if (node instanceof SubScene subScene) {
            // SubScene 只允许绑定到 wrapper
            subScene.widthProperty().bind(contentWrapper.widthProperty());
            subScene.heightProperty().bind(contentWrapper.heightProperty());

        } else if (node instanceof Region region) {
            // 只设置 minSize，绝不绑定 prefSize
            region.setMinSize(0, 0);
        }

        contentWrapper.getChildren().add(node);
        return contentWrapper;
    }

    /**
     * 子类实现此方法以提供自定义的 UI 内容
     * 框架会自动处理节点的尺寸绑定，无需手动绑定到父容器
     *
     * @return 自定义的 UI 节点（支持 SubScene、Region 或其他 Node 类型）
     */
    protected abstract Node createNode();


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

        viewMenu.getItems().addAll(transparent, restore);

        // 添加菜单到菜单栏
        addTitleEventTarget(fileMenu);
        addTitleEventTarget(editMenu);
        addTitleEventTarget(viewMenu);
    }
}
