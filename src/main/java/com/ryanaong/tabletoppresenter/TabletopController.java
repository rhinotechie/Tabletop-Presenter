package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


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
    public MediaPlayer mediaPlayer;
    public MenuItem startResumeItem;
    public MenuItem pauseMusicItem;
    public MenuItem restartMusicItem;

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
                    if (mediaPlayer != null){
                        mediaPlayer.stop();
                    }

                    // Plays selected music
                    String musicName = musicList.getSelectionModel().getSelectedItem();

                    try {
                        Media media = new Media(new File("./Music/" + musicName).toURI().toString());
                        mediaPlayer = new MediaPlayer(media);

                        // Updates media menu
                        startResumeItem.setDisable(true);
                        pauseMusicItem.setDisable(false);
                        restartMusicItem.setDisable(false);

                        mediaPlayer.play();
                    } catch (IllegalArgumentException iae){
                        // TODO: Notify user the URL and invalid
                    } catch (UnsupportedOperationException | MediaException e){
                        // TODO: Notify user of unplayable file
                    }


                }
            }
        });
        sceneList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sceneList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 != null){
                    String sceneName = sceneList.getSelectionModel().getSelectedItem();

                    // Opens json file and converts the String to a JSONObject.
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonObject;
                    try (FileReader fileReader = new FileReader("./Scenes/" + sceneName)){
                        Object obj = jsonParser.parse(fileReader);
                        jsonObject = (JSONObject) obj;
                    } catch (IOException | ParseException e) {
                        throw new RuntimeException(e);
                    }

                    // Selects the scene items from the lists.
                    backgroundList.getSelectionModel().select((String) jsonObject.get("background"));
                    foregroundList.getSelectionModel().select((String) jsonObject.get("foreground"));
                    musicList.getSelectionModel().select((String) jsonObject.get("music"));
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

    // Starts the music. Updates media menu.
    @FXML
    public void onStartResumeClicked(){
        if (mediaPlayer != null){
            if (mediaPlayer.getStatus() == MediaPlayer.Status.READY || mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED){
                // Enables/disables music menu items
                startResumeItem.setDisable(true);
                pauseMusicItem.setDisable(false);
                restartMusicItem.setDisable(false);

                mediaPlayer.play();
            }
        }
    }

    // Pauses the music to be resumed later. Updates media menu.
    @FXML
    public void onPauseClicked(){
        if (mediaPlayer != null){
            System.out.println(mediaPlayer.getStatus());
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                // Enables/disables music menu items
                startResumeItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                restartMusicItem.setDisable(false);

                mediaPlayer.pause();
            }
        }
    }

    // Plays music from the beginning. Updates media menu.
    @FXML
    public void onRestartClicked(){
        if (mediaPlayer != null){
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING || mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
                // Enables/disables music menu items
                startResumeItem.setDisable(true);
                pauseMusicItem.setDisable(false);
                restartMusicItem.setDisable(true);

                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            }
        }
    }

    @FXML
    public void onSaveSceneClicked(){
        // Alert the user that nothing is selected so the scene is not saved.
        if (backgroundList.getSelectionModel().isEmpty() && foregroundList.getSelectionModel().isEmpty() &&
                musicList.getSelectionModel().isEmpty()){
            // TODO: Notify user that nothing is selected
            return;
        }

        // Grabs the scene elements, stores them in a hashmap, and converts the map to a json object.
        Map<String, String> jsonMap = new HashMap<>();
        if (!backgroundList.getSelectionModel().isEmpty())
        {
            jsonMap.put("background", backgroundList.getSelectionModel().getSelectedItem());
        }
        if (!foregroundList.getSelectionModel().isEmpty())
        {
            jsonMap.put("foreground", foregroundList.getSelectionModel().getSelectedItem());
        }
        if (!musicList.getSelectionModel().isEmpty())
        {
            jsonMap.put("music", musicList.getSelectionModel().getSelectedItem());
        }
        JSONObject jsonObject = new JSONObject(jsonMap);

        // Prompts user for filename.
        // If it exists, it alerts the user that the file already exists.
        // Creates a new file and writes the json contents to it.
        // TODO: Get title from user
        try {
            File newFile = new File(".\\Scenes\\someTitle.json");
            if (!newFile.createNewFile()){
                //TODO: notify user of existing file
                System.out.println("File exists");
            } else {
                FileWriter fileWriter = new FileWriter(newFile.getAbsoluteFile());
                fileWriter.write(jsonObject.toJSONString());
                fileWriter.close();
            }
        } catch (IOException e){
            // TODO: inform user the file couldn't be created
        }
    }

    @FXML
    public void onExitClicked(){
        Platform.exit();
    }
}