module com.bingbaihanji.bfxt {

    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    requires org.slf4j;
    requires org.fxyz3d.core;


    // 导出你需要对外访问的包
    exports com.bingbaihanji.bfxt.stage;
    exports com.bingbaihanji.bfxt.tools;



}

