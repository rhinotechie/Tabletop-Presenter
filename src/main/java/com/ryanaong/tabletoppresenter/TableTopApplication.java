package com.ryanaong.tabletoppresenter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class TableTopApplication extends Application {

    private GraphicsContext graphicsContext;

    @Override
    public void start(Stage mainStage) throws IOException {
        // Locate the layout file for the mainStage's scene.
        URL url = getClass().getResource("mainScene.fxml");
        if (Objects.isNull(url)){
            Platform.exit();
            return;
        }

        // Imports the main-view fxml file and sets it as the scene's main parent node.
        Parent parent = FXMLLoader.load(url);
        Scene scene = new Scene(parent, 800, 540);

        // Attaches graphicsContext to fxml canvas.
        graphicsContext = ((Canvas) scene.lookup("#previewCanvas")).getGraphicsContext2D();

        // Draws initial content in preview canvas.
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setFill(Color.GRAY);
        graphicsContext.fillRect(0, 0, 400, 400);

        // Configures the application window
        mainStage.setTitle("Tabletop Presenter");
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }
}