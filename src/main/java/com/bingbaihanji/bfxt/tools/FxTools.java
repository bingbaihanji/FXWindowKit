package com.bingbaihanji.bfxt.tools;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FxTools {

    private static final Logger log = LoggerFactory.getLogger(FxTools.class);

    // 拖拽偏移
    private static double dragOffsetX;
    private static double dragOffsetY;

    // 还原尺寸
    private static double restoreX;
    private static double restoreY;
    private static double restoreW;
    private static double restoreH;


    // 从组件拖拽整个窗口,双击最大化
    public static void enableDrag(Node node, Stage stage) {

        node.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });

        node.setOnMouseDragged(e -> {
            // 最大化状态下开始拖拽 → 先还原
            if (stage.isMaximized()) {
                double mouseXRatio = e.getSceneX() / stage.getWidth();
                double mouseYRatio = e.getSceneY() / stage.getHeight();
                stage.setMaximized(false);
                stage.setWidth(restoreW);
                stage.setHeight(restoreH);
                stage.setX(e.getScreenX() - restoreW * mouseXRatio);
                stage.setY(e.getScreenY() - restoreH * mouseYRatio);
                dragOffsetX = restoreW * mouseXRatio;
                dragOffsetY = restoreH * mouseYRatio;
            }

            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        node.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !isResizeCursor(node)) {
                toggleMaximize(stage, e.getScreenX(), e.getScreenY());
            }
        });
    }

    /**
     * 在鼠标所在屏幕最大化 / 还原
     */
    private static void toggleMaximize(Stage stage, double mouseX, double mouseY) {

        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setX(restoreX);
            stage.setY(restoreY);
            stage.setWidth(restoreW);
            stage.setHeight(restoreH);
            return;
        }

        restoreX = stage.getX();
        restoreY = stage.getY();
        restoreW = stage.getWidth();
        restoreH = stage.getHeight();

        Screen screen = getScreenByMouse(mouseX, mouseY);
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setMaximized(true);
    }

    /**
     * 根据鼠标位置获取屏幕
     */
    private static Screen getScreenByMouse(double x, double y) {
        return Screen.getScreens().stream()
                .filter(s -> s.getBounds().contains(x, y))
                .findFirst()
                .orElse(Screen.getPrimary());
    }

    // 判断鼠标当前是不是在“窗口缩放状态”，如果是，就别把这次操作当成“拖动标题栏”或“双击最大化”。
    private static boolean isResizeCursor(Node node) {
        Cursor c = node.getCursor();
        return c == Cursor.E_RESIZE
                || c == Cursor.W_RESIZE
                || c == Cursor.N_RESIZE
                || c == Cursor.S_RESIZE
                || c == Cursor.NE_RESIZE
                || c == Cursor.NW_RESIZE
                || c == Cursor.SE_RESIZE
                || c == Cursor.SW_RESIZE;
    }


    //  截图与导出

    /**
     * 截图功能：截取节点内容，保存到剪切板并允许保存为文件
     *
     * @param primaryStage 主舞台
     * @param node         要截图的节点
     */
    public static void screenshots(Stage primaryStage, Node node) {

        // 创建快照
        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT); // 使用透明背景
        WritableImage image = node.snapshot(snapshotParameters, null);

        // 保存截图到剪切板
        copyImageToClipboard(image);

        // 保存到文件
        BufferedImage png = SwingFXUtils.fromFXImage(image, null);
        FileChooser fileChooser = new FileChooser();
        // 设置默认文件名：当前日期时间
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss.SSS");
        fileChooser.setInitialFileName(formatter.format(now));

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image File", ".png", "*.png")
        );
        File save = fileChooser.showSaveDialog(primaryStage);
        if (save != null) {
            try {
                ImageIO.write(png, "png", save);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 将图片复制到剪切板
     *
     * @param image 图片
     */
    public static void copyImageToClipboard(WritableImage image) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putImage(image);
        clipboard.setContent(content);
    }


    /**
     * 创建配置好的 FileChooser
     *
     * @param title           标题
     * @param initialFileName 默认文件名
     * @param description     文件类型描述
     * @param extensions      文件扩展名（例如："*.png", ".png"）
     * @return 配置好的 FileChooser
     */
    public static FileChooser createFileChooser(String title, String initialFileName, String description, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        if (initialFileName != null) {
            fileChooser.setInitialFileName(initialFileName);
        }
        if (description != null && extensions != null && extensions.length > 0) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(description, extensions)
            );
        }
        return fileChooser;
    }


    /**
     * 为 Dialog 设置图标（通用方法）
     * <p>
     * 使用监听器在对话框显示后获取 Stage 并设置图标
     *
     * @param dialog        对话框
     * @param iconPath      图标路径（相对于 resources 目录，例如："/icon/setting.png"）
     * @param resourceClass 用于加载资源的类（如果为 null，使用 FxTools.class）
     */
    public static void setDialogIcon(Dialog<?> dialog, String iconPath, Class<?> resourceClass) {
        dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // 对话框正在显示
                Window window = dialog.getDialogPane().getScene().getWindow();
                if (window instanceof Stage stage) {
                    try {
                        Class<?> loader = resourceClass != null ? resourceClass : FxTools.class;
                        var iconUrl = loader.getResource(iconPath);
                        if (iconUrl != null) {
                            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
                        }
                    } catch (Exception e) {
                        log.error("机子图标失败: {}", e.getMessage(), e);
                    }
                }
            }
        });
    }


    /**
     * 显示自定义错误对话框（带异常堆栈）- 科技感风格
     *
     * @param title   标题
     * @param message 消息
     * @param e       异常
     */
    public static void showErrorAlert(String title, String message, Throwable e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.setResizable(true); // 启用窗口缩放
            alert.setTitle(title);
            alert.setHeaderText(message);
            alert.setContentText(null); // 不显示内容区域
            alertStage.initStyle(StageStyle.UTILITY);

            // 添加详细异常信息
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            alert.getDialogPane().setExpandableContent(textArea);
            alert.getDialogPane().setExpanded(true); // 默认展开详细信息

            // 等待对话框显示后再应用样式
            alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    // 应用样式
                    applyCyberStyleToErrorAlert(alert, textArea);
                }
            });
            alert.showAndWait();
        });
    }

    /**
     * 为错误对话框应用科技感的浅蓝色样式
     *
     * @param alert    警告对话框
     * @param textArea 堆栈信息文本区域
     */
    private static void applyCyberStyleToErrorAlert(Alert alert, TextArea textArea) {
        var dialogPane = alert.getDialogPane();
        var scene = dialogPane.getScene();

        // 设置场景背景为透明
        scene.setFill(Color.TRANSPARENT);

        // CSS 样式：科技感浅蓝色主题
        String css = """
                
                /* 对话框主容器 - 浅蓝色科技感背景 */
                .dialog-pane > .content.container {
                    -fx-background-color: rgb(15, 25, 45);
                    -fx-border-color: rgb(100, 200, 255);
                    -fx-border-width: 2px;
                    -fx-border-radius: 10px;
                    -fx-background-radius: 10px;
                    -fx-effect: dropshadow(gaussian, rgb(100, 200, 255), 20, 0.3, 0, 0);
                }
                
                /* 标题栏区域 */
                .dialog-pane > .header-panel {
                    -fx-background-color: linear-gradient(to bottom, rgb(30, 60, 100), rgb(20, 40, 70));
                    -fx-border-color: rgb(100, 200, 255);
                    -fx-border-width: 0 0 1px 0;
                    -fx-padding: 15px;
                    -fx-background-radius: 8px 8px 0 0;
                }
                
                /* 标题文字 */
                .dialog-pane > .header-panel > .label {
                    -fx-text-fill: #FF4444;
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-effect: dropshadow(gaussian, rgb(255, 68, 68), 5, 0.5, 0, 0);
                }
                
                /* 内容区域 - 隐藏 */
                .dialog-pane > .content {
                    -fx-background-color: transparent;
                    -fx-padding: 0;
                    -fx-pref-height: 0;
                    -fx-max-height: 0;
                    -fx-min-height: 0;
                }
                
                /* 隐藏内容区域的文本 */
                .dialog-pane > .content > .label {
                    -fx-pref-height: 0;
                    -fx-max-height: 0;
                    -fx-min-height: 0;
                    visibility: hidden;
                }
                
                /* 按钮栏 */
                .dialog-pane > .button-bar > .container {
                    -fx-background-color: linear-gradient(to top, rgb(30, 60, 100), rgb(20, 40, 70));
                    -fx-border-color: rgb(100, 200, 255);
                    -fx-border-width: 1px 0 0 0;
                    -fx-padding: 10px;
                }
                
                /* 按钮样式 */
                .dialog-pane .button {
                    -fx-background-color: linear-gradient(to bottom, rgb(60, 140, 220), rgb(40, 100, 180));
                    -fx-text-fill: white;
                    -fx-font-size: 13px;
                    -fx-font-weight: bold;
                    -fx-border-color: rgb(100, 200, 255);
                    -fx-border-width: 1px;
                    -fx-border-radius: 5px;
                    -fx-background-radius: 5px;
                    -fx-padding: 8px 20px;
                    -fx-cursor: hand;
                }
                
                .dialog-pane .button:hover {
                    -fx-background-color: linear-gradient(to bottom, rgb(80, 160, 240), rgb(60, 120, 200));
                    -fx-effect: dropshadow(gaussian, rgba(100, 200, 255, 0.6), 10, 0.4, 0, 0);
                }
                
                .dialog-pane .button:pressed {
                    -fx-background-color: linear-gradient(to bottom, rgb(40, 100, 180), rgb(30, 80, 160));
                }
                
                /* 展开按钮 */
                .dialog-pane .more-button {
                    -fx-text-fill: #64C8FF;
                    -fx-font-weight: bold;
                }
                
                /* 滚动条样式 */
                .text-area .scroll-bar:vertical,
                .text-area .scroll-bar:horizontal {
                    -fx-background-color: rgb(30, 50, 80);
                }
                
                .text-area .scroll-bar .thumb {
                    -fx-background-color: rgb(100, 200, 255);
                    -fx-background-radius: 5px;
                }
                
                .text-area .scroll-bar .thumb:hover {
                    -fx-background-color: rgb(120, 220, 255);
                }
                
                .text-area .scroll-bar .track {
                    -fx-background-color: rgb(20, 30, 50);
                }
                """;

        // 堆栈信息文本框样式 - 深色背景，红色文本
        String textAreaCss = """
                -fx-control-inner-background: rgb(10, 15, 25);
                -fx-background-color: rgb(10, 15, 25);
                -fx-text-fill: #FF4444;
                -fx-border-color: rgb(100, 200, 255);
                -fx-border-width: 1px;
                -fx-border-radius: 5px;
                -fx-background-radius: 5px;
                -fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                -fx-font-size: 12px;
                -fx-effect: dropshadow(gaussian, rgb(255, 68, 68), 5, 0.3, 0, 0);
                """;

        // DialogPane整体样式 - 关键：设置整个对话框背景
        String dialogPaneStyle = """
                -fx-background-color: rgb(15, 25, 45);
                -fx-border-color: rgb(100, 200, 255);
                -fx-border-width: 2px;
                -fx-border-radius: 10px;
                -fx-background-radius: 10px;
                -fx-effect: dropshadow(gaussian, rgb(100, 200, 255), 20, 0.3, 0, 0);
                """;

        // 应用样式
        dialogPane.setStyle(dialogPaneStyle);
        dialogPane.getStylesheets().add("data:text/css," + css.replace("\n", " "));
        textArea.setStyle(textAreaCss);
    }


}
