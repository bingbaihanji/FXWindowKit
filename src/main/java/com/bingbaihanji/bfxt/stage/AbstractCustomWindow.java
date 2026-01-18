package com.bingbaihanji.bfxt.stage;

import com.bingbaihanji.bfxt.tools.FXNativeWindowsTools;
import com.sun.jna.platform.win32.WinDef;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;
import java.util.Stack;

/**
 * 自定义无边框窗口基类
 * 提供标题栏、窗口控制按钮、拖拽移动、边缘调整大小、最大化等功能
 * 子类需要实现抽象方法来定制窗口标题、图标和内容区域
 */
public abstract class AbstractCustomWindow extends Application {

    // 常量定义

    // 标题栏高度
    private static final double TITLE_BAR_HEIGHT = 32.0;

    // 窗口控制按钮尺寸
    private static final double WINDOW_BTN_WIDTH = 34.6;
    private static final double WINDOW_BTN_HEIGHT = 26.0;
    private static final double WINDOW_BTN_ICON_SIZE = 12.0;
    private static final double APP_ICON_SIZE = 16.0;

    // 窗口尺寸
    private static final double DEFAULT_WINDOW_WIDTH = 1000.0;
    private static final double DEFAULT_WINDOW_HEIGHT = 700.0;
    private static final double MIN_WINDOW_WIDTH = 300.0;
    private static final double MIN_WINDOW_HEIGHT = 200.0;

    // 边缘拖拽区域宽度
    private static final int RESIZE_MARGIN = 6;

    // 窗口控制按钮类型枚举
    // 窗口状态变量
    private Stage stage;              // 主舞台


    // 子类可以实现的抽象方法
    private BorderPane root;          // 主布局容器
    private HBox titleBar;            // 标题栏容器
    private MenuBar menuBar;          // 标题栏菜单栏
    private Region spacer;            // 标题栏弹性空白区域（用于左右分隔）
    private Label titleLabel;         // 标题文字标签（用于主题切换时更新样式）
    private Scene scene;              // 场景对象（用于动态更新CSS）

    // 标题栏左右两侧组件管理
    private Stack<javafx.scene.Node> leftComponentsStack;   // 左侧组件栈（按入栈顺序从左往右显示）
    private Stack<javafx.scene.Node> rightComponentsStack;  // 右侧组件栈（按出栈顺序从左往右显示）
    private HBox leftBox;             // 左侧组件容器
    private HBox rightBox;            // 右侧组件容器

    // 当前主题
    private WindowTheme currentTheme;
    // 最大化状态及还原信息
    private boolean maximized = false;
    private double lastX, lastY, lastW, lastH;
    // 拖拽移动相关
    private double dragOffsetX, dragOffsetY;
    // 窗口控制按钮引用
    private Button closeBtn, maxBtn, minBtn, toTopBtn, ThemeBtn;
    private ImageView maxBtnIcon;  // 最大化按钮的图标（用于动态切换）
    private ImageView toTopBtnIcon;  // 置顶按钮的图标（用于动态切换）
    private ImageView themeBtnIcon;  // 主题切换按钮的图标（用于动态切换）
    // 调整大小时的初始位置和尺寸
    private double resizeStartX;
    private double resizeStartY;
    private double resizeStartW;
    private double resizeStartH;


    // 窗口是否始终置顶
    private boolean alwaysOnTop = false;

    // 返回窗口标题文本  设置窗口标题时让子类实现即可
    protected String appTitle() {
        return "";
    }

    // 返回窗口图标 设置图标时让子类返回有效Image对象即可
    protected Image appIcon() {
        return null;
    }

    // 子类重写此方法以启用置顶按钮

    // 是否开启置顶按钮
    protected boolean isAlwaysOnTopEnabled() {
        return false;
    }

    // 是否开启主题切换功能
    protected boolean isThemeSwitchEnabled() {
        return false;
    }

    // 创建并返回窗口内容区域
    protected abstract Parent createContent();

    // 返回窗口主题（默认为暗色主题，子类可重写）
    protected WindowTheme getTheme() {
        return WindowTheme.dark();
    }

    /*
     * 切换窗口主题
     * 更新标题栏、窗口背景、按钮样式、菜单栏样式等
     * @param theme 新主题
     */
    protected final void setTheme(WindowTheme theme) {
        this.currentTheme = theme;

        // 更新窗口背景色
        root.setStyle("-fx-background-color: " + theme.windowBgColor() + ";");
        // 更新标题栏背景色
        titleBar.setStyle("""
                -fx-background-color: %s;
                -fx-alignment: center-left;
                -fx-padding: 0 6;
                """.formatted(theme.titleBarBgColor()));

        // 更新标题文字颜色
        titleLabel.setStyle("-fx-text-fill: " + theme.titleTextColor() + "; -fx-padding: 0 8;");

        // 更新菜单栏中所有菜单的文字颜色
        updateMenuBarTheme();

        // 更新菜单项的CSS样式
        applyMenuThemeCSS();

        // 更新所有窗口按钮样式
        updateButtonTheme(minBtn, WindowButtonType.MINIMIZE);
        updateButtonTheme(maxBtn, WindowButtonType.MAXIMIZE);
        updateButtonTheme(closeBtn, WindowButtonType.CLOSE);

        // 如果启用了置顶按钮，也更新其样式
        if (isAlwaysOnTopEnabled() && toTopBtn != null) {
            updateButtonTheme(toTopBtn, WindowButtonType.TO_TOP);
        }
        // 如果启用了主题切换按钮，也更新其样式
        if (isThemeSwitchEnabled() && ThemeBtn != null) {
            updateButtonTheme(ThemeBtn, WindowButtonType.THEME_SWITCHING);
        }
    }

    // 标题栏创建完成后的回调（可选，用于子类进行额外初始化）
    protected void onTitleBarReady() {
    }

    // JavaFX 生命周期方法

    /*
     * 应用启动入口
     * 初始化无边框窗口、创建标题栏和内容区域、启用拖拽和调整大小功能
     */
    @Override
    public final void start(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);  // 移除系统默认边框

        // 1. 初始化主题
        currentTheme = getTheme();

        // 2. 创建主布局容器
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + currentTheme.windowBgColor() + ";");

        // 3. 创建标题栏（必须在创建内容之前，因为子类可能在 createContent 中需要访问标题栏状态）
        titleBar = createTitleBar();
        root.setTop(titleBar);

        // 4. 创建内容区域（由子类实现具体的 UI 布局）
        Parent content = createContent();
        if (content == null) {
            throw new IllegalStateException("createContent() 不能返回 null，请在子类中正确实现此方法");
        }
        root.setCenter(content);

        // 5. 创建场景并设置透明背景
        scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        scene.setFill(Color.TRANSPARENT);

        // 6. 应用菜单主题CSS
        applyMenuThemeCSS();

        // 7. 启用窗口拖拽和边缘调整大小功能
        enableDrag();
        enableResize(scene);

        // 8. 配置窗口属性并显示
        if (Objects.nonNull(appIcon())) {
            stage.getIcons().add(appIcon());
        }

        if (!appTitle().isEmpty()) {
            stage.setTitle(appTitle());
        }

        // 设置窗口最小尺寸约束
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);

        stage.setScene(scene);
        stage.show();

        // 9. 延迟执行的 Windows 平台特定设置（设置窗口圆角）
        Platform.runLater(() -> {
            WinDef.HWND hWnd = FXNativeWindowsTools.getHWndByEnumeration();
            // 设置窗口圆角样式
            FXNativeWindowsTools.setWindowCornerPreference(hWnd, FXNativeWindowsTools.DwmWindowCornerPreference.ROUND);
        });

        // 10. 触发子类自定义初始化回调
        onTitleBarReady();
    }

    /*
     * 创建自定义标题栏
     * 使用栈结构管理左右两侧组件，方便后续动态添加按钮
     * 左侧栈：按入栈顺序从左往右显示（如：titleLabel -> menuBar）
     * 右侧栈：按出栈顺序从左往右显示（如：入栈顺序 closeBtn -> maxBtn -> minBtn，显示顺序 minBtn -> maxBtn -> closeBtn）
     */
    private HBox createTitleBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(TITLE_BAR_HEIGHT);
        bar.setMinHeight(TITLE_BAR_HEIGHT);
        bar.setMaxHeight(TITLE_BAR_HEIGHT);
        bar.setStyle("""
                    -fx-background-color: %s;
                    -fx-alignment: center-left;
                    -fx-padding: 0 6;
                """.formatted(currentTheme.titleBarBgColor()));

        // 标题栏整体使用默认光标（避免边缘调整大小光标影响）
        bar.setCursor(Cursor.DEFAULT);

        // 初始化左右两侧的栈和容器
        leftComponentsStack = new Stack<>();
        rightComponentsStack = new Stack<>();
        leftBox = new HBox();
        leftBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);  // 左侧组件垂直居中对齐
        rightBox = new HBox();
        rightBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);  // 右侧组件垂直居中对齐

        //  左侧组件区域 
        // 应用图标（可选）
        if (Objects.nonNull(appIcon())) {
            ImageView icon = new ImageView(appIcon());
            icon.setFitWidth(APP_ICON_SIZE);
            icon.setFitHeight(APP_ICON_SIZE);
            icon.setCursor(Cursor.DEFAULT);
            leftComponentsStack.push(icon);  // 图标入栈
        }

        // 标题文字
        titleLabel = new Label(appTitle());
        titleLabel.setStyle("-fx-text-fill: " + currentTheme.titleTextColor() + "; -fx-padding: 0 8;");
        titleLabel.setCursor(Cursor.DEFAULT);
        leftComponentsStack.push(titleLabel);  // 标题入栈

        // 菜单栏
        menuBar = new MenuBar();
        menuBar.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-text-base-color: %s;
                """.formatted(currentTheme.titleTextColor(), currentTheme.titleTextColor()));
        menuBar.setCursor(Cursor.DEFAULT);
        menuBar.setOnMouseEntered(e -> {
            scene.setCursor(Cursor.DEFAULT);
            menuBar.setCursor(Cursor.DEFAULT);
        });
        leftComponentsStack.push(menuBar);  // 菜单栏入栈

        // 从左侧栈中按入栈顺序取出组件并添加到左侧HBox（保持入栈顺序：图标 -> 标题 -> 菜单栏）
        Stack<javafx.scene.Node> tempStack = new Stack<>();
        while (!leftComponentsStack.isEmpty()) {
            tempStack.push(leftComponentsStack.pop());
        }
        while (!tempStack.isEmpty()) {
            javafx.scene.Node node = tempStack.pop();
            leftBox.getChildren().add(node);
            leftComponentsStack.push(node);  // 重新压回栈以便后续管理
        }


        //  中间弹性空白区域 
        spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setCursor(Cursor.DEFAULT);

        //  右侧组件区域 
        // 窗口控制按钮（按照期望的显示顺序反向入栈）
        closeBtn = createWindowButton("/icons/close.png", WindowButtonType.CLOSE);
        rightComponentsStack.push(closeBtn);  // 关闭按钮先入栈（最后显示）

        maxBtn = createWindowButton("/icons/max.png", WindowButtonType.MAXIMIZE);
        rightComponentsStack.push(maxBtn);    // 最大化按钮入栈

        minBtn = createWindowButton("/icons/min.png", WindowButtonType.MINIMIZE);
        rightComponentsStack.push(minBtn);    // 最小化按钮入栈

        // 如果启用置顶功能，添加置顶按钮
        if (isAlwaysOnTopEnabled()) {
            toTopBtn = createWindowButton("/icons/top.png", WindowButtonType.TO_TOP);
            rightComponentsStack.push(toTopBtn);  // 置顶按钮入栈
        }

        // 如果启用主题切换功能，添加主题切换按钮
        if (isThemeSwitchEnabled()) {
            // 默认暗色主题，显示切换到亮色主题的图标
            String themeIconPath = currentTheme.isDark() ? "/icons/lightTheme.png" : "/icons/darkTheme.png";
            ThemeBtn = createWindowButton(themeIconPath, WindowButtonType.THEME_SWITCHING);
            rightComponentsStack.push(ThemeBtn);  // 主题切换按钮入栈
        }


        // 从右侧栈中按出栈顺序取出组件并添加到右侧HBox（显示顺序：minBtn -> maxBtn -> closeBtn）
        while (!rightComponentsStack.isEmpty()) {
            rightBox.getChildren().add(rightComponentsStack.pop());
        }

        rightBox.setMinWidth(computeHBoxMinWidth(rightBox));
        leftBox.setMinWidth(computeHBoxMinWidth(leftBox));
        // 组装标题栏：左侧区域 + 弹性空白 + 右侧区域
        bar.getChildren().addAll(leftBox, spacer, rightBox);

        return bar;
    }
    private double computeHBoxMinWidth(HBox box) {
        double width = 0;

        for (javafx.scene.Node node : box.getChildren()) {
            if (node instanceof Region region) {
                width += region.getMinWidth();
            } else {
                width += node.prefWidth(-1);
            }
        }

        // 加上 HBox spacing
        width += box.getSpacing() * Math.max(0, box.getChildren().size() - 1);

        return width;
    }

    // 窗口控制按钮创建

    /*
     * 创建窗口控制按钮（最小化、最大化、关闭、置顶、主题切换）
     * 包含图标、固定尺寸、点击行为、鼠标悬停/按下效果
     */
    private Button createWindowButton(String iconPath, WindowButtonType type) {
        // 加载按钮图标
        ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
        iv.setFitWidth(WINDOW_BTN_ICON_SIZE);
        iv.setFitHeight(WINDOW_BTN_ICON_SIZE);
        iv.setPreserveRatio(true);

        // 如果是最大化按钮，保存图标引用以便后续切换
        if (type == WindowButtonType.MAXIMIZE) {
            maxBtnIcon = iv;
        }
        // 如果是置顶按钮，保存图标引用以便后续切换
        if (type == WindowButtonType.TO_TOP) {
            toTopBtnIcon = iv;
        }
        // 如果是主题切换按钮，保存图标引用以便后续切换
        if (type == WindowButtonType.THEME_SWITCHING) {
            themeBtnIcon = iv;
        }

        Button btn = new Button();
        btn.setGraphic(iv);

        // 设置固定尺寸（所有窗口按钮统一尺寸）
        btn.setPrefSize(WINDOW_BTN_WIDTH, WINDOW_BTN_HEIGHT);
        btn.setMinSize(WINDOW_BTN_WIDTH, WINDOW_BTN_HEIGHT);
        btn.setMaxSize(WINDOW_BTN_WIDTH, WINDOW_BTN_HEIGHT);
        btn.setFocusTraversable(false);  // 禁用 Tab 键焦点
        btn.setStyle(currentTheme.btnStyleTransparent());

        // 设置按钮的光标为默认样式（确保鼠标移到按钮上时总是显示标准箭头）
        btn.setCursor(Cursor.DEFAULT);

        // 绑定按钮点击行为
        switch (type) {
            case MINIMIZE -> btn.setOnAction(e -> stage.setIconified(true));       // 最小化到任务栏
            case MAXIMIZE -> btn.setOnAction(e -> toggleMaximize());                // 切换最大化/还原
            case CLOSE -> btn.setOnAction(e -> stage.close());                      // 关闭窗口
            case TO_TOP -> btn.setOnAction(e -> toggleAlwaysOnTop());              // 切换置顶状态
            case THEME_SWITCHING -> btn.setOnAction(e -> toggleTheme());           // 切换主题
        }

        // 配置鼠标悬停效果
        btn.setOnMouseEntered(e -> {
            // 强制将光标设置为默认样式（覆盖边缘调整大小的光标）
            scene.setCursor(Cursor.DEFAULT);
            btn.setCursor(Cursor.DEFAULT);

            if (type == WindowButtonType.CLOSE) {
                btn.setStyle(currentTheme.closeBtnStyleHover());  // 关闭按钮悬停显示红色
            } else {
                btn.setStyle(currentTheme.btnStyleHover());       // 其他按钮悬停显示半透明
            }
        });

        // 配置鼠标按下效果
        btn.setOnMousePressed(e -> {
            if (type == WindowButtonType.CLOSE) {
                btn.setStyle(currentTheme.closeBtnStylePressed());  // 关闭按钮按下显示深红色
            } else {
                btn.setStyle(currentTheme.btnStylePressed());       // 其他按钮按下显示更明显
            }
        });

        // 鼠标移出恢复透明背景
        btn.setOnMouseExited(e -> btn.setStyle(currentTheme.btnStyleTransparent()));

        return btn;
    }


    // 标题栏菜单扩展接口

    /*
     * 向标题栏菜单栏添加菜单
     * 子类可调用此方法在标题栏添加菜单项（如：文件、编辑、视图等）
     * @param menu 要添加的菜单
     */
    protected final void addTitleEventTarget(Menu menu) {
        // 为菜单设置主题样式
        applyMenuStyle(menu);

        // 添加到菜单栏
        menuBar.getMenus().add(menu);
    }

    /*
     * 向标题栏左侧添加组件
     * 新添加的组件会显示在左侧最右边（追加到左侧HBox末尾）
     * 适用场景：在菜单栏后面添加自定义组件
     * @param node 要添加的节点
     */
    protected final void addLeftComponent(javafx.scene.Node node) {
        node.setCursor(Cursor.DEFAULT);  // 设置默认光标
        leftBox.getChildren().add(node);
        leftComponentsStack.push(node);  // 同步到栈中以便管理
    }

    /*
     * 向标题栏右侧添加组件
     * 新添加的组件会显示在右侧最左边（插入到右侧HBox开头）
     * 适用场景：在最小化按钮前面添加自定义按钮（如主题切换、设置等）
     * @param node 要添加的节点
     */
    protected final void addRightComponent(javafx.scene.Node node) {
        node.setCursor(Cursor.DEFAULT);  // 设置默认光标
        rightBox.getChildren().addFirst(node);  // 插入到最前面
        rightComponentsStack.push(node);      // 同步到栈中以便管理
    }

    /*
     * 为单个菜单应用主题样式
     */
    private void applyMenuStyle(Menu menu) {
        menu.setStyle("""
                -fx-text-fill: %s;
                -fx-text-base-color: %s;
                """.formatted(currentTheme.titleTextColor(), currentTheme.titleTextColor()));
    }

    // 窗口拖拽功能

    /*
     * 启用标题栏拖拽移动窗口功能
     * 鼠标在标题栏上按下并拖动可移动窗口位置
     * 双击标题栏可切换最大化/还原状态
     */
    private void enableDrag() {
        // 记录拖拽起始点的场景坐标
        titleBar.setOnMousePressed(e -> {
            // 如果鼠标处于调整大小状态，禁止拖拽
            if (isResizeCursor()) return;

            dragOffsetX = e.getSceneX();
            dragOffsetY = e.getSceneY();
        });

        // 拖拽过程中更新窗口位置
        titleBar.setOnMouseDragged(e -> {
            // 最大化状态下禁止拖拽
            if (maximized) return;
            // 调整大小状态下禁止拖拽
            if (isResizeCursor()) return;

            // 计算新的窗口位置（屏幕坐标 - 鼠标在窗口内的偏移）
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        // 双击标题栏切换最大化
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !isResizeCursor()) {
                toggleMaximize();
            }
        });
    }

    /*
     * 检查当前鼠标光标是否为调整大小状态
     * 用于区分拖拽移动和边缘调整大小操作
     */
    private boolean isResizeCursor() {
        Cursor c = stage.getScene().getCursor();
        return c == Cursor.E_RESIZE
                || c == Cursor.W_RESIZE
                || c == Cursor.N_RESIZE
                || c == Cursor.S_RESIZE
                || c == Cursor.NE_RESIZE
                || c == Cursor.NW_RESIZE
                || c == Cursor.SE_RESIZE
                || c == Cursor.SW_RESIZE;
    }

    /*
     * 更新标题栏所有组件的光标样式
     * 用于在边缘调整大小时，让标题栏组件的光标与场景光标保持一致
     */
    private void updateTitleBarCursor(Cursor cursor) {
        titleBar.setCursor(cursor);
        titleBar.getChildren().forEach(node -> node.setCursor(cursor));
    }


    // 窗口最大化功能

    /*
     * 切换窗口最大化/还原状态
     * 最大化时填充当前屏幕的可视区域，还原时恢复到之前的位置和尺寸
     */
    private void toggleMaximize() {
        if (!maximized) {
            // 保存当前窗口位置和尺寸，用于还原
            lastX = stage.getX();
            lastY = stage.getY();
            lastW = stage.getWidth();
            lastH = stage.getHeight();

            // 获取窗口当前所在的屏幕（支持多显示器）
            Screen screen = getScreenForStage(stage);
            Rectangle2D bounds = screen.getVisualBounds();  // 可视区域（排除任务栏）

            // 设置窗口填充整个屏幕可视区域
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            maximized = true;

            // 切换到还原图标
            maxBtnIcon.setImage(new Image(getClass().getResourceAsStream("/icons/restoreIcon.png")));
        } else {
            // 先设置状态为非最大化
            maximized = false;

            // 使用 Platform.runLater 确保状态更新后再设置尺寸
            Platform.runLater(() -> {
                // 还原到之前的位置和尺寸
                stage.setX(lastX);
                stage.setY(lastY);
                stage.setWidth(lastW);
                stage.setHeight(lastH);
            });

            // 切换到最大化图标
            maxBtnIcon.setImage(new Image(getClass().getResourceAsStream("/icons/max.png")));
        }
    }

    /*
     * 切换窗口置顶状态
     * 切换窗口是否始终显示在其他窗口之上，并更新按钮图标
     */
    private void toggleAlwaysOnTop() {
        alwaysOnTop = !alwaysOnTop;
        stage.setAlwaysOnTop(alwaysOnTop);

        // 根据置顶状态切换图标
        String iconPath = alwaysOnTop ? "/icons/openTop.png" : "/icons/top.png";
        toTopBtnIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
    }

    /*
     * 切换窗口主题
     * 在暗色主题和亮色主题之间切换，并更新主题切换按钮的图标
     */
    private void toggleTheme() {
        // 切换主题：暗色 <-> 亮色
        WindowTheme newTheme = currentTheme.isDark() ? WindowTheme.light() : WindowTheme.dark();
        setTheme(newTheme);

        // 根据新主题切换按钮图标
        // 暗色主题显示亮色图标（表示可以切换到亮色）
        // 亮色主题显示暗色图标（表示可以切换到暗色）
        String iconPath = newTheme.isDark() ? "/icons/lightTheme.png" : "/icons/darkTheme.png";
        themeBtnIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
    }

    /*
     * 获取窗口当前所在的屏幕
     * 通过窗口中心点坐标判断位于哪个显示器
     */
    private Screen getScreenForStage(Stage stage) {
        // 计算窗口中心点坐标
        double centerX = stage.getX() + stage.getWidth() / 2;
        double centerY = stage.getY() + stage.getHeight() / 2;

        // 遍历所有屏幕，找到包含窗口中心点的屏幕
        for (Screen screen : Screen.getScreens()) {
            if (screen.getVisualBounds().contains(centerX, centerY)) {
                return screen;
            }
        }

        // 如果没有找到，返回主屏幕
        return Screen.getPrimary();
    }


    // 窗口边缘调整大小功能

    /*
     * 启用窗口边缘拖拽调整大小功能
     * 鼠标移动到窗口边缘时，光标会变化为调整大小样式
     * 按下并拖拽可调整窗口尺寸（支持八个方向：上下左右及四个角）
     */
    private void enableResize(Scene scene) {
        // 鼠标移动时检测是否靠近边缘，更新光标样式
        scene.setOnMouseMoved(e -> {
            // 最大化状态下禁用边缘调整大小
            if (maximized) {
                scene.setCursor(Cursor.DEFAULT);
                updateTitleBarCursor(Cursor.DEFAULT);
                return;
            }

            double x = e.getSceneX();
            double y = e.getSceneY();
            double w = scene.getWidth();
            double h = scene.getHeight();

            // 判断鼠标是否在各个边缘区域内
            boolean left = x < RESIZE_MARGIN;
            boolean right = x > w - RESIZE_MARGIN;
            boolean top = y < RESIZE_MARGIN;
            boolean bottom = y > h - RESIZE_MARGIN;

            // 如果鼠标在标题栏主体区域（不在边缘），使用默认光标
            // 标题栏主体区域 = 标题栏范围内 && 不在上边缘 && 不在左右边缘
            if (y >= RESIZE_MARGIN && y < TITLE_BAR_HEIGHT && !left && !right) {
                scene.setCursor(Cursor.DEFAULT);
                updateTitleBarCursor(Cursor.DEFAULT);
                return;
            }

            // 根据边缘位置设置对应的光标样式
            Cursor cursor = Cursor.DEFAULT;

            if (left && top) cursor = Cursor.NW_RESIZE;         // 左上角
            else if (left && bottom) cursor = Cursor.SW_RESIZE;  // 左下角
            else if (right && top) cursor = Cursor.NE_RESIZE;    // 右上角
            else if (right && bottom) cursor = Cursor.SE_RESIZE; // 右下角
            else if (left) cursor = Cursor.W_RESIZE;             // 左边
            else if (right) cursor = Cursor.E_RESIZE;            // 右边
            else if (top) cursor = Cursor.N_RESIZE;              // 上边
            else if (bottom) cursor = Cursor.S_RESIZE;           // 下边

            scene.setCursor(cursor);
            // 同步更新标题栏组件的光标，让边缘调整大小光标能够正确显示
            updateTitleBarCursor(cursor);
        });

        // 记录拖拽开始时的窗口位置和尺寸
        scene.setOnMousePressed(e -> {
            resizeStartX = stage.getX();
            resizeStartY = stage.getY();
            resizeStartW = stage.getWidth();
            resizeStartH = stage.getHeight();
        });

        // 根据当前光标类型执行相应的调整大小操作
        scene.setOnMouseDragged(e -> {
            // 最大化状态下禁止调整大小
            if (maximized) return;

            Cursor c = scene.getCursor();

            //  调整右边或右侧相关边缘 
            if (c == Cursor.E_RESIZE || c == Cursor.NE_RESIZE || c == Cursor.SE_RESIZE) {
                // 新宽度 = 鼠标X坐标 - 窗口左边界
                stage.setWidth(Math.max(MIN_WINDOW_WIDTH, e.getScreenX() - resizeStartX));
            }

            //  调整下边或下侧相关边缘 
            if (c == Cursor.S_RESIZE || c == Cursor.SE_RESIZE || c == Cursor.SW_RESIZE) {
                // 新高度 = 鼠标Y坐标 - 窗口上边界
                stage.setHeight(Math.max(MIN_WINDOW_HEIGHT, e.getScreenY() - resizeStartY));
            }

            //  调整左边或左侧相关边缘 
            if (c == Cursor.W_RESIZE || c == Cursor.NW_RESIZE || c == Cursor.SW_RESIZE) {
                double newX = e.getScreenX();
                double newW = resizeStartX + resizeStartW - newX;  // 计算新宽度
                // 只有新宽度满足最小宽度限制时才更新
                if (newW >= MIN_WINDOW_WIDTH) {
                    stage.setX(newX);      // 同步移动窗口左边界
                    stage.setWidth(newW);
                }
            }

            //  调整上边或上侧相关边缘
            if (c == Cursor.N_RESIZE || c == Cursor.NW_RESIZE || c == Cursor.NE_RESIZE) {
                double newY = e.getScreenY();
                double newH = resizeStartY + resizeStartH - newY;  // 计算新高度
                // 只有新高度满足最小高度限制时才更新
                if (newH >= MIN_WINDOW_HEIGHT) {
                    stage.setY(newY);      // 同步移动窗口上边界
                    stage.setHeight(newH);
                }
            }
        });
    }

    // 主题切换功能

    /*
     * 更新菜单栏主题样式
     * 遍历所有菜单并应用新主题的文字颜色
     */
    private void updateMenuBarTheme() {
        // 更新 MenuBar 本身的样式
        menuBar.setStyle("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-text-base-color: %s;
                """.formatted(currentTheme.titleTextColor(), currentTheme.titleTextColor()));

        // 更新所有菜单的样式
        for (Menu menu : menuBar.getMenus()) {
            applyMenuStyle(menu);
        }
    }

    /*
     * 应用菜单项主题CSS
     * 将当前主题的菜单CSS动态添加到场景的样式表中
     */
    private void applyMenuThemeCSS() {
        // 移除旧的菜单CSS（如果存在）
        scene.getStylesheets().removeIf(s -> s.startsWith("data:text/css"));

        // 生成新的菜单CSS
        String menuCSS = currentTheme.generateMenuCSS();

        // 使用data URI将CSS添加到场景
        String dataUri = "data:text/css," + menuCSS.replace("\n", "").replace(" ", "%20");
        scene.getStylesheets().add(dataUri);
    }

    /*
     * 更新单个按钮的主题样式
     * 重新绑定鼠标事件处理器以使用新主题的样式
     */
    private void updateButtonTheme(Button btn, WindowButtonType type) {
        // 重置为默认样式
        btn.setStyle(currentTheme.btnStyleTransparent());

        // 重新绑定鼠标悬停效果
        btn.setOnMouseEntered(e -> {
            if (type == WindowButtonType.CLOSE) {
                btn.setStyle(currentTheme.closeBtnStyleHover());
            } else {
                btn.setStyle(currentTheme.btnStyleHover());
            }
        });

        // 重新绑定鼠标按下效果
        btn.setOnMousePressed(e -> {
            if (type == WindowButtonType.CLOSE) {
                btn.setStyle(currentTheme.closeBtnStylePressed());
            } else {
                btn.setStyle(currentTheme.btnStylePressed());
            }
        });


        // 重新绑定鼠标移出效果
        btn.setOnMouseExited(e -> btn.setStyle(currentTheme.btnStyleTransparent()));
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private enum WindowButtonType {
        MINIMIZE,   // 最小化按钮
        MAXIMIZE,   // 最大化/还原按钮
        CLOSE,      // 关闭按钮
        TO_TOP,
        THEME_SWITCHING
    }
}
