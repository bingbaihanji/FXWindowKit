package com.bingbaihanji.bfxwt.test;

import com.bingbaihanji.bfxwt.stage.DefaultLayout;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class App extends DefaultLayout {

    @Override
    protected Node createNode() {
        BorderPane root = new BorderPane();


        var top = new HBox();
        top.setPrefHeight(40);
        top.setStyle("""
                
                 -fx-background-color: rgb(0,255,255);
                
                """);
        root.setTop(top);

        var vbox = new VBox();
        vbox.setStyle("""
                
                 -fx-background-color: rgb(255,255,0);
                
                """);
        root.setCenter(vbox);
        return root;
    }


}
