package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @FXML
    public ImageView backgroundImage;
    @FXML
    public ImageView foregroundImage;

    public boolean foregroundVisibility;
    public MediaPlayer mediaPlayer;

    public void initialize(){
        File foregroundDir = new File("./Foregrounds");
        File backgroundDir = new File("./Backgrounds");
        File musicDir = new File("./Music");
        File sceneDir = new File("./Scenes");

        if (!foregroundDir.mkdir()) {
            String[] foregrounds = foregroundDir.list();
            foregroundList.getItems().setAll(foregrounds);
        }

        if (!backgroundDir.mkdir()) {
            String[] backgrounds = backgroundDir.list();
            backgroundList.getItems().setAll(backgrounds);
        }

        if (!musicDir.mkdir()) {
            String[] music = musicDir.list();
            musicList.getItems().setAll(music);
        }

        if (!sceneDir.mkdir()) {
            String[] scenes = sceneDir.list();
            sceneList.getItems().setAll(scenes);
        }

        // Enables click behavior for item lists.
        backgroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        backgroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String backgroundName = backgroundList.getSelectionModel().getSelectedItem();
                    Image image;
                    try (FileInputStream fileInputStream = new FileInputStream("./Backgrounds/" + backgroundName)) {
                        image = new Image(fileInputStream);
                        backgroundImage.setImage(image);
                    } catch (IllegalArgumentException | IOException e) {
                        // TODO: Alert user that resource couldn't be loaded.
                        System.out.println(backgroundName);
                    }
                }
            }
        });
        foregroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        foregroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String foregroundName = foregroundList.getSelectionModel().getSelectedItem();
                    Image image;
                    try (FileInputStream fileInputStream = new FileInputStream("./Foregrounds/" + foregroundName)) {
                        image = new Image(fileInputStream);
                        foregroundImage.setImage(image);
                    } catch (IllegalArgumentException | IOException e) {
                        // TODO: Alert user that resource couldn't be loaded.
                        System.out.println(foregroundName);
                    }
                }
            }
        });
        musicList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    // Prevents music from overlapping when changing music.
                    if (mediaPlayer != null){
                        mediaPlayer.stop();
                    }

                    String musicName = musicList.getSelectionModel().getSelectedItem();
                    Media media = new Media(new File("./Music/" + musicName).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.play();
                }
            }
        });
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
    }

    // Clears displayed background and unselects the resource from the list.
    @FXML
    public void onBackgroundClearedClicked(){
        backgroundImage.setImage(null);
        backgroundList.getSelectionModel().clearSelection();
    }

    // Clears displayed foreground and unselects the resource from the list.
    @FXML
    public void onForegroundClearedClicked(){
        foregroundImage.setImage(null);
        foregroundList.getSelectionModel().clearSelection();
    }

    // Clears displayed background and foreground and unselects the resources from the lists.
    @FXML
    public void onAllClearedClicked(){
        backgroundImage.setImage(null);
        backgroundList.getSelectionModel().clearSelection();
        foregroundImage.setImage(null);
        foregroundList.getSelectionModel().clearSelection();
    }

    @FXML
    public void onExitClicked(){
        Platform.exit();
    }
}