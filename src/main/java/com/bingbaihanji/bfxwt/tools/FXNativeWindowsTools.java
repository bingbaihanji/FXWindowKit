package com.bingbaihanji.bfxwt.tools;


import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX 原生窗口工具类
 *
 * @author bingbaihanji
 * @version 1.0
 * @date 2026-01-12 16:49:28
 */
public class FXNativeWindowsTools {


    /*
     *
     * 以下方法需要添加 JVM 参数才能使用：
     * getHWnd()
     * getHWndOfReflection()
     * --add-opens javafx.graphics/com.sun.glass.ui=com.bingbaihanji
     * --add-exports javafx.graphics/com.sun.glass.ui=com.bingbaihanji
     *
     * 推荐使用下面的新方法替代（无需 JVM 参数）：
     * - getHWndByEnumeration() - 推荐
     * - getHWndByTitle(Stage stage)
     * - getAllHWndByEnumeration()
     */

    /*
    /**
     * 获取当前 JavaFX 应用窗口对应的 Windows 原生窗口句柄（HWND）。
     *
     * <p>
     * 该方法通过 JavaFX 内部的 {@code com.sun.glass.ui.Window} API，
     * 遍历当前已创建的窗口列表，找到第一个拥有原生窗口句柄的窗口，
     * 并将其 nativeWindow（long 类型）封装为 JNA 的 {@link WinDef.HWND}。
     * </p>
     *
     * <p>
     * 主要用于在 Windows 平台下，通过 JNA 调用 Win32 API，
     * 对 JavaFX 窗口进行底层操作（如置顶、透明、样式修改、消息发送等）。
     * </p>
     *
     * @return <ul>
     * <li>返回 {@link WinDef.HWND}：成功获取到 JavaFX 主窗口的 HWND</li>
     * <li>返回 {@code null}：当前尚未创建窗口，或获取过程中发生异常</li>
     * </ul>
     * @implNote <ul>
     * <li>该方法依赖 {@code com.sun.glass.ui} 内部 API，存在一定的不稳定性</li>
     * <li>仅适用于 Windows 平台</li>
     * <li>应在 JavaFX Application Thread 且窗口已显示后调用</li>
     * <li>jdk9 及以上版本需要指定jvm参数才能访问 com.sun.glass.ui 包(模块化系统):</br>
     *  - 使用直接访问时: --add-exports javafx.graphics/com.sun.glass.ui=com.bingbaihanji</br>
     *  - 使用反射访问时: --add-opens javafx.graphics/com.sun.glass.ui=com.bingbaihanji
     * </li>
     * </ul>
     * @deprecated 推荐使用 {@link #getHWndByEnumeration()} 替代，无需 JVM 参数
     */


//    public static WinDef.HWND getHWnd() {
//        try {
//            com.sun.glass.ui.Window window =
//                    com.sun.glass.ui.Window.getWindows().stream()
//                            .filter(w -> w.getNativeWindow() != 0)
//                            .findFirst()
//                            .orElse(null);
//
//            if (window == null) {
//                return null;
//            }
//
//            long hwnd = window.getNativeWindow();
//            return new WinDef.HWND(new Pointer(hwnd));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    // 通过反射获取javafx窗口句柄
    public static WinDef.HWND getHWndOfReflection() {
        try {
            // 使用反射访问 com.sun.glass.ui.Window 类
            Class<?> windowClass = Class.forName("com.sun.glass.ui.Window");

            // 调用静态方法 getWindows() 获取所有窗口列表
            Method getWindowsMethod = windowClass.getMethod("getWindows");
            getWindowsMethod.setAccessible(true);

            @SuppressWarnings("unchecked")
            List<Object> windows = (List<Object>) getWindowsMethod.invoke(null);

            // 获取 getNativeWindow() 方法
            Method getNativeWindowMethod = windowClass.getMethod("getNativeWindow");
            getNativeWindowMethod.setAccessible(true);

            // 遍历窗口列表，找到第一个有效的原生窗口句柄
            for (Object window : windows) {
                Long nativeHandle = (Long) getNativeWindowMethod.invoke(window);
                if (nativeHandle != null && nativeHandle != 0) {
                    return new WinDef.HWND(new Pointer(nativeHandle));
                }
            }

            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 通过窗口标题查找 JavaFX 窗口句柄（不需要 JVM 参数）
     *
     * <p>
     * 该方法通过 Windows API 的 FindWindow 函数，根据窗口标题查找窗口句柄。
     * <b>无需任何 JVM 参数或模块访问权限</b>，是最简单的获取窗口句柄的方式。
     * </p>
     *
     * <p><b>使用步骤：</b></p>
     * <ol>
     *   <li>为 JavaFX Stage 设置一个唯一的标题（建议使用 UUID 或时间戳）</li>
     *   <li>调用此方法传入该标题即可获取窗口句柄</li>
     * </ol>
     *
     * <p><b>使用示例：</b></p>
     * <pre>{@code
     * Stage stage = new Stage();
     * stage.setTitle("MyApp-" + System.currentTimeMillis());
     * stage.show();
     *
     * // 等待窗口完全显示
     * Platform.runLater(() -> {
     *     WinDef.HWND hwnd = FXNativeTools.getHWndByTitle(stage.getTitle());
     *     if (hwnd != null) {
     *         // 成功获取窗口句柄
     *     }
     * });
     * }</pre>
     *
     * @param windowTitle 窗口标题（必须与 Stage.setTitle() 设置的标题完全一致）
     * @return <ul>
     * <li>返回 {@link WinDef.HWND}：成功找到窗口</li>
     * <li>返回 {@code null}：未找到匹配标题的窗口</li>
     * </ul>
     * @implNote <ul>
     * <li>仅适用于 Windows 平台</li>
     * <li>标题匹配是精确匹配，需要完全一致</li>
     * <li>如果有多个窗口使用相同标题，返回第一个找到的</li>
     * <li><b>优点：无需 JVM 参数，简单易用</b></li>
     * <li><b>缺点：需要设置唯一标题，标题可能被用户看到</b></li>
     * </ul>
     */
    public static WinDef.HWND getHWndByTitle(String windowTitle) {
        if (windowTitle == null || windowTitle.isEmpty()) {
            return null;
        }
        return User32.INSTANCE.FindWindow(null, windowTitle);
    }


    /**
     * 通过窗口标题查找 JavaFX 窗口句柄（重载方法，直接传入 Stage）
     *
     * <p>
     * 便捷方法，直接传入 JavaFX Stage 对象，自动获取其标题进行查找。
     * </p>
     *
     * <p><b>使用示例：</b></p>
     * <pre>{@code
     * Stage stage = new Stage();
     * stage.setTitle("MyApp-" + System.currentTimeMillis());
     * stage.show();
     *
     * Platform.runLater(() -> {
     *     WinDef.HWND hwnd = FXNativeTools.getHWndByTitle(stage);
     * });
     * }</pre>
     *
     * @param stage JavaFX Stage 对象
     * @return 窗口句柄，未找到则返回 null
     */
    public static WinDef.HWND getHWndByTitle(Stage stage) {
        if (stage == null) {
            return null;
        }
        return getHWndByTitle(stage.getTitle());
    }


    /**
     * 枚举当前进程的所有窗口，获取第一个 JavaFX 窗口句柄（不需要 JVM 参数）
     *
     * <p>
     * 该方法通过枚举系统所有窗口，筛选出属于当前 Java 进程且类名为 JavaFX 窗口类的窗口。
     * <b>无需任何 JVM 参数或模块访问权限</b>，且不需要设置特殊的窗口标题。
     * </p>
     *
     * <p><b>工作原理：</b></p>
     * <ol>
     *   <li>调用 EnumWindows 枚举所有顶层窗口</li>
     *   <li>获取每个窗口所属的进程 ID</li>
     *   <li>筛选出属于当前 Java 进程的窗口</li>
     *   <li>进一步筛选窗口类名为 "GlassWndClass-GlassWindowClass-*" 的 JavaFX 窗口</li>
     *   <li>返回第一个匹配的窗口句柄</li>
     * </ol>
     *
     * <p><b>使用示例：</b></p>
     * <pre>{@code
     * Stage stage = new Stage();
     * stage.show();
     *
     * // 等待窗口完全显示
     * Platform.runLater(() -> {
     *     WinDef.HWND hwnd = FXNativeTools.getHWndByEnumeration();
     *     if (hwnd != null) {
     *         // 成功获取窗口句柄
     *     }
     * });
     * }</pre>
     *
     * @return <ul>
     * <li>返回 {@link WinDef.HWND}：成功找到 JavaFX 窗口</li>
     * <li>返回 {@code null}：未找到属于当前进程的 JavaFX 窗口</li>
     * </ul>
     * @implNote <ul>
     * <li>仅适用于 Windows 平台</li>
     * <li>如果有多个 JavaFX 窗口，返回第一个找到的</li>
     * <li><b>优点：无需 JVM 参数，无需设置特殊标题，自动识别</b></li>
     * <li><b>缺点：性能略低于直接获取（需要枚举所有窗口）</b></li>
     * <li>推荐在应用启动后、窗口完全显示后调用</li>
     * </ul>
     */
    public static WinDef.HWND getHWndByEnumeration() {
        // 获取当前进程 ID
        int currentPid = Kernel32Api.INSTANCE.GetCurrentProcessId();
        // 存储找到的窗口句柄
        List<WinDef.HWND> result = new ArrayList<>();

        // 枚举所有窗口
        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            // 获取窗口所属的进程 ID
            IntByReference windowPid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, windowPid);

            // 检查是否属于当前进程
            if (windowPid.getValue() == currentPid) {
                // 获取窗口类名
                char[] className = new char[256];
                User32.INSTANCE.GetClassName(hWnd, className, className.length);
                String classNameStr = new String(className).trim();

                // JavaFX 窗口的类名通常是 "GlassWndClass-GlassWindowClass-*"
                if (classNameStr.startsWith("GlassWndClass")) {
                    result.add(hWnd);
                    return false; // 停止枚举，找到第一个就返回
                }
            }
            return true; // 继续枚举
        }, null);

        return result.isEmpty() ? null : result.get(0);
    }


    /**
     * 获取当前进程的所有 JavaFX 窗口句柄列表（不需要 JVM 参数）
     *
     * <p>
     * 与 {@link #getHWndByEnumeration()} 类似，但返回所有找到的 JavaFX 窗口，
     * 而不是只返回第一个。适用于多窗口应用。
     * </p>
     *
     * @return JavaFX 窗口句柄列表，如果没有找到则返回空列表
     */
    public static List<WinDef.HWND> getAllHWndByEnumeration() {
        int currentPid = Kernel32Api.INSTANCE.GetCurrentProcessId();
        List<WinDef.HWND> result = new ArrayList<>();

        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            IntByReference windowPid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, windowPid);

            if (windowPid.getValue() == currentPid) {
                char[] className = new char[256];
                User32.INSTANCE.GetClassName(hWnd, className, className.length);
                String classNameStr = new String(className).trim();

                if (classNameStr.startsWith("GlassWndClass")) {
                    result.add(hWnd);
                }
            }
            return true; // 继续枚举所有窗口
        }, null);

        return result;
    }


    /**
     * 启用窗口拖拽功能（用于无边框窗口）
     *
     * <p>
     * 通过在指定的 JavaFX Node 上添加鼠标事件监听器，
     * 实现点击拖拽该 Node 时移动整个窗口的效果。
     * 常用于自定义标题栏的无边框窗口。
     * </p>
     *
     * @param hwnd     窗口句柄
     * @param dragArea 可拖拽的 JavaFX 节点（通常是标题栏区域）
     */
    public static void enableWindowDrag(WinDef.HWND hwnd, Node dragArea) {
        dragArea.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) {
                User32Api.INSTANCE.ReleaseCapture();
                User32Api.INSTANCE.SendMessage(
                        hwnd,
                        Win32Constants.WindowMessage.WM_NCLBUTTONDOWN,
                        new WinDef.WPARAM(Win32Constants.HitTestCode.HTCAPTION),
                        new WinDef.LPARAM(0)
                );
            }
        });
    }

    /**
     * 设置窗口系统背景样式（Acrylic / Mica）
     *
     * <p>
     * 设置 Windows 11 的 Acrylic（亚克力）或 Mica（云母）背景效果。
     * </p>
     *
     * @param hwnd         窗口句柄
     * @param backdropType 背景类型（NONE、MICA、ACRYLIC）
     * @implNote 需要 Windows 11 及以上版本
     */
    public static void setSystemStageStyle(
            WinDef.HWND hwnd,
            SystemBackdropType backdropType) {
        IntByReference backdrop = new IntByReference(backdropType.getValue());
        DwmApi.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                Win32Constants.DwmAttribute.DWMWA_SYSTEMBACKDROP_TYPE,
                backdrop,
                Integer.BYTES
        );
    }


    /**
     * 设置窗口透明度
     * <p>
     * 该方法通过 Win32 API 设置指定窗口的透明度。
     *
     * @param hwnd  窗口句柄，不能为 null
     * @param value 透明度值，范围 0~1（浮点数）：
     *              <ul>
     *                <li>0.0 = 完全透明（不可见）</li>
     *                <li>0.5 = 半透明（50% 透明度）</li>
     *                <li>1.0 = 完全不透明（正常显示）</li>
     *              </ul>
     * @throws IllegalArgumentException 如果 value 不在 (0, 1) 范围内
     * @implNote 仅适用于 Windows 平台，依赖 User32.dll API
     */
    public static void setWindowAlpha(WinDef.HWND hwnd, float value) {
        if (hwnd == null) return;

        if (value <= 0 || value > 1) {
            throw new IllegalArgumentException("Alpha value must be in range (0, 1], received: " + value);
        }

        byte alpha = (byte) (value * 255);

        // 步骤1: 获取窗口的当前扩展样式（Extended Style）
        BaseTSD.LONG_PTR exStyle = User32Api.INSTANCE.GetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_EXSTYLE
        );

        // 步骤2: 将当前样式与 WS_EX_LAYERED 进行按位或操作
        // WS_EX_LAYERED 是启用分层窗口的必要标志
        // 分层窗口可以支持透明度、alpha 混合等高级视觉效果
        long style = exStyle.longValue();
        long newStyle = style | Win32Constants.WindowStyleEx.WS_EX_LAYERED;

        // 步骤3: 设置新的扩展样式到窗口
        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_EXSTYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        // 步骤4: 调用 SetLayeredWindowAttributes 设置透明度
        // 参数说明：
        // - hwnd: 目标窗口句柄
        // - 0: crKey（色键），此处不使用颜色透明，设为 0
        // - (byte) alpha: 透明度值，0=全透明，255=不透明
        // - LWA_ALPHA: 标志，表示使用 alpha 值来控制透明度
        User32Api.INSTANCE.SetLayeredWindowAttributes(
                hwnd,
                0,
                (byte) alpha,
                Win32Constants.LayeredWindowAttribute.LWA_ALPHA
        );
    }


    /**
     * 设置窗口圆角样式（Windows 11）
     *
     * <p>
     * 设置窗口的圆角偏好，包括默认、不圆角、圆角、小圆角等样式。
     * </p>
     *
     * @param hwnd             窗口句柄
     * @param cornerPreference 圆角偏好（DEFAULT、DO_NOT_ROUND、ROUND、ROUND_SMALL）
     * @implNote 需要 Windows 11 及以上版本
     */
    public static void setWindowCornerPreference(WinDef.HWND hwnd, DwmWindowCornerPreference cornerPreference) {
        IntByReference value = new IntByReference(cornerPreference.getValue());
        DwmApi.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                Win32Constants.DwmAttribute.DWMWA_WINDOW_CORNER_PREFERENCE,
                value,
                Integer.BYTES
        );
    }

    /**
     * 获取窗口圆角样式（Windows 11）
     *
     * <p>
     * 获取窗口当前的圆角偏好设置。
     * </p>
     *
     * @param hwnd 窗口句柄
     * @return 当前的圆角偏好，获取失败返回 null
     * @implNote 需要 Windows 11 及以上版本
     */
    public static DwmWindowCornerPreference getWindowCornerPreference(WinDef.HWND hwnd) {
        IntByReference value = new IntByReference();
        int result = DwmApi.INSTANCE.DwmGetWindowAttribute(
                hwnd,
                Win32Constants.DwmAttribute.DWMWA_WINDOW_CORNER_PREFERENCE,
                value,
                Integer.BYTES
        );

        if (result == 0) { // S_OK
            return DwmWindowCornerPreference.fromValue(value.getValue());
        }
        return null;
    }

    /**
     * 设置窗口暗色/亮色模式（Windows 10 1809+）
     *
     * <p>
     * 设置窗口标题栏是否使用暗色模式。
     * </p>
     *
     * @param hwnd        窗口句柄
     * @param useDarkMode true=暗色模式，false=亮色模式
     * @implNote 需要 Windows 10 1809 (Build 17763) 及以上版本
     */
    public static void setWindowDarkMode(WinDef.HWND hwnd, boolean useDarkMode) {
        IntByReference value = new IntByReference(useDarkMode ? 1 : 0);
        DwmApi.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                Win32Constants.DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE,
                value,
                Integer.BYTES
        );
    }


    /**
     * 获取窗口暗色/亮色模式状态（Windows 10 1809+）
     *
     * <p>
     * 获取窗口标题栏当前是否使用暗色模式。
     * </p>
     *
     * @param hwnd 窗口句柄
     * @return true=暗色模式，false=亮色模式，获取失败返回 null
     * @implNote 需要 Windows 10 1809 (Build 17763) 及以上版本
     */
    public static Boolean getWindowDarkMode(WinDef.HWND hwnd) {
        IntByReference value = new IntByReference();
        int result = DwmApi.INSTANCE.DwmGetWindowAttribute(
                hwnd,
                Win32Constants.DwmAttribute.DWMWA_USE_IMMERSIVE_DARK_MODE,
                value,
                Integer.BYTES
        );

        if (result == 0) { // S_OK
            return value.getValue() != 0;
        }
        return null;
    }

    /**
     * 设置窗口置顶 / 取消置顶
     *
     * @param hwnd        窗口句柄
     * @param alwaysOnTop true=置顶，false=取消置顶
     */
    public static void setAlwaysOnTop(WinDef.HWND hwnd, boolean alwaysOnTop) {
        if (hwnd == null) return;

        WinDef.HWND insertAfter = alwaysOnTop ? Win32Constants.HWndInsertAfter.HWND_TOPMOST :
                Win32Constants.HWndInsertAfter.HWND_NOTOPMOST;

        User32Api.INSTANCE.SetWindowPos(
                hwnd,
                insertAfter,
                0, 0, 0, 0,
                Win32Constants.SetWindowPosFlags.SWP_NOMOVE
                        | Win32Constants.SetWindowPosFlags.SWP_NOSIZE
                        | Win32Constants.SetWindowPosFlags.SWP_NOACTIVATE
        );
    }

    /**
     * 最小化窗口（系统级）
     *
     * @param hwnd 窗口句柄
     */
    public static void minimizeWindow(WinDef.HWND hwnd) {
        if (hwnd == null) return;
        User32Api.INSTANCE.ShowWindow(hwnd, Win32Constants.ShowWindowCmd.SW_MINIMIZE);
    }

    /**
     * 最大化窗口（系统级，当前屏幕）
     *
     * @param hwnd 窗口句柄
     */
    public static void maximizeWindow(WinDef.HWND hwnd) {
        if (hwnd == null) return;
        User32Api.INSTANCE.ShowWindow(hwnd, Win32Constants.ShowWindowCmd.SW_MAXIMIZE);
    }

    /**
     * 还原窗口（从最小化 / 最大化恢复）
     *
     * @param hwnd 窗口句柄
     */
    public static void restoreWindow(WinDef.HWND hwnd) {
        if (hwnd == null) return;
        User32Api.INSTANCE.ShowWindow(hwnd, Win32Constants.ShowWindowCmd.SW_RESTORE);
    }

    /**
     * 判断窗口是否最大化
     *
     * @param hwnd 窗口句柄
     * @return true=最大化，false=未最大化
     */
    public static boolean isWindowMaximized(WinDef.HWND hwnd) {
        if (hwnd == null) return false;
        return User32Api.INSTANCE.IsZoomed(hwnd);
    }

    /**
     * 判断窗口是否最小化
     *
     * @param hwnd 窗口句柄
     * @return true=最小化，false=未最小化
     */
    public static boolean isWindowMinimized(WinDef.HWND hwnd) {
        if (hwnd == null) return false;
        return User32Api.INSTANCE.IsIconic(hwnd);
    }

    /**
     * 禁用窗口最大化按钮
     *
     * @param hwnd    窗口句柄
     * @param disable true=禁用，false=恢复
     */
    public static void disableMaximize(WinDef.HWND hwnd, boolean disable) {
        if (hwnd == null) return;

        BaseTSD.LONG_PTR style = User32Api.INSTANCE.GetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE
        );

        long currentStyle = style.longValue();
        long newStyle;

        if (disable) {
            newStyle = currentStyle & ~Win32Constants.WindowStyle.WS_MAXIMIZEBOX;
        } else {
            newStyle = currentStyle | Win32Constants.WindowStyle.WS_MAXIMIZEBOX;
        }

        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        refreshWindowFrame(hwnd);
    }

    /**
     * 禁止窗口调整大小（移除可拉伸边框）
     *
     * @param hwnd    窗口句柄
     * @param disable true=禁用，false=恢复
     */
    public static void disableResize(WinDef.HWND hwnd, boolean disable) {
        if (hwnd == null) return;

        BaseTSD.LONG_PTR style = User32Api.INSTANCE.GetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE
        );

        long currentStyle = style.longValue();
        long newStyle;

        if (disable) {
            newStyle = currentStyle & ~Win32Constants.WindowStyle.WS_THICKFRAME;
            newStyle = newStyle & ~Win32Constants.WindowStyle.WS_MAXIMIZEBOX;
        } else {
            newStyle = currentStyle | Win32Constants.WindowStyle.WS_THICKFRAME;
            newStyle = newStyle | Win32Constants.WindowStyle.WS_MAXIMIZEBOX;
        }

        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        refreshWindowFrame(hwnd);
    }


    /**
     * 启用 Aero Snap / Win11 贴边吸附支持
     *
     * <p>
     * 确保窗口具有可调整大小的边框样式，这是 Aero Snap 功能的前提条件。
     * </p>
     *
     * <p><b>重要说明：</b></p>
     * <ul>
     *   <li>此方法会将 JavaFX 的无边框窗口转换为标准窗口样式</li>
     *   <li>转换后窗口将显示标准的 Windows 标题栏和边框</li>
     *   <li>Aero Snap 需要标准窗口样式（非 WS_POPUP）才能工作</li>
     *   <li>如果你想保持无边框但支持 Aero Snap，请使用 {@link #enableAeroSnapForBorderless}</li>
     * </ul>
     *
     * @param hwnd 窗口句柄
     * @see #enableAeroSnapForBorderless(WinDef.HWND)
     */
    public static void enableAeroSnap(WinDef.HWND hwnd) {
        if (hwnd == null) return;

        BaseTSD.LONG_PTR style = User32Api.INSTANCE.GetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE
        );

        long currentStyle = style.longValue();

        // 移除 WS_POPUP 样式（JavaFX 默认样式）
        long newStyle = currentStyle & ~Win32Constants.WindowStyle.WS_POPUP;

        // 添加标准窗口样式，这是 Aero Snap 所必需的
        newStyle = newStyle | Win32Constants.WindowStyle.WS_OVERLAPPEDWINDOW;

        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        refreshWindowFrame(hwnd);
    }

    /**
     * 为无边框窗口启用 Aero Snap（保持无边框外观）
     *
     * <p>
     * 这个方法通过添加 WS_THICKFRAME 样式来启用 Aero Snap，
     * 同时保持窗口的无边框外观（通过移除 WS_CAPTION）。
     * </p>
     *
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>自定义标题栏的无边框窗口</li>
     *   <li>需要支持 Windows 的贴边吸附功能</li>
     *   <li>不希望显示系统标题栏</li>
     * </ul>
     *
     * <p><b>注意事项：</b></p>
     * <ul>
     *   <li>窗口边缘会显示1像素的细边框（可调整大小的指示）</li>
     *   <li>需要自行实现标题栏拖动功能（使用 {@link #enableWindowDrag}）</li>
     *   <li>可能需要配合 {@link #extendFrameIntoClientArea} 实现完全沉浸式</li>
     * </ul>
     *
     * @param hwnd 窗口句柄
     * @see #enableWindowDrag(WinDef.HWND, Node)
     * @see #extendFrameIntoClientArea(WinDef.HWND)
     */
    public static void enableAeroSnapForBorderless(WinDef.HWND hwnd) {
        if (hwnd == null) return;

        BaseTSD.LONG_PTR style = User32Api.INSTANCE.GetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE
        );

        long currentStyle = style.longValue();

        // 添加 WS_THICKFRAME（可调整大小的边框，Aero Snap 必需）
        long newStyle = currentStyle | Win32Constants.WindowStyle.WS_THICKFRAME;

        // 移除 WS_CAPTION（标题栏），保持无边框外观
        // 注意：WS_CAPTION 包含了 WS_BORDER 和 WS_DLGFRAME
        newStyle = newStyle & ~Win32Constants.WindowStyle.WS_CAPTION;

        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                Win32Constants.WindowLongIndex.GWL_STYLE,
                new BaseTSD.LONG_PTR(newStyle).toPointer()
        );

        refreshWindowFrame(hwnd);
    }


    /**
     * 扩展内容到标题栏（真正的沉浸式）
     *
     * <p>
     * 将窗口框架扩展到客户区，实现内容延伸到标题栏的效果。
     * 常用于创建自定义标题栏的沉浸式窗口。
     * </p>
     *
     * <p><b>重要说明：</b></p>
     * <ul>
     *   <li>此方法必须配合窗口样式使用才能看到效果</li>
     *   <li>窗口必须有 WS_CAPTION 样式（可以使用 {@link #enableAeroSnap} 或 {@link #enableAeroSnapForBorderless}）</li>
     *   <li>JavaFX Scene 建议设置透明填充（否则可能看不到 DWM 框架）</li>
     *   <li>此方法在 Windows Vista 及以上版本有效</li>
     * </ul>
     *
     * <p><b>使用示例：</b></p>
     * <pre>{@code
     * // 1. 启用 Aero Snap（添加必要的窗口样式）
     * WinDef.HWND hwnd = FXNativeWindowsTools.getHWndByEnumeration();
     * FXNativeWindowsTools.enableAeroSnapForBorderless(hwnd);
     *
     * // 2. 扩展框架到客户区（所有边距为 -1 表示完全沉浸式）
     * FXNativeWindowsTools.extendFrameIntoClientArea(hwnd);
     *
     * // 3. （可选）设置 Scene 透明背景
     * Scene scene = new Scene(root);
     * scene.setFill(null);  // 透明背景
     * }</pre>
     *
     * @param hwnd   窗口句柄
     * @param left   左边距（像素），-1 表示扩展整个边框
     * @param right  右边距（像素）
     * @param top    顶部边距（像素）
     * @param bottom 底部边距（像素）
     * @implNote <ul>
     * <li>设置所有边距为 -1 可实现完全沉浸式效果</li>
     * <li>仅设置 top 为负值可保留边框但扩展标题栏</li>
     * <li>需要 Windows Vista 及以上版本</li>
     * </ul>
     * @see #enableAeroSnapForBorderless(WinDef.HWND)
     */
    public static void extendFrameIntoClientArea(WinDef.HWND hwnd, int left, int right, int top, int bottom) {
        if (hwnd == null) return;

        DwmApi.MARGINS margins = new DwmApi.MARGINS();
        margins.cxLeftWidth = left;
        margins.cxRightWidth = right;
        margins.cyTopHeight = top;
        margins.cyBottomHeight = bottom;

        DwmApi.INSTANCE.DwmExtendFrameIntoClientArea(hwnd, margins);
    }

    /**
     * 扩展内容到标题栏（完全沉浸式，所有边距为 -1）
     *
     * <p>
     * 便捷方法，将所有边距设置为 -1，实现完全沉浸式效果。
     * </p>
     *
     * @param hwnd 窗口句柄
     */
    public static void extendFrameIntoClientArea(WinDef.HWND hwnd) {
        extendFrameIntoClientArea(hwnd, -1, -1, -1, -1);
    }

    /**
     * 刷新窗口非客户区（样式变更后必须调用）
     */
    private static void refreshWindowFrame(WinDef.HWND hwnd) {
        User32Api.INSTANCE.SetWindowPos(
                hwnd,
                null,
                0, 0, 0, 0,
                Win32Constants.SetWindowPosFlags.SWP_NOMOVE
                        | Win32Constants.SetWindowPosFlags.SWP_NOSIZE
                        | Win32Constants.SetWindowPosFlags.SWP_NOZORDER
                        | Win32Constants.SetWindowPosFlags.SWP_FRAMECHANGED
        );
    }

    //  Windows API 接口定义

    /**
     * 调试工具：打印当前进程的所有窗口信息（包括窗口类名、标题、句柄）
     *
     * @implNote 仅适用于 Windows 平台
     */
    public static void printAllWindowsInfo() {
        int currentPid = Kernel32Api.INSTANCE.GetCurrentProcessId();
        System.out.println("\n========== 当前进程的所有窗口 ==========");
        System.out.println("进程 ID: " + currentPid);
        System.out.println();

        final int[] windowCount = {0};

        User32.INSTANCE.EnumWindows((hWnd, lParam) -> {
            IntByReference windowPid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hWnd, windowPid);

            if (windowPid.getValue() == currentPid) {
                windowCount[0]++;

                // 获取窗口类名
                char[] className = new char[256];
                User32.INSTANCE.GetClassName(hWnd, className, className.length);
                String classNameStr = new String(className).trim();

                // 获取窗口标题
                char[] windowText = new char[256];
                User32.INSTANCE.GetWindowText(hWnd, windowText, windowText.length);
                String windowTitle = new String(windowText).trim();

                // 获取窗口句柄
                long hwndValue = Pointer.nativeValue(hWnd.getPointer());

                System.out.println("窗口 " + windowCount[0] + ":");
                System.out.println("  句柄: 0x" + Long.toHexString(hwndValue).toUpperCase());
                System.out.println("  类名: " + classNameStr);
                System.out.println("  标题: " + (windowTitle.isEmpty() ? "(无标题)" : windowTitle));
                System.out.println();
            }
            return true;
        }, null);

        if (windowCount[0] == 0) {
            System.out.println("未找到任何窗口");
        }
        System.out.println("\n");
    }

    /**
     * DWM窗口属性ID枚举 (窗口样式 暗色 与 亮色)
     * 对应原接口中的 DWMWA_* 常量
     */
    public enum DwmWindowAttribute {
        // 沉浸式深色模式属性(暗色)
        USE_IMMERSIVE_DARK_MODE(20),
        // 窗口圆角偏好属性(亮色)
        WINDOW_CORNER_PREFERENCE(33);

        // 存储属性对应的整型值
        private final int value;

        // 私有构造方法初始化值
        DwmWindowAttribute(int value) {
            this.value = value;
        }

        // 根据数值反向查找枚举（可选，便于使用）
        public static DwmWindowAttribute fromValue(int value) {
            for (DwmWindowAttribute attr : DwmWindowAttribute.values()) {
                if (attr.value == value) {
                    return attr;
                }
            }
            throw new IllegalArgumentException("无效的DWM窗口属性值: " + value);
        }

        // 获取属性对应的整型值
        public int getValue() {
            return value;
        }
    }

    /**
     * DWM窗口圆角偏好枚举
     * 对应原接口中的 DWMWCP_* 常量
     */
    public enum DwmWindowCornerPreference {
        // 默认圆角设置
        DEFAULT(0),
        // 不圆角
        DO_NOT_ROUND(1),
        // 圆角
        ROUND(2),
        // 小圆角
        ROUND_SMALL(3);

        // 存储偏好值对应的整型值
        private final int value;

        // 私有构造方法初始化值
        DwmWindowCornerPreference(int value) {
            this.value = value;
        }

        // 根据数值反向查找枚举（可选）
        public static DwmWindowCornerPreference fromValue(int value) {
            for (DwmWindowCornerPreference pref : DwmWindowCornerPreference.values()) {
                if (pref.value == value) {
                    return pref;
                }
            }
            throw new IllegalArgumentException("无效的DWM窗口圆角偏好值: " + value);
        }

        // 获取偏好值对应的整型值
        public int getValue() {
            return value;
        }
    }

    //  Windows 常量枚举定义

    /**
     * 系统背景类型（Windows 11）
     *
     * <p>
     * 用于设置窗口的系统背景效果，如 Mica（云母）、Acrylic（亚克力）等。
     * </p>
     */
    public enum SystemBackdropType {
        /**
         * 无背景效果
         */
        DWMSBT_NONE(1),

        /**
         * Mica 效果（云母）
         */
        DWMSBT_MICA(2),

        /**
         * Acrylic 效果（亚克力），需要 Windows 11
         */
        DWMSBT_ACRYLIC(3);

        private final int value;

        SystemBackdropType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * DWM (Desktop Window Manager) API 接口
     *
     * <p>
     * 用于调用 dwmapi.dll 中的 DWM 相关函数，
     * 支持窗口特效、Aero 效果、Acrylic/Mica 背景等功能。
     * </p>
     */
    public interface DwmApi extends Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);

        /**
         * 设置窗口的 DWM 属性
         *
         * @param hwnd        窗口句柄
         * @param dwAttribute DWM 属性 ID
         * @param pvAttribute 属性值指针
         * @param cbAttribute 属性值大小（字节）
         * @return 成功返回 0，失败返回错误码
         */
        int DwmSetWindowAttribute(
                WinDef.HWND hwnd,
                int dwAttribute,
                IntByReference pvAttribute,
                int cbAttribute
        );

        /**
         * 获取窗口的 DWM 属性
         *
         * @param hwnd        窗口句柄
         * @param dwAttribute DWM 属性 ID
         * @param pvAttribute 用于接收属性值的指针
         * @param cbAttribute 属性值大小（字节）
         * @return 成功返回 0（S_OK），失败返回错误码
         */
        int DwmGetWindowAttribute(
                WinDef.HWND hwnd,
                int dwAttribute,
                IntByReference pvAttribute,
                int cbAttribute
        );

        /**
         * 扩展窗口框架到客户区
         *
         * @param hwnd      窗口句柄
         * @param pMarInset 边距结构指针
         * @return 成功返回 0，失败返回错误码
         */
        int DwmExtendFrameIntoClientArea(
                WinDef.HWND hwnd,
                MARGINS pMarInset
        );

        /**
         * MARGINS 结构体，用于定义扩展边距
         */
        class MARGINS extends com.sun.jna.Structure {
            public int cxLeftWidth;
            public int cxRightWidth;
            public int cyTopHeight;
            public int cyBottomHeight;

            @Override
            protected List<String> getFieldOrder() {
                return java.util.Arrays.asList("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight");
            }
        }
    }


    /**
     * User32 API 接口扩展
     *
     * <p>
     * 扩展 JNA 的 User32 接口，添加窗口透明度、消息发送等功能。
     * </p>
     *
     * @see <a href="https://learn.microsoft.com/en-us/windows/win32/api/winuser/">User32 API - Microsoft Docs</a>
     */
    public interface User32Api extends User32 {
        User32Api INSTANCE = Native.load("user32", User32Api.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * 设置分层窗口的不透明度和透明色
         *
         * @param hwnd    窗口句柄
         * @param crKey   透明色键（RGB 值），当使用 LWA_COLORKEY 标志时有效
         * @param bAlpha  透明度，0=全透明，255=不透明
         * @param dwFlags 标志（LWA_ALPHA 或 LWA_COLORKEY）
         * @return 成功返回 true，失败返回 false
         */
        boolean SetLayeredWindowAttributes(
                HWND hwnd,
                int crKey,
                byte bAlpha,
                int dwFlags
        );

        /**
         * 释放鼠标捕获
         *
         * @return 成功返回 true，失败返回 false
         */
        boolean ReleaseCapture();

        /**
         * 发送消息到窗口
         *
         * @param hwnd   目标窗口句柄
         * @param msg    消息 ID
         * @param wParam 消息参数 1
         * @param lParam 消息参数 2
         * @return 消息处理结果
         */
        LRESULT SendMessage(
                HWND hwnd,
                int msg,
                WPARAM wParam,
                LPARAM lParam
        );

        /**
         * 将客户区坐标转换为屏幕坐标
         *
         * @param hwnd    窗口句柄
         * @param lpPoint 指向包含客户区坐标的 POINT 结构的指针，返回时包含屏幕坐标
         * @return 成功返回 true，失败返回 false
         */
        boolean ClientToScreen(
                HWND hwnd,
                POINT lpPoint
        );

        /**
         * 将屏幕坐标转换为客户区坐标
         *
         * @param hwnd    窗口句柄
         * @param lpPoint 指向包含屏幕坐标的 POINT 结构的指针，返回时包含客户区坐标
         * @return 成功返回 true，失败返回 false
         */
        boolean ScreenToClient(
                HWND hwnd,
                POINT lpPoint
        );

        /**
         * 设置窗口位置和 Z 序
         *
         * @param hwnd            窗口句柄
         * @param hWndInsertAfter 在 Z 序中位于被置位窗口前的窗口句柄
         * @param X               窗口新位置的左边界
         * @param Y               窗口新位置的上边界
         * @param cx              窗口的新宽度
         * @param cy              窗口的新高度
         * @param uFlags          窗口尺寸和定位标志
         * @return 成功返回 true，失败返回 false
         */
        boolean SetWindowPos(
                HWND hwnd,
                HWND hWndInsertAfter,
                int X,
                int Y,
                int cx,
                int cy,
                int uFlags
        );

        /**
         * 显示或隐藏窗口
         *
         * @param hwnd     窗口句柄
         * @param nCmdShow 控制窗口如何显示
         * @return 如果窗口之前可见返回 true，否则返回 false
         */
        boolean ShowWindow(HWND hwnd, int nCmdShow);

        /**
         * 判断窗口是否最大化
         *
         * @param hwnd 窗口句柄
         * @return 最大化返回 true，否则返回 false
         */
        boolean IsZoomed(HWND hwnd);

        /**
         * 判断窗口是否最小化
         *
         * @param hwnd 窗口句柄
         * @return 最小化返回 true，否则返回 false
         */
        boolean IsIconic(HWND hwnd);
    }

    // Win32 常量定义（统一管理）

    /**
     * Kernel32 API 接口扩展
     *
     * <p>
     * 扩展 JNA 的 Kernel32 接口，添加进程 ID 获取等功能。
     * </p>
     */
    public interface Kernel32Api extends Kernel32 {
        Kernel32Api INSTANCE = Native.load("kernel32", Kernel32Api.class, W32APIOptions.DEFAULT_OPTIONS);

        /**
         * 获取当前进程的进程 ID
         *
         * @return 当前进程 ID
         */
        int GetCurrentProcessId();
    }

    /**
     * Win32 API 常量统一管理类
     *
     * <p>
     * 将所有 Windows API 相关的常量集中管理，按功能分组：
     * <ul>
     *   <li>窗口样式（WindowStyle）- 位掩码类型，可组合</li>
     *   <li>窗口消息（WindowMessage）- 普通值类型</li>
     *   <li>ShowWindow 命令（ShowWindowCmd）- 普通值类型</li>
     *   <li>SetWindowPos 标志（SetWindowPosFlags）- 位掩码类型，可组合</li>
     *   <li>DWM 属性（DwmAttribute）- 普通值类型</li>
     * </ul>
     * </p>
     */
    public static class Win32Constants {

        /**
         * 窗口样式常量（位掩码，可用 | 组合）
         *
         * <p>
         * 用于 GetWindowLongPtr/SetWindowLongPtr 的 GWL_STYLE 参数。
         * 这些常量可以通过位或运算（|）组合使用。
         * </p>
         */
        public static class WindowStyle {
            // 基础窗口样式
            public static final long WS_OVERLAPPED = 0x00000000L;
            public static final long WS_POPUP = 0x80000000L;
            public static final long WS_CHILD = 0x40000000L;
            public static final long WS_MINIMIZE = 0x20000000L;
            public static final long WS_VISIBLE = 0x10000000L;
            public static final long WS_DISABLED = 0x08000000L;
            public static final long WS_CLIPSIBLINGS = 0x04000000L;
            public static final long WS_CLIPCHILDREN = 0x02000000L;
            public static final long WS_MAXIMIZE = 0x01000000L;

            // 窗口边框和标题栏样式
            public static final long WS_CAPTION = 0x00C00000L;
            public static final long WS_BORDER = 0x00800000L;
            public static final long WS_DLGFRAME = 0x00400000L;
            public static final long WS_VSCROLL = 0x00200000L;
            public static final long WS_HSCROLL = 0x00100000L;
            public static final long WS_SYSMENU = 0x00080000L;
            public static final long WS_THICKFRAME = 0x00040000L;
            public static final long WS_GROUP = 0x00020000L;
            public static final long WS_TABSTOP = 0x00010000L;

            // 窗口按钮样式
            public static final long WS_MINIMIZEBOX = 0x00020000L;
            public static final long WS_MAXIMIZEBOX = 0x00010000L;

            // 组合样式
            public static final long WS_OVERLAPPEDWINDOW = WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU
                    | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
            public static final long WS_TILEDWINDOW = WS_OVERLAPPEDWINDOW;
            public static final long WS_POPUPWINDOW = WS_POPUP | WS_BORDER | WS_SYSMENU;
            // 别名
            public static final long WS_SIZEBOX = WS_THICKFRAME;
            public static final long WS_TILED = WS_OVERLAPPED;
        }

        /**
         * 扩展窗口样式常量（位掩码，可用 | 组合）
         *
         * <p>
         * 用于 GetWindowLongPtr/SetWindowLongPtr 的 GWL_EXSTYLE 参数。
         * </p>
         */
        public static class WindowStyleEx {
            public static final int WS_EX_DLGMODALFRAME = 0x00000001;
            public static final int WS_EX_NOPARENTNOTIFY = 0x00000004;
            public static final int WS_EX_TOPMOST = 0x00000008;
            public static final int WS_EX_ACCEPTFILES = 0x00000010;
            public static final int WS_EX_TRANSPARENT = 0x00000020;
            public static final int WS_EX_MDICHILD = 0x00000040;
            public static final int WS_EX_TOOLWINDOW = 0x00000080;
            public static final int WS_EX_WINDOWEDGE = 0x00000100;
            public static final int WS_EX_CLIENTEDGE = 0x00000200;
            public static final int WS_EX_CONTEXTHELP = 0x00000400;
            public static final int WS_EX_RIGHT = 0x00001000;
            public static final int WS_EX_LEFT = 0x00000000;
            public static final int WS_EX_RTLREADING = 0x00002000;
            public static final int WS_EX_LTRREADING = 0x00000000;
            public static final int WS_EX_LEFTSCROLLBAR = 0x00004000;
            public static final int WS_EX_RIGHTSCROLLBAR = 0x00000000;
            public static final int WS_EX_CONTROLPARENT = 0x00010000;
            public static final int WS_EX_STATICEDGE = 0x00020000;
            public static final int WS_EX_APPWINDOW = 0x00040000;
            public static final int WS_EX_LAYERED = 0x00080000;
            public static final int WS_EX_NOINHERITLAYOUT = 0x00100000;
            public static final int WS_EX_NOREDIRECTIONBITMAP = 0x00200000;
            public static final int WS_EX_LAYOUTRTL = 0x00400000;
            public static final int WS_EX_COMPOSITED = 0x02000000;
            public static final int WS_EX_NOACTIVATE = 0x08000000;

            // 组合样式
            public static final int WS_EX_OVERLAPPEDWINDOW = WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE;
            public static final int WS_EX_PALETTEWINDOW = WS_EX_WINDOWEDGE | WS_EX_TOOLWINDOW | WS_EX_TOPMOST;
        }

        /**
         * GetWindowLongPtr/SetWindowLongPtr 索引常量
         *
         * <p>
         * 用于指定要获取或设置的窗口属性类型。
         * </p>
         */
        public static class WindowLongIndex {
            public static final int GWL_WNDPROC = -4;
            public static final int GWL_HINSTANCE = -6;
            public static final int GWL_HWNDPARENT = -8;
            public static final int GWL_STYLE = -16;
            public static final int GWL_EXSTYLE = -20;
            public static final int GWL_USERDATA = -21;
            public static final int GWL_ID = -12;
        }

        /**
         * SetWindowPos 标志常量（位掩码，可用 | 组合）
         *
         * <p>
         * 用于 SetWindowPos 函数的 uFlags 参数。
         * 这些标志可以通过位或运算（|）组合使用。
         * </p>
         */
        public static class SetWindowPosFlags {
            public static final int SWP_NOSIZE = 0x0001;
            public static final int SWP_NOMOVE = 0x0002;
            public static final int SWP_NOZORDER = 0x0004;
            public static final int SWP_NOREDRAW = 0x0008;
            public static final int SWP_NOACTIVATE = 0x0010;
            public static final int SWP_FRAMECHANGED = 0x0020;
            public static final int SWP_SHOWWINDOW = 0x0040;
            public static final int SWP_HIDEWINDOW = 0x0080;
            public static final int SWP_NOCOPYBITS = 0x0100;
            public static final int SWP_NOOWNERZORDER = 0x0200;
            public static final int SWP_NOSENDCHANGING = 0x0400;
            public static final int SWP_DRAWFRAME = SWP_FRAMECHANGED;
            public static final int SWP_NOREPOSITION = SWP_NOOWNERZORDER;
            public static final int SWP_DEFERERASE = 0x2000;
            public static final int SWP_ASYNCWINDOWPOS = 0x4000;
        }

        /**
         * SetWindowPos 特殊窗口句柄常量
         */
        public static class HWndInsertAfter {
            public static final WinDef.HWND HWND_TOP = new WinDef.HWND(new Pointer(0));
            public static final WinDef.HWND HWND_BOTTOM = new WinDef.HWND(new Pointer(1));
            public static final WinDef.HWND HWND_TOPMOST = new WinDef.HWND(new Pointer(-1));
            public static final WinDef.HWND HWND_NOTOPMOST = new WinDef.HWND(new Pointer(-2));
        }

        /**
         * ShowWindow 命令常量（普通值，不可组合）
         *
         * <p>
         * 用于 ShowWindow 函数的 nCmdShow 参数。
         * 这些是互斥的命令值，不能组合使用。
         * </p>
         */
        public static class ShowWindowCmd {
            public static final int SW_HIDE = 0;
            public static final int SW_SHOWNORMAL = 1;
            public static final int SW_NORMAL = 1;
            public static final int SW_SHOWMINIMIZED = 2;
            public static final int SW_SHOWMAXIMIZED = 3;
            public static final int SW_MAXIMIZE = 3;
            public static final int SW_SHOWNOACTIVATE = 4;
            public static final int SW_SHOW = 5;
            public static final int SW_MINIMIZE = 6;
            public static final int SW_SHOWMINNOACTIVE = 7;
            public static final int SW_SHOWNA = 8;
            public static final int SW_RESTORE = 9;
            public static final int SW_SHOWDEFAULT = 10;
            public static final int SW_FORCEMINIMIZE = 11;
        }

        /**
         * 窗口消息常量（普通值，不可组合）
         */
        public static class WindowMessage {
            public static final int WM_NULL = 0x0000;
            public static final int WM_CREATE = 0x0001;
            public static final int WM_DESTROY = 0x0002;
            public static final int WM_MOVE = 0x0003;
            public static final int WM_SIZE = 0x0005;
            public static final int WM_ACTIVATE = 0x0006;
            public static final int WM_SETFOCUS = 0x0007;
            public static final int WM_KILLFOCUS = 0x0008;
            public static final int WM_ENABLE = 0x000A;
            public static final int WM_SETREDRAW = 0x000B;
            public static final int WM_SETTEXT = 0x000C;
            public static final int WM_GETTEXT = 0x000D;
            public static final int WM_GETTEXTLENGTH = 0x000E;
            public static final int WM_PAINT = 0x000F;
            public static final int WM_CLOSE = 0x0010;
            public static final int WM_QUIT = 0x0012;
            public static final int WM_ERASEBKGND = 0x0014;
            public static final int WM_SYSCOLORCHANGE = 0x0015;
            public static final int WM_SHOWWINDOW = 0x0018;
            public static final int WM_ACTIVATEAPP = 0x001C;
            public static final int WM_SETCURSOR = 0x0020;
            public static final int WM_MOUSEACTIVATE = 0x0021;
            public static final int WM_GETMINMAXINFO = 0x0024;
            public static final int WM_WINDOWPOSCHANGING = 0x0046;
            public static final int WM_WINDOWPOSCHANGED = 0x0047;
            public static final int WM_NCCREATE = 0x0081;
            public static final int WM_NCDESTROY = 0x0082;
            public static final int WM_NCCALCSIZE = 0x0083;
            public static final int WM_NCHITTEST = 0x0084;
            public static final int WM_NCPAINT = 0x0085;
            public static final int WM_NCACTIVATE = 0x0086;
            public static final int WM_NCMOUSEMOVE = 0x00A0;
            public static final int WM_NCLBUTTONDOWN = 0x00A1;
            public static final int WM_NCLBUTTONUP = 0x00A2;
            public static final int WM_NCLBUTTONDBLCLK = 0x00A3;
            public static final int WM_NCRBUTTONDOWN = 0x00A4;
            public static final int WM_NCRBUTTONUP = 0x00A5;
            public static final int WM_NCRBUTTONDBLCLK = 0x00A6;
            public static final int WM_NCMBUTTONDOWN = 0x00A7;
            public static final int WM_NCMBUTTONUP = 0x00A8;
            public static final int WM_NCMBUTTONDBLCLK = 0x00A9;
            public static final int WM_KEYDOWN = 0x0100;
            public static final int WM_KEYUP = 0x0101;
            public static final int WM_CHAR = 0x0102;
            public static final int WM_SYSKEYDOWN = 0x0104;
            public static final int WM_SYSKEYUP = 0x0105;
            public static final int WM_SYSCHAR = 0x0106;
            public static final int WM_MOUSEMOVE = 0x0200;
            public static final int WM_LBUTTONDOWN = 0x0201;
            public static final int WM_LBUTTONUP = 0x0202;
            public static final int WM_LBUTTONDBLCLK = 0x0203;
            public static final int WM_RBUTTONDOWN = 0x0204;
            public static final int WM_RBUTTONUP = 0x0205;
            public static final int WM_RBUTTONDBLCLK = 0x0206;
            public static final int WM_MBUTTONDOWN = 0x0207;
            public static final int WM_MBUTTONUP = 0x0208;
            public static final int WM_MBUTTONDBLCLK = 0x0209;
            public static final int WM_MOUSEWHEEL = 0x020A;
            public static final int WM_MOUSEHWHEEL = 0x020E;
            public static final int WM_DWMCOMPOSITIONCHANGED = 0x031E;
        }

        /**
         * 窗口点击测试代码（Hit Test）
         */
        public static class HitTestCode {
            public static final int HTERROR = -2;
            public static final int HTTRANSPARENT = -1;
            public static final int HTNOWHERE = 0;
            public static final int HTCLIENT = 1;
            public static final int HTCAPTION = 2;
            public static final int HTSYSMENU = 3;
            public static final int HTGROWBOX = 4;
            public static final int HTSIZE = HTGROWBOX;
            public static final int HTMENU = 5;
            public static final int HTHSCROLL = 6;
            public static final int HTVSCROLL = 7;
            public static final int HTMINBUTTON = 8;
            public static final int HTMAXBUTTON = 9;
            public static final int HTLEFT = 10;
            public static final int HTRIGHT = 11;
            public static final int HTTOP = 12;
            public static final int HTTOPLEFT = 13;
            public static final int HTTOPRIGHT = 14;
            public static final int HTBOTTOM = 15;
            public static final int HTBOTTOMLEFT = 16;
            public static final int HTBOTTOMRIGHT = 17;
            public static final int HTBORDER = 18;
            public static final int HTREDUCE = HTMINBUTTON;
            public static final int HTZOOM = HTMAXBUTTON;
            public static final int HTSIZEFIRST = HTLEFT;
            public static final int HTSIZELAST = HTBOTTOMRIGHT;
            public static final int HTOBJECT = 19;
            public static final int HTCLOSE = 20;
            public static final int HTHELP = 21;
        }

        /**
         * 分层窗口属性标志（位掩码，可用 | 组合）
         *
         * <p>
         * 用于 SetLayeredWindowAttributes 函数的 dwFlags 参数。
         * </p>
         */
        public static class LayeredWindowAttribute {
            public static final int LWA_COLORKEY = 0x00000001;
            public static final int LWA_ALPHA = 0x00000002;
        }

        /**
         * DWM 窗口属性常量（普通值，不可组合）
         *
         * <p>
         * 用于 DwmSetWindowAttribute/DwmGetWindowAttribute 的 dwAttribute 参数。
         * </p>
         */
        public static class DwmAttribute {
            public static final int DWMWA_NCRENDERING_ENABLED = 1;
            public static final int DWMWA_NCRENDERING_POLICY = 2;
            public static final int DWMWA_TRANSITIONS_FORCEDISABLED = 3;
            public static final int DWMWA_ALLOW_NCPAINT = 4;
            public static final int DWMWA_CAPTION_BUTTON_BOUNDS = 5;
            public static final int DWMWA_NONCLIENT_RTL_LAYOUT = 6;
            public static final int DWMWA_FORCE_ICONIC_REPRESENTATION = 7;
            public static final int DWMWA_FLIP3D_POLICY = 8;
            public static final int DWMWA_EXTENDED_FRAME_BOUNDS = 9;
            public static final int DWMWA_HAS_ICONIC_BITMAP = 10;
            public static final int DWMWA_DISALLOW_PEEK = 11;
            public static final int DWMWA_EXCLUDED_FROM_PEEK = 12;
            public static final int DWMWA_CLOAK = 13;
            public static final int DWMWA_CLOAKED = 14;
            public static final int DWMWA_FREEZE_REPRESENTATION = 15;
            public static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
            public static final int DWMWA_WINDOW_CORNER_PREFERENCE = 33;
            public static final int DWMWA_BORDER_COLOR = 34;
            public static final int DWMWA_CAPTION_COLOR = 35;
            public static final int DWMWA_TEXT_COLOR = 36;
            public static final int DWMWA_VISIBLE_FRAME_BORDER_THICKNESS = 37;
            public static final int DWMWA_SYSTEMBACKDROP_TYPE = 38;
        }

        /**
         * DWM 窗口圆角偏好常量（普通值，不可组合）
         */
        public static class DwmWindowCornerPreference {
            public static final int DWMWCP_DEFAULT = 0;
            public static final int DWMWCP_DONOTROUND = 1;
            public static final int DWMWCP_ROUND = 2;
            public static final int DWMWCP_ROUNDSMALL = 3;
        }

        /**
         * 系统背景类型常量（普通值，不可组合）
         *
         * <p>
         * 用于 Windows 11 的 Mica 和 Acrylic 背景效果。
         * </p>
         */
        public static class SystemBackdropType {
            public static final int DWMSBT_AUTO = 0;
            public static final int DWMSBT_NONE = 1;
            public static final int DWMSBT_MAINWINDOW = 2;
            public static final int DWMSBT_TRANSIENTWINDOW = 3;
            public static final int DWMSBT_TABBEDWINDOW = 4;
        }
    }


}
