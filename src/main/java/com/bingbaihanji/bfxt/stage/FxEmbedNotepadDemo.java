package com.bingbaihanji.bfxt.stage;

import com.sun.glass.ui.Window;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static com.sun.jna.platform.win32.WinUser.*;

public class FxEmbedNotepadDemo extends Application {

    private WinDef.HWND fxHwnd;
    private WinDef.HWND hostHwnd;
    private WinDef.HWND childHwnd;

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("FX + Win32 Host + Notepad");
        stage.setScene(scene);
        stage.show();

        Platform.runLater(() -> {
            fxHwnd = getFxHwnd();
            createHostHwnd(scene);
            launchAndEmbedNotepad(scene);
        });
    }

    // 1️⃣ 获取 JavaFX 窗口 HWND
    private WinDef.HWND getFxHwnd() {
        long hwnd = Window.getWindows().getFirst().getNativeWindow();
        return new WinDef.HWND(Pointer.createConstant(hwnd));
    }

    // 2️⃣ 创建真正的 Win32 Host HWND
    private void createHostHwnd(Scene scene) {
        hostHwnd = User32.INSTANCE.CreateWindowEx(
                0,
                "STATIC",
                "",
                WS_CHILD | WS_VISIBLE,
                0, 0,
                (int) scene.getWidth(),
                (int) scene.getHeight(),
                fxHwnd,
                null,
                Kernel32.INSTANCE.GetModuleHandle(null),
                null
        );
    }

    // 3️⃣ 启动并嵌入 Notepad
    private void launchAndEmbedNotepad(Scene scene) {
        try {
            Runtime.getRuntime().exec("notepad.exe");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        new Thread(() -> {
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}

            // Notepad 的稳定窗口类名
            childHwnd = User32.INSTANCE.FindWindow("Notepad", null);
            if (childHwnd == null) {
                System.out.println("Notepad not found");
                return;
            }

            embed(childHwnd, scene);
        }).start();
    }

    // 4️⃣ 嵌入逻辑
    private void embed(WinDef.HWND child, Scene scene) {
        User32.INSTANCE.SetParent(child, hostHwnd);

        long style = User32.INSTANCE.GetWindowLongPtr(child, GWL_STYLE).longValue();
        style &= ~(WS_POPUP | WS_CAPTION | WS_THICKFRAME);
        style |= WS_CHILD | WS_VISIBLE;

        User32.INSTANCE.SetWindowLongPtr(
                child,
                GWL_STYLE,
                new BaseTSD.LONG_PTR(style).toPointer()
        );

        User32.INSTANCE.SetWindowPos(
                child,
                null,
                0, 0,
                (int) scene.getWidth(),
                (int) scene.getHeight(),
                SWP_NOZORDER | SWP_FRAMECHANGED
        );

        Platform.runLater(() -> {
            scene.widthProperty().addListener((o, a, b) -> resize(scene));
            scene.heightProperty().addListener((o, a, b) -> resize(scene));
        });
    }

    private void resize(Scene scene) {
        int w = (int) scene.getWidth();
        int h = (int) scene.getHeight();

        User32.INSTANCE.SetWindowPos(hostHwnd, null, 0, 0, w, h, SWP_NOZORDER);
        User32.INSTANCE.SetWindowPos(childHwnd, null, 0, 0, w, h, SWP_NOZORDER);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
