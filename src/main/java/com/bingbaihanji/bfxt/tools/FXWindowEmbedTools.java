package com.bingbaihanji.bfxt.tools;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.stage.Stage;

import static com.bingbaihanji.bfxt.tools.FXNativeWindowsTools.Win32Constants.WindowLongIndex.GWL_STYLE;
import static com.bingbaihanji.bfxt.tools.FXNativeWindowsTools.Win32Constants.WindowStyle.*;

/**
 * JavaFX 窗口嵌入工具类
 *
 * <p>
 * 该工具类专门用于处理 JavaFX 窗口与外部应用窗口之间的嵌入操作。
 * 提供了三层结构的窗口嵌入方案：JavaFX Stage → Win32 Host 窗口 → 外部应用窗口。
 * </p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>创建 Win32 Host 窗口作为嵌入容器</li>
 *   <li>将外部应用窗口嵌入到 JavaFX 窗口中</li>
 *   <li>动态更新嵌入窗口的位置和大小</li>
 *   <li>销毁和分离嵌入的窗口</li>
 *   <li>管理窗口父子关系</li>
 * </ul>
 *
 * <p><b>平台要求：</b></p>
 * <ul>
 *   <li>仅支持 Windows 平台</li>
 *   <li>需要 JNA 和 JNA-Platform 依赖</li>
 * </ul>
 *
 * @author bingbaihanji
 * @version 1.0
 * @date 2026-01-13
 */
public class FXWindowEmbedTools {

    /**
     * 将 JavaFX Node 的 Scene 坐标转换为 Windows 原生窗口客户区坐标
     *
     * <p>
     * JavaFX 中 Node 的坐标是相对于 Scene 的，而 Win32 API 需要的是相对于原生窗口客户区的坐标。
     * 这两个坐标系统之间通常有偏移（由窗口装饰、标题栏等引起）。
     * </p>
     *
     * @param node         JavaFX 节点
     * @param stage        JavaFX 舞台
     * @param fxWindowHwnd JavaFX 窗口的原生句柄
     * @return 包含转换后坐标的 WinDef.POINT 对象
     */
    public static WinDef.POINT convertNodeToNativeClientCoordinates(Node node, Stage stage, WinDef.HWND fxWindowHwnd) {
        if (node == null || stage == null || fxWindowHwnd == null) {
            throw new IllegalArgumentException("参数不能为 null");
        }

        // 获取 Node 在 Scene 中的边界
        Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());

        // Scene 的坐标 + Stage 的屏幕位置 = 屏幕坐标
        double screenX = stage.getX() + boundsInScene.getMinX() + node.getScene().getX();
        double screenY = stage.getY() + boundsInScene.getMinY() + node.getScene().getY();

        // 创建屏幕坐标点
        WinDef.POINT screenPoint = new WinDef.POINT((int) screenX, (int) screenY);

        // 将屏幕坐标转换为窗口客户区坐标
        FXNativeWindowsTools.User32Api.INSTANCE.ScreenToClient(fxWindowHwnd, screenPoint);

        return screenPoint;
    }

    /**
     * 将外部窗口嵌入到 JavaFX Node 容器中（自动处理坐标转换）
     *
     * <p>
     * 这是推荐的高级方法，直接接受 JavaFX Node 作为容器，自动处理坐标系统转换。
     * 使用三层结构：JavaFX Stage → Win32 Host 窗口 → 外部应用窗口
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>{@code
     * Pane embedContainer = new Pane();
     * // ... 将 embedContainer 添加到 Scene 中
     *
     * Platform.runLater(() -> {
     *     WinDef.HWND fxHwnd = FXNativeWindowsTools.getHWndByEnumeration();
     *     WinDef.HWND externalHwnd = User32.INSTANCE.FindWindow("Notepad", null);
     *
     *     WinDef.HWND hostHwnd = FXWindowEmbedTools.embedExternalWindowInNode(
     *         externalHwnd,
     *         fxHwnd,
     *         embedContainer,
     *         stage
     *     );
     * });
     * }</pre>
     *
     * @param externalHwnd 要嵌入的外部窗口句柄
     * @param fxWindowHwnd JavaFX 窗口句柄
     * @param container    JavaFX 容器节点（嵌入窗口将放置在此容器中）
     * @param stage        JavaFX 舞台
     * @return Host 窗口句柄（用于后续更新位置），失败返回 null
     */
    public static WinDef.HWND embedExternalWindowInNode(
            WinDef.HWND externalHwnd,
            WinDef.HWND fxWindowHwnd,
            Node container,
            Stage stage) {

        if (externalHwnd == null || fxWindowHwnd == null || container == null || stage == null) {
            throw new IllegalArgumentException("参数不能为 null");
        }

        // 转换坐标
        WinDef.POINT nativePos = convertNodeToNativeClientCoordinates(container, stage, fxWindowHwnd);

        // 获取容器的大小
        int width = (int) container.getLayoutBounds().getWidth();
        int height = (int) container.getLayoutBounds().getHeight();

        System.out.println("✓ 转换坐标: JavaFX容器位置 → 原生客户区坐标 (" + nativePos.x + ", " + nativePos.y + ")");

        // 使用转换后的坐标嵌入窗口
        return embedExternalWindow(externalHwnd, fxWindowHwnd, nativePos.x, nativePos.y, width, height);
    }

    /**
     * 更新嵌入窗口的位置和大小（基于 JavaFX Node，自动处理坐标转换）
     *
     * <p>
     * 当 JavaFX 容器的位置或大小改变时，调用此方法自动同步 Host 窗口和嵌入的外部窗口。
     * </p>
     *
     * @param hostHwnd     Host 窗口句柄
     * @param externalHwnd 外部窗口句柄
     * @param fxWindowHwnd JavaFX 窗口句柄
     * @param container    JavaFX 容器节点
     * @param stage        JavaFX 舞台
     */
    public static void updateHostWindowFromNode(
            WinDef.HWND hostHwnd,
            WinDef.HWND externalHwnd,
            WinDef.HWND fxWindowHwnd,
            Node container,
            Stage stage) {

        if (hostHwnd == null || container == null || stage == null || fxWindowHwnd == null) {
            return;
        }

        // 转换坐标
        WinDef.POINT nativePos = convertNodeToNativeClientCoordinates(container, stage, fxWindowHwnd);

        // 获取容器的大小
        int width = (int) container.getLayoutBounds().getWidth();
        int height = (int) container.getLayoutBounds().getHeight();

        // 更新窗口
        updateHostWindow(hostHwnd, externalHwnd, nativePos.x, nativePos.y, width, height);
    }


    /**
     * 创建一个原生 Win32 窗口作为嵌入容器
     *
     * <p>
     * 创建一个简单的 Win32 子窗口，用作嵌入外部窗口的真正容器。
     * 这个窗口会成为 JavaFX 窗口和外部窗口之间的桥梁。
     * </p>
     *
     * @param parentHwnd 父窗口句柄（JavaFX 窗口）
     * @param x          窗口 X 坐标（相对于父窗口客户区）
     * @param y          窗口 Y 坐标（相对于父窗口客户区）
     * @param width      窗口宽度
     * @param height     窗口高度
     * @return 创建的 Host 窗口句柄，失败返回 null
     */
    public static WinDef.HWND createHostWindow(WinDef.HWND parentHwnd, int x, int y, int width, int height) {
        if (parentHwnd == null) {
            throw new IllegalArgumentException("父窗口句柄不能为 null");
        }

        try {
            // 使用 "STATIC" 窗口类创建一个简单的子窗口
            WinDef.HWND hostHwnd = User32.INSTANCE.CreateWindowEx(
                    0,                              // dwExStyle
                    "STATIC",                       // lpClassName - 使用系统预定义的 STATIC 类
                    "FX_HOST",                      // lpWindowName
                    (int) (WS_CHILD | WS_VISIBLE),  // dwStyle
                    x, y, width, height,            // 位置和大小
                    parentHwnd,                     // hWndParent
                    null,                           // hMenu
                    null,                           // hInstance
                    null                            // lpParam
            );

            if (hostHwnd == null || Pointer.nativeValue(hostHwnd.getPointer()) == 0) {
                System.err.println("创建 Host 窗口失败");
                return null;
            }

            System.out.println("✓ 创建 Host 窗口: 0x" + Long.toHexString(Pointer.nativeValue(hostHwnd.getPointer())));
            return hostHwnd;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将外部窗口嵌入到 JavaFX 窗口（使用三层结构）
     *
     * <p>
     * 这是推荐的嵌入方法，使用三层结构：
     * JavaFX Stage → Win32 Host 窗口 → 外部应用窗口
     * </p>
     *
     * <p><b>工作流程：</b></p>
     * <ol>
     *   <li>创建一个原生 Win32 Host 窗口作为中间层</li>
     *   <li>将 Host 窗口设置为 JavaFX 窗口的子窗口</li>
     *   <li>将外部应用窗口嵌入到 Host 窗口中</li>
     *   <li>同步 Host 窗口位置到 JavaFX 容器区域</li>
     * </ol>
     *
     * @param externalHwnd 要嵌入的外部窗口句柄
     * @param fxWindowHwnd JavaFX 窗口句柄
     * @param x            嵌入位置 X（相对于 JavaFX 客户区）
     * @param y            嵌入位置 Y（相对于 JavaFX 客户区）
     * @param width        嵌入窗口宽度
     * @param height       嵌入窗口高度
     * @return Host 窗口句柄（用于后续更新位置），失败返回 null
     */
    public static WinDef.HWND embedExternalWindow(
            WinDef.HWND externalHwnd,
            WinDef.HWND fxWindowHwnd,
            int x, int y, int width, int height) {

        if (externalHwnd == null || fxWindowHwnd == null) {
            throw new IllegalArgumentException("窗口句柄不能为 null");
        }

        // 步骤1: 创建 Host 窗口（作为真正的容器）
        WinDef.HWND hostHwnd = createHostWindow(fxWindowHwnd, x, y, width, height);
        if (hostHwnd == null) {
            System.err.println("创建 Host 窗口失败");
            return null;
        }

        // 步骤2: 将外部窗口嵌入到 Host 窗口
        embedToParent(externalHwnd, hostHwnd);

        // 步骤3: 调整外部窗口大小以填充 Host 窗口
        User32.INSTANCE.SetWindowPos(
                externalHwnd,
                null,
                0, 0,           // 相对于 Host 窗口的位置
                width, height,
                0x0040 | 0x0004 | 0x0020  // SWP_SHOWWINDOW | SWP_NOZORDER | SWP_FRAMECHANGED
        );

        // 步骤4: 设置焦点到外部窗口，确保可以接收键盘输入
        User32.INSTANCE.SetFocus(externalHwnd);

        // 步骤5: 强制刷新窗口显示
        User32.INSTANCE.InvalidateRect(externalHwnd, null, true);
        User32.INSTANCE.UpdateWindow(externalHwnd);

        System.out.println("✓ 外部窗口已嵌入到三层结构中");
        return hostHwnd;
    }

    /**
     * 更新 Host 窗口的位置和大小（自动同步外部窗口）
     *
     * <p>
     * 当 JavaFX 容器的位置或大小改变时，调用此方法同步 Host 窗口和嵌入的外部窗口。
     * </p>
     *
     * @param hostHwnd     Host 窗口句柄
     * @param externalHwnd 外部窗口句柄
     * @param x            新的 X 坐标（相对于 JavaFX 客户区）
     * @param y            新的 Y 坐标（相对于 JavaFX 客户区）
     * @param width        新的宽度
     * @param height       新的高度
     */
    public static void updateHostWindow(WinDef.HWND hostHwnd, WinDef.HWND externalHwnd, int x, int y, int width, int height) {
        if (hostHwnd == null) {
            return;
        }

        // 更新 Host 窗口位置和大小
        User32.INSTANCE.SetWindowPos(
                hostHwnd,
                null,
                x, y, width, height,
                0x0040 | 0x0004  // SWP_SHOWWINDOW | SWP_NOZORDER
        );

        // 同步更新外部窗口大小，确保填充整个 Host 窗口
        if (externalHwnd != null) {
            User32.INSTANCE.SetWindowPos(
                    externalHwnd,
                    null,
                    0, 0,  // 相对于 Host 窗口的位置
                    width, height,
                    0x0040 | 0x0004 | 0x0020  // SWP_SHOWWINDOW | SWP_NOZORDER | SWP_FRAMECHANGED
            );

            // 设置焦点，确保可以接收输入
            User32.INSTANCE.SetFocus(externalHwnd);

            // 刷新显示
            User32.INSTANCE.InvalidateRect(externalHwnd, null, true);
            User32.INSTANCE.UpdateWindow(externalHwnd);
        }
    }

    /**
     * 销毁 Host 窗口并分离外部窗口
     *
     * <p>
     * 清理三层结构，恢复外部窗口为独立窗口。
     * </p>
     *
     * @param hostHwnd     Host 窗口句柄
     * @param externalHwnd 外部窗口句柄
     */
    public static void destroyHostWindow(WinDef.HWND hostHwnd, WinDef.HWND externalHwnd) {
        if (externalHwnd != null) {
            // 先分离外部窗口
            detachFromParent(externalHwnd);
        }

        if (hostHwnd != null) {
            // 销毁 Host 窗口
            User32.INSTANCE.DestroyWindow(hostHwnd);
            System.out.println("✓ Host 窗口已销毁");
        }
    }

    /**
     * 将子窗口嵌入到父窗口（用于三层结构中的嵌入操作）
     *
     * <p>
     * 将 JavaFX 独立窗口或外部应用窗口转换为子窗口，嵌入到指定的父窗口中。
     * 这个方法会修改窗口样式，移除顶层窗口特性（如标题栏、边框），
     * 并添加子窗口样式，使其成为父窗口的一部分。
     * </p>
     *
     * <p><b>注意：</b></p>
     * <ul>
     *   <li>对于嵌入外部应用到 JavaFX，推荐使用 {@link #embedExternalWindow} 方法（三层结构）</li>
     *   <li>该方法是三层结构的底层实现，通常不需要直接调用</li>
     * </ul>
     *
     * @param childHwnd  要嵌入的窗口句柄
     * @param parentHwnd 目标父窗口句柄
     * @implNote 仅适用于 Windows 平台
     */
    public static void embedToParent(WinDef.HWND childHwnd, WinDef.HWND parentHwnd) {
        if (childHwnd == null || parentHwnd == null) {
            throw new IllegalArgumentException("窗口句柄不能为 null");
        }

        // 步骤1: 设置父窗口关系
        User32.INSTANCE.SetParent(childHwnd, parentHwnd);

        // 步骤2: 获取当前窗口样式（GWL_STYLE，不是 GWL_EXSTYLE）
        BaseTSD.LONG_PTR currentStyle = FXNativeWindowsTools.User32Api.INSTANCE.GetWindowLongPtr(
                childHwnd,
                GWL_STYLE
        );

        long newStyle = currentStyle.longValue();

        // 步骤3: 移除顶层窗口特性
        // WS_POPUP - 弹出窗口样式
        // WS_CAPTION - 标题栏
        // WS_THICKFRAME - 可调整大小的边框
        newStyle &= ~WS_POPUP;
        newStyle &= ~WS_CAPTION;
        newStyle &= ~WS_THICKFRAME;

        // 步骤4: 添加子窗口样式
        // WS_CHILD - 子窗口样式（必需）
        // WS_VISIBLE - 可见样式
        newStyle |= FXNativeWindowsTools.Win32Constants.WindowStyle.WS_CHILD;
        newStyle |= FXNativeWindowsTools.Win32Constants.WindowStyle.WS_VISIBLE;

        // 步骤5: 应用新的窗口样式
        FXNativeWindowsTools.User32Api.INSTANCE.SetWindowLongPtr(
                childHwnd,
                GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        // 步骤6: 刷新窗口以应用样式变化
        User32.INSTANCE.SetWindowPos(
                childHwnd,
                null,
                0, 0, 0, 0,
                0x0001 | 0x0002 | 0x0004 | 0x0020 // SWP_NOSIZE | SWP_NOMOVE | SWP_NOZORDER | SWP_FRAMECHANGED
        );
    }

    /**
     * 从父窗口中分离子窗口，恢复为独立窗口
     *
     * <p>
     * 将之前嵌入的子窗口恢复为独立的顶层窗口。
     * 这个方法会移除父窗口关系，并恢复顶层窗口的样式（如标题栏、边框）。
     * </p>
     *
     * @param childHwnd 要分离的子窗口句柄
     * @implNote 仅适用于 Windows 平台
     */
    public static void detachFromParent(WinDef.HWND childHwnd) {
        if (childHwnd == null) {
            throw new IllegalArgumentException("窗口句柄不能为 null");
        }

        // 步骤1: 移除父窗口关系（设置为桌面窗口）
        User32.INSTANCE.SetParent(childHwnd, null);

        // 步骤2: 获取当前窗口样式
        BaseTSD.LONG_PTR currentStyle = FXNativeWindowsTools.User32Api.INSTANCE.GetWindowLongPtr(
                childHwnd,
                GWL_STYLE
        );

        long newStyle = currentStyle.longValue();

        // 步骤3: 移除子窗口样式
        newStyle &= ~WS_CHILD;

        // 步骤4: 添加顶层窗口样式
        newStyle |= WS_OVERLAPPEDWINDOW;
        newStyle |= WS_VISIBLE;

        // 步骤5: 应用新的窗口样式
        FXNativeWindowsTools.User32Api.INSTANCE.SetWindowLongPtr(
                childHwnd,
                GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        // 步骤6: 刷新窗口以应用样式变化
        User32.INSTANCE.SetWindowPos(
                childHwnd,
                null,
                0, 0, 0, 0,
                0x0001 | 0x0002 | 0x0004 | 0x0020 // SWP_NOSIZE | SWP_NOMOVE | SWP_NOZORDER | SWP_FRAMECHANGED
        );
    }

    /**
     * 获取窗口的父窗口句柄
     *
     * <p>
     * 返回指定窗口的父窗口句柄。如果窗口没有父窗口（独立顶层窗口），返回 null。
     * </p>
     *
     * @param hwnd 窗口句柄
     * @return 父窗口句柄，如果没有父窗口则返回 null
     * @implNote 仅适用于 Windows 平台
     */
    public static WinDef.HWND getParentWindow(WinDef.HWND hwnd) {
        if (hwnd == null) {
            return null;
        }

        WinDef.HWND parent = User32.INSTANCE.GetParent(hwnd);
        // GetParent 返回 null 或 0 表示没有父窗口
        if (parent == null || Pointer.nativeValue(parent.getPointer()) == 0) {
            return null;
        }
        return parent;
    }
}
