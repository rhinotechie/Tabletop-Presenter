package com.ryanaong.tabletoppresenter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class TableTopApplication extends Application {

    private GraphicsContext graphicsContext;

    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Creates a scene by importing the main-view fxml file.
        FXMLLoader fxmlLoader = new FXMLLoader(TableTopApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 540);
        TabletopController tableTopController = (TabletopController) fxmlLoader.getController();

        // Attaches graphicsContext to fxml canvas.
        graphicsContext = ((Canvas) scene.lookup("#previewCanvas")).getGraphicsContext2D();

        // Draws initial content in preview canvas.
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setFill(Color.GRAY);
        graphicsContext.fillRect(0, 0, 400, 400);

        // Configures the current stage window
        stage.setTitle("Tabletop Presenter");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }
}