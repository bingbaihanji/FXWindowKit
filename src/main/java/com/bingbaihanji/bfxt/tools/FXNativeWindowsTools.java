package com.bingbaihanji.bfxt.tools;


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


    public static WinDef.HWND getHWnd() {
        try {
            com.sun.glass.ui.Window window =
                    com.sun.glass.ui.Window.getWindows().stream()
                            .filter(w -> w.getNativeWindow() != 0)
                            .findFirst()
                            .orElse(null);

            if (window == null) {
                return null;
            }

            long hwnd = window.getNativeWindow();
            return new WinDef.HWND(new Pointer(hwnd));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


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
                        WindowMessage.WM_NCLBUTTONDOWN.getValue(),
                        new WinDef.WPARAM(HitTestCode.HTCAPTION.getValue()),
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
                DwmAttribute.DWMWA_SYSTEMBACKDROP_TYPE.getValue(),
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
                WindowStyleIndex.GWL_EXSTYLE.getValue()
        );

        // 步骤2: 将当前样式与 WS_EX_LAYERED 进行按位或操作
        // WS_EX_LAYERED 是启用分层窗口的必要标志
        // 分层窗口可以支持透明度、alpha 混合等高级视觉效果
        long style = exStyle.longValue();
        long newStyle = style | WindowStyleEx.WS_EX_LAYERED.getValue();

        // 步骤3: 设置新的扩展样式到窗口
        User32Api.INSTANCE.SetWindowLongPtr(
                hwnd,
                WindowStyleIndex.GWL_EXSTYLE.getValue(),
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
                LayeredWindowAttribute.LWA_ALPHA.getValue()
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
                DwmWindowAttribute.WINDOW_CORNER_PREFERENCE.getValue(),
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
                DwmWindowAttribute.WINDOW_CORNER_PREFERENCE.getValue(),
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
                DwmWindowAttribute.USE_IMMERSIVE_DARK_MODE.getValue(),
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
                DwmWindowAttribute.USE_IMMERSIVE_DARK_MODE.getValue(),
                value,
                Integer.BYTES
        );

        if (result == 0) { // S_OK
            return value.getValue() != 0;
        }
        return null;
    }



    //  Windows API 接口定义

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
                WinDef.POINT lpPoint
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
                WinDef.POINT lpPoint
        );
    }

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

    //  Windows 常量枚举定义 


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

        // 获取属性对应的整型值
        public int getValue() {
            return value;
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

        // 获取偏好值对应的整型值
        public int getValue() {
            return value;
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
    }


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
     * DWM 窗口属性常量
     */
    public enum DwmAttribute {
        /**
         * 系统背景类型属性 ID
         */
        DWMWA_SYSTEMBACKDROP_TYPE(38);

        private final int value;

        DwmAttribute(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 窗口样式索引常量（GetWindowLongPtr/SetWindowLongPtr）
     */
    public enum WindowStyleIndex {

        /**
         * 普通窗口样式
         */
        GWL_STYLE(-16),

        /**
         * 扩展窗口样式索引
         */
        GWL_EXSTYLE(-20);

        private final int value;

        WindowStyleIndex(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 扩展窗口样式标志
     */
    public enum WindowStyleEx {
        /**
         * 分层窗口样式（Layered Window）
         * 必须设置此标志才能使用透明度和颜色键功能
         */
        WS_EX_LAYERED(0x00080000);

        private final int value;

        WindowStyleEx(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


    /**
     * 窗口样式常量（Window Style）
     *
     * <p>
     * 定义 Win32 API 中的窗口样式标志，用于 GetWindowLongPtr/SetWindowLongPtr。
     * 这些样式控制窗口的外观和行为。
     * </p>
     */
    public enum WindowStyle {
        /**
         * 子窗口样式
         * 必须是其他窗口的子窗口，不能拥有菜单栏
         */
        WS_CHILD(0x40000000L),

        /**
         * 可见窗口样式
         * 窗口初始为可见状态
         */
        WS_VISIBLE(0x10000000L),

        /**
         * 弹出窗口样式
         * 创建弹出窗口，不能与 WS_CHILD 同时使用
         */
        WS_POPUP(0x80000000L),

        /**
         * 标题栏样式
         * 创建带有标题栏的窗口（包含 WS_BORDER）
         */
        WS_CAPTION(0x00C00000L),

        /**
         * 可调整大小边框样式
         * 创建带有可调整大小边框的窗口
         */
        WS_THICKFRAME(0x00040000L),

        /**
         * 重叠窗口样式（标准顶层窗口）
         * 组合样式：WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX
         */
        WS_OVERLAPPEDWINDOW(0x00CF0000L);

        private final long value;

        WindowStyle(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }


    /**
     * 分层窗口属性标志（SetLayeredWindowAttributes）
     */
    public enum LayeredWindowAttribute {
        /**
         * 使用颜色键透明
         */
        LWA_COLORKEY(0x00000001),

        /**
         * 使用 Alpha 透明度
         */
        LWA_ALPHA(0x00000002);

        private final int value;

        LayeredWindowAttribute(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 窗口消息常量
     */
    public enum WindowMessage {
        /**
         * 非客户区鼠标左键按下消息
         */
        WM_NCLBUTTONDOWN(0x00A1);

        private final int value;

        WindowMessage(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 窗口点击测试代码（Hit Test）
     */
    public enum HitTestCode {
        /**
         * 标题栏区域
         */
        HTCAPTION(0x0002);

        private final int value;

        HitTestCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


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


}
