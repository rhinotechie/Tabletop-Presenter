package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class TabletopController {
    @FXML
    public ListView<String> scenesList;
    @FXML
    public ListView<String> foregroundList;
    @FXML
    public ListView<String> backgroundList;
    @FXML
    public ListView<String> musicList;
    @FXML
    public ListView<String> sceneList;
    @FXML
    public Canvas previewCanvas;

    @FXML
    public void onExitClicked(){
        Platform.exit();
    }
}