package com.bingbaihanji.bfxt.stage;

/**
 * 窗口主题配置类
 * 定义窗口标题栏、背景、按钮、菜单等的颜色和样式
 *
 * @param titleBarBgColor      标题栏背景颜色
 * @param windowBgColor        窗口主体背景颜色
 * @param titleTextColor       标题文字颜色
 * @param btnStyleTransparent  窗口按钮默认样式（完全透明背景）
 * @param btnStyleHover        窗口按钮鼠标悬停样式
 * @param btnStylePressed      窗口按钮鼠标按下样式
 * @param closeBtnStyleHover   关闭按钮鼠标悬停样式
 * @param closeBtnStylePressed 关闭按钮鼠标按下样式
 * @param menuBgColor          菜单背景颜色
 * @param menuTextColor        菜单文字颜色
 * @param menuHoverBgColor     菜单项悬停背景颜色
 * @param menuHoverTextColor   菜单项悬停文字颜色
 */
public record WindowTheme(String titleBarBgColor, String windowBgColor, String titleTextColor,
                          String btnStyleTransparent, String btnStyleHover, String btnStylePressed,
                          String closeBtnStyleHover, String closeBtnStylePressed,
                          String menuBgColor, String menuTextColor, String menuHoverBgColor,
                          String menuHoverTextColor) {


    /*
     * 暗色主题（深色背景，适合夜间使用）
     */
    public static WindowTheme dark() {
        return new WindowTheme(
                "#3c3f41",  // 标题栏背景：深灰色
                "#2b2b2b",  // 窗口背景：更深的灰色
                "#bbbbbb",  // 标题文字：浅灰色
                createButtonStyle("transparent"),  // 按钮默认：透明
                createButtonStyle("rgba(255,255,255,0.10)"),  // 按钮悬停：10% 白色
                createButtonStyle("rgba(255,255,255,0.18)"),  // 按钮按下：18% 白色
                createButtonStyle("#c42b1c"),  // 关闭按钮悬停：红色
                createButtonStyle("#a81f16"),   // 关闭按钮按下：深红色
                "#3c3c3c",  // 菜单背景：深灰色
                "#bbbbbb",  // 菜单文字：浅灰色
                "#4e5254",  // 菜单项悬停背景：稍亮的灰色
                "#ffffff"   // 菜单项悬停文字：白色
        );
    }

    /*
     * 亮色主题（浅色背景，适合日间使用）
     */
    public static WindowTheme light() {
        return new WindowTheme(
                "#f0f0f0",  // 标题栏背景：浅灰色
                "#ffffff",  // 窗口背景：白色
                "#333333",  // 标题文字：深灰色
                createButtonStyle("transparent"),  // 按钮默认：透明
                createButtonStyle("rgba(0,0,0,0.06)"),  // 按钮悬停：6% 黑色
                createButtonStyle("rgba(0,0,0,0.12)"),  // 按钮按下：12% 黑色
                createButtonStyle("#c42b1c"),  // 关闭按钮悬停：红色（保持一致）
                createButtonStyle("#a81f16"),   // 关闭按钮按下：深红色（保持一致）
                "#ffffff",  // 菜单背景：白色
                "#333333",  // 菜单文字：深灰色
                "#e5f3ff",  // 菜单项悬停背景：浅蓝色
                "#000000"   // 菜单项悬停文字：黑色
        );
    }

    //  辅助方法

    /*
     * 创建窗口按钮样式字符串
     */
    private static String createButtonStyle(String backgroundColor) {
        return """
                -fx-background-color: %s;
                -fx-padding: 0;
                -fx-alignment: center;
                """.formatted(backgroundColor);
    }

    /*
     * 生成菜单主题CSS样式
     * 用于控制MenuItem的背景和文字颜色
     */
    public String generateMenuCSS() {
        return """
                .context-menu {
                    -fx-background-color: %s;
                }
                .menu-item {
                    -fx-background-color: %s;
                }
                .menu-item .label {
                    -fx-text-fill: %s;
                }
                .menu-item:focused {
                    -fx-background-color: %s;
                }
                .menu-item:focused .label {
                    -fx-text-fill: %s;
                }
                .menu-item:hover {
                    -fx-background-color: %s;
                }
                .menu-item:hover .label {
                    -fx-text-fill: %s;
                }
                """.formatted(
                menuBgColor,         // context-menu背景
                menuBgColor,         // menu-item默认背景
                menuTextColor,       // menu-item默认文字
                menuHoverBgColor,    // menu-item聚焦背景
                menuHoverTextColor,  // menu-item聚焦文字
                menuHoverBgColor,    // menu-item悬停背景
                menuHoverTextColor   // menu-item悬停文字
        );
    }

}
