package com.bingbaihanji.bfxwt.test;//package com.bingbaihanji.bfxwt.stage;
//
//import com.bingbaihanji.bfxwt.tools.FXNativeWindowsTools;
//import com.bingbaihanji.bfxwt.tools.FXWindowEmbedTools;
//import com.sun.jna.Pointer;
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//
/// **
// * 外部窗口嵌入测试
// *
// * @author bingbaihanji
// * @date 2026-01-13 11:40:25
// * @description 将外部应用窗口（如记事本、计算器等）嵌入到 JavaFX 窗口中
// */
//public class AppTest1 extends Application {
//
//    private WinDef.HWND embeddedHwnd;  // 被嵌入的外部窗口句柄
//    private WinDef.HWND containerHwnd; // JavaFX 容器窗口句柄
//    private WinDef.HWND hostHwnd;      // Host 窗口句柄（三层结构的中间层）
//    private Pane embedContainer;        // 嵌入容器区域
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//
//        BorderPane root = new BorderPane();
//        root.setStyle("-fx-background-color: #f5f5f5;");
//
//        // ========== 顶部控制区 ==========
//        VBox topControls = new VBox(10);
//        topControls.setPadding(new Insets(15));
//        topControls.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
//
//        Label titleLabel = new Label("外部窗口嵌入测试");
//        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
//
//        Label tipLabel = new Label("提示：先打开外部应用（如记事本、计算器等），然后输入窗口标题进行嵌入");
//        tipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
//
//        // 输入区域
//        HBox inputBox = new HBox(10);
//        inputBox.setAlignment(Pos.CENTER_LEFT);
//
//        Label inputLabel = new Label("窗口标题:");
//        inputLabel.setStyle("-fx-font-size: 13px;");
//
//        TextField windowTitleField = new TextField();
//        windowTitleField.setPromptText("例如: 无标题 - 记事本");
//        windowTitleField.setPrefWidth(250);
//
//        Button searchButton = new Button("搜索窗口");
//        searchButton.setStyle("-fx-background-color: #5856D6; -fx-text-fill: white;");
//
//        inputBox.getChildren().addAll(inputLabel, windowTitleField, searchButton);
//
//        // 按钮区域
//        HBox buttonBox = new HBox(10);
//        buttonBox.setAlignment(Pos.CENTER_LEFT);
//
//        Button embedButton = new Button("嵌入窗口");
//        embedButton.setPrefWidth(120);
//        embedButton.setDisable(true);
//        embedButton.setStyle("-fx-background-color: #34C759; -fx-text-fill: white;");
//
//        Button detachButton = new Button("分离窗口");
//        detachButton.setPrefWidth(120);
//        detachButton.setDisable(true);
//        detachButton.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white;");
//
//        Button refreshButton = new Button("刷新布局");
//        refreshButton.setPrefWidth(120);
//        refreshButton.setDisable(true);
//        refreshButton.setStyle("-fx-background-color: #007AFF; -fx-text-fill: white;");
//
//        buttonBox.getChildren().addAll(embedButton, detachButton, refreshButton);
//
//        // 状态显示
//        Label statusLabel = new Label("状态: 未嵌入窗口");
//        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
//
//        topControls.getChildren().addAll(titleLabel, tipLabel, inputBox, buttonBox, statusLabel);
//
//        // ========== 中间嵌入容器区 ==========
//        StackPane centerArea = new StackPane();
//        centerArea.setStyle("-fx-background-color: #e8e8e8;");
//
//        embedContainer = new Pane();
//        embedContainer.setStyle("-fx-background-color: white; -fx-border-color: #007AFF; -fx-border-width: 2; -fx-border-style: dashed;");
//        embedContainer.setPrefSize(600, 400);
//
//        // 添加点击监听器，点击容器时将焦点转移到嵌入的窗口
//        embedContainer.setOnMouseClicked(event -> {
//            if (embeddedHwnd != null && hostHwnd != null) {
//                User32.INSTANCE.SetFocus(embeddedHwnd);
//                System.out.println("✓ 点击容器，焦点已转移到外部窗口");
//            }
//        });
//
//        Label placeholderLabel = new Label("外部窗口将显示在这里");
//        placeholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
//        StackPane.setAlignment(placeholderLabel, Pos.CENTER);
//
//        centerArea.getChildren().addAll(embedContainer, placeholderLabel);
//
//        // ========== 底部信息区 ==========
//        VBox bottomInfo = new VBox(5);
//        bottomInfo.setPadding(new Insets(10));
//        bottomInfo.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 0 0;");
//
//        Label infoTitle = new Label("操作说明:");
//        infoTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
//
//        Label step1 = new Label("1. 打开外部应用（如记事本：运行 notepad）");
//        Label step2 = new Label("2. 输入窗口标题并点击搜索窗口");
//        Label step3 = new Label("3. 点击嵌入窗口将外部窗口嵌入到中间区域");
//        Label step4 = new Label("4. 点击分离窗口恢复外部窗口为独立窗口");
//
//        step1.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
//        step2.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
//        step3.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
//        step4.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
//
//        bottomInfo.getChildren().addAll(infoTitle, step1, step2, step3, step4);
//
//        root.setTop(topControls);
//        root.setCenter(centerArea);
//        root.setBottom(bottomInfo);
//
//        Scene scene = new Scene(root, 800, 600);
//        primaryStage.setTitle("外部窗口嵌入测试 - JavaFX 容器");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//
//        // 获取 JavaFX 容器窗口句柄
//        Platform.runLater(() -> {
//            containerHwnd = FXNativeWindowsTools.getHWndByEnumeration();
//            if (containerHwnd != null) {
//                System.out.println("✓ 容器窗口句柄: 0x" + Long.toHexString(Pointer.nativeValue(containerHwnd.getPointer())));
//            }
//        });
//
//        // ========== 事件处理 ==========
//
//        // 搜索窗口
//        searchButton.setOnAction(e -> {
//            String windowTitle = windowTitleField.getText().trim();
//            if (windowTitle.isEmpty()) {
//                showAlert(Alert.AlertType.WARNING, "输入错误", "请输入窗口标题");
//                return;
//            }
//
//            // 查找窗口
//            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
//
//            if (hwnd != null && Pointer.nativeValue(hwnd.getPointer()) != 0) {
//                embeddedHwnd = hwnd;
//                long hwndValue = Pointer.nativeValue(hwnd.getPointer());
//
//                embedButton.setDisable(false);
//                statusLabel.setText("状态: 找到窗口 - 句柄: 0x" + Long.toHexString(hwndValue));
//                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #007AFF;");
//
//                System.out.println("✓ 找到窗口: " + windowTitle);
//                System.out.println("  句柄: 0x" + Long.toHexString(hwndValue));
//
//                placeholderLabel.setText("找到窗口，点击嵌入窗口按钮");
//            } else {
//                embeddedHwnd = null;
//                embedButton.setDisable(true);
//                statusLabel.setText("状态: 未找到窗口");
//                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF3B30;");
//
//                showAlert(Alert.AlertType.ERROR, "窗口未找到",
//                        "未找到标题为 \"" + windowTitle + "\" 的窗口\n\n" +
//                                "请确保：\n" +
//                                "1. 目标窗口已打开\n" +
//                                "2. 窗口标题完全匹配（包括空格和标点）\n" +
//                                "3. 窗口没有被最小化");
//            }
//        });
//
//        // 嵌入窗口
//        embedButton.setOnAction(e -> {
//            if (embeddedHwnd != null && containerHwnd != null) {
//                try {
//                    // 延迟两次执行，确保布局完全完成
//                    Platform.runLater(() -> Platform.runLater(() -> {
//                        // 获取 JavaFX 窗口的客户区矩形
//                        WinDef.RECT fxClientRect = new WinDef.RECT();
//                        User32.INSTANCE.GetClientRect(containerHwnd, fxClientRect);
//
//                        // 获取 JavaFX 窗口在屏幕上的位置
//                        WinDef.RECT fxWindowRect = new WinDef.RECT();
//                        User32.INSTANCE.GetWindowRect(containerHwnd, fxWindowRect);
//
//                        // 获取 embedContainer 在屏幕上的绝对位置
//                        double containerScreenX = embedContainer.localToScreen(0, 0).getX();
//                        double containerScreenY = embedContainer.localToScreen(0, 0).getY();
//                        double containerWidth = embedContainer.getWidth();
//                        double containerHeight = embedContainer.getHeight();
//
//                        // embedContainer 有 2px 边框，需要调整坐标和大小
//                        int borderWidth = 2;
//                        containerScreenX += borderWidth;  // 左边框
//                        containerScreenY += borderWidth;  // 上边框
//                        containerWidth -= borderWidth * 2;  // 减去左右边框
//                        containerHeight -= borderWidth * 2;  // 减去上下边框
//
//                        // 计算相对于 FX 窗口客户区左上角的偏移
//                        // FX 窗口客户区在屏幕上的起始位置
//                        WinDef.POINT clientOrigin = new WinDef.POINT();
//                        clientOrigin.x = 0;
//                        clientOrigin.y = 0;
//                        FXNativeWindowsTools.User32Api.INSTANCE.ClientToScreen(containerHwnd, clientOrigin);
//
//                        // 计算相对坐标
//                        int relativeX = (int) (containerScreenX - clientOrigin.x);
//                        int relativeY = (int) (containerScreenY - clientOrigin.y);
//
//                        System.out.println("=== 详细坐标调试信息 ===" );
//                        System.out.println("FX 窗口位置 (屏幕): (" + fxWindowRect.left + ", " + fxWindowRect.top + ")");
//                        System.out.println("FX 客户区大小: " + fxClientRect.right + " x " + fxClientRect.bottom);
//                        System.out.println("FX 客户区起点 (屏幕): (" + clientOrigin.x + ", " + clientOrigin.y + ")");
//                        System.out.println("容器位置 (屏幕): (" + (int)containerScreenX + ", " + (int)containerScreenY + ")");
//                        System.out.println("容器大小: " + (int)containerWidth + " x " + (int)containerHeight);
//                        System.out.println("计算的相对坐标: (" + relativeX + ", " + relativeY + ")");
//
//                        // 使用三层结构嵌入外部窗口
//                        hostHwnd = FXWindowEmbedTools.embedExternalWindow(
//                                embeddedHwnd,
//                                containerHwnd,
//                                relativeX,
//                                relativeY,
//                                (int) containerWidth,
//                                (int) containerHeight
//                        );
//
//                        if (hostHwnd != null) {
//                            // 验证 Host 窗口的实际位置
//                            WinDef.RECT hostRect = new WinDef.RECT();
//                            User32.INSTANCE.GetWindowRect(hostHwnd, hostRect);
//                            System.out.println("Host 窗口位置 (屏幕): (" + hostRect.left + ", " + hostRect.top + ")");
//                            System.out.println("Host 窗口大小: " + (hostRect.right - hostRect.left) + " x " + (hostRect.bottom - hostRect.top));
//
//                            embedButton.setDisable(true);
//                            detachButton.setDisable(false);
//                            refreshButton.setDisable(false);
//                            statusLabel.setText("状态: 窗口已嵌入（三层结构）");
//                            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34C759;");
//
//                            placeholderLabel.setVisible(false);
//
//                            System.out.println("✓ 外部窗口已成功嵌入到 JavaFX 容器（三层结构）");
//
//                            // 延迟一下，然后点击外部窗口确保获得焦点
//                            Platform.runLater(() -> {
//                                try {
//                                    Thread.sleep(100);
//                                } catch (InterruptedException ex) {
//                                    ex.printStackTrace();
//                                }
//                                User32.INSTANCE.SetFocus(embeddedHwnd);
//                                System.out.println("✓ 已设置焦点到外部窗口");
//                            });
//                        } else {
//                            showAlert(Alert.AlertType.ERROR, "嵌入失败", "Host 窗口创建失败");
//                        }
//                    }));
//                } catch (Exception ex) {
//                    showAlert(Alert.AlertType.ERROR, "嵌入失败", "嵌入失败: " + ex.getMessage());
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        // 分离窗口
//        detachButton.setOnAction(e -> {
//            if (embeddedHwnd != null && hostHwnd != null) {
//                try {
//                    // 销毁 Host 窗口并分离外部窗口
//                    FXWindowEmbedTools.destroyHostWindow(hostHwnd, embeddedHwnd);
//
//                    // 恢复窗口位置和大小
//                    User32.INSTANCE.SetWindowPos(
//                            embeddedHwnd,
//                            null,
//                            100, 100,
//                            600, 400,
//                            0x0040  // SWP_SHOWWINDOW
//                    );
//
//                    hostHwnd = null;  // 清空 Host 窗口句柄
//                    embedButton.setDisable(false);
//                    detachButton.setDisable(true);
//                    refreshButton.setDisable(true);
//                    statusLabel.setText("状态: 窗口已分离");
//                    statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9500;");
//
//                    placeholderLabel.setVisible(true);
//                    placeholderLabel.setText("外部窗口将显示在这里");
//
//                    System.out.println("✓ 外部窗口已成功分离，恢复为独立窗口");
//                } catch (Exception ex) {
//                    showAlert(Alert.AlertType.ERROR, "分离失败", "分离失败: " + ex.getMessage());
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        // 刷新布局（当容器大小改变时重新调整嵌入窗口）
//        refreshButton.setOnAction(e -> {
//            if (hostHwnd != null && embeddedHwnd != null) {
//                // 获取 embedContainer 在屏幕上的绝对位置
//                double containerScreenX = embedContainer.localToScreen(0, 0).getX();
//                double containerScreenY = embedContainer.localToScreen(0, 0).getY();
//                double containerWidth = embedContainer.getWidth();
//                double containerHeight = embedContainer.getHeight();
//
//                // 计算相对于 FX 窗口客户区左上角的偏移
//                WinDef.POINT clientOrigin = new WinDef.POINT();
//                clientOrigin.x = 0;
//                clientOrigin.y = 0;
//                FXNativeWindowsTools.User32Api.INSTANCE.ClientToScreen(containerHwnd, clientOrigin);
//
//                int relativeX = (int) (containerScreenX - clientOrigin.x);
//                int relativeY = (int) (containerScreenY - clientOrigin.y);
//
//                // 更新 Host 窗口和外部窗口的位置和大小
//                FXWindowEmbedTools.updateHostWindow(hostHwnd, embeddedHwnd, relativeX, relativeY, (int) containerWidth, (int) containerHeight);
//
//                System.out.println("✓ 已刷新 Host 窗口和外部窗口布局: (" + relativeX + ", " + relativeY + ") " + (int)containerWidth + "x" + (int)containerHeight);
//            }
//        });
//
//        // 监听窗口大小变化，自动调整嵌入窗口
//        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
//            if (hostHwnd != null && !detachButton.isDisabled()) {
//                Platform.runLater(() -> refreshButton.fire());
//            }
//        });
//
//        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
//            if (hostHwnd != null && !detachButton.isDisabled()) {
//                Platform.runLater(() -> refreshButton.fire());
//            }
//        });
//    }
//
//    /**
//     * 显示提示对话框
//     */
//    private void showAlert(Alert.AlertType type, String title, String message) {
//        Alert alert = new Alert(type);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    public static void com.bingbaihanji.kfxgl.main(String[] args) {
//        launch(args);
//    }
//}
