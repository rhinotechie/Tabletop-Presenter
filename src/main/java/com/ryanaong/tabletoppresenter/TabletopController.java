package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.ArrayList;
import java.util.List;

public class TabletopController {
    @FXML
    public ListView<String> sceneList;
    @FXML
    public ListView<String> foregroundList;
    @FXML
    public ListView<String> backgroundList;
    @FXML
    public ListView<String> musicList;
    @FXML
    public Canvas previewCanvas;

    private List<String> testMusic;

    public void initialize(){

        // Test data for item lists
        // TODO: Remove test data
        testMusic = new ArrayList<>();
        testMusic.add("Dark Cave");
        testMusic.add("Ocean");
        testMusic.add("Haunted House");
        musicList.getItems().setAll(testMusic);

        // Enables click behavior for item lists.
        sceneList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sceneList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 != null){
                    String scene = sceneList.getSelectionModel().getSelectedItem();
                    //TODO: Load resource from project directory using item string.
                }
            }
        });
        foregroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        foregroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String foreground = foregroundList.getSelectionModel().getSelectedItem();
                    //TODO: Load resource from project directory using item string.
                }
            }
        });
        backgroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        backgroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String background = backgroundList.getSelectionModel().getSelectedItem();
                    //TODO: Load resource from project directory using item string.
                }
            }
        });
        musicList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String music = musicList.getSelectionModel().getSelectedItem();
                    //TODO: Load resource from project directory using item string.
                }
            }
        });
    }

    @FXML
    public void onExitClicked(){
        Platform.exit();
    }
}