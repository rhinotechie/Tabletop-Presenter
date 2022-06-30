package com.ryanaong.tabletoppresenter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;

public class TableTopApplication extends Application {

    GraphicsContext graphicsContext;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Creates a scene by importing the main-view fxml file.
        FXMLLoader fxmlLoader = new FXMLLoader(TableTopApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);

        BorderPane root = (BorderPane) scene.getRoot();

        // Attaches graphicsContext to fxml canvas.
        graphicsContext = ((Canvas) root.getRight()).getGraphicsContext2D();

        // Draws initial content in preview canvas.
        graphicsContext.fillRect(0, 0, 300, 300);
        graphicsContext.setStroke(Color.valueOf("#0000ff"));

        // Configures the current stage window
        stage.setTitle("Tabletop Presenter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}