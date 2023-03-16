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
    public static void main(String[] args) {
        Application.launch();
    }

    @Override
    public void start(Stage mainStage) throws IOException {
        // Imports the layout file for the mainStage's scene.
        URL url = getClass().getResource("mainScene.fxml");
        if (Objects.isNull(url)){
            Platform.exit();
            return;
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent parent = loader.load();
        Scene scene = new Scene(parent, 800, 540);

        TabletopController tabletopController = loader.getController();
        tabletopController.setHostingStage(mainStage);

        // Draws initial content in preview canvas.
        Canvas canvas = (Canvas) scene.lookup("#previewCanvas");
        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setFill(Color.GRAY);
        graphicsContext.fillRect(0, 0, 400, 400);

        // Configures the application window
        mainStage.setTitle("Tabletop Presenter");
        mainStage.setScene(scene);
        mainStage.setResizable(false);
        mainStage.show();
    }
}