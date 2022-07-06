package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.*;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.json.*;



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
    @FXML
    public MenuItem startResumeItem;
    @FXML
    public MenuItem pauseMusicItem;
    @FXML
    public MenuItem restartMusicItem;
    @FXML
    public BorderPane mainBorderPane;

    public MediaPlayer mediaPlayer;
    public MenuItem startPresenterItem;
    public MenuItem endPresenterItem;
    public MenuItem freezePresenterItem;
    public MenuItem unfreezePresenterItem;
    public Label frozenLabel;
    private Stage mainStage;
    private Stage displayWindow;
    private ImageView liveBackgroundImageView;
    private ImageView liveForegroundImageView;
    private boolean isFrozen = false;


    public void initialize(){
        onRefreshResourcesClicked();

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
                        if (liveBackgroundImageView != null && !isFrozen){
                            liveBackgroundImageView.setImage(image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Background Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load background with that name from project resource folder.");
                        alert.showAndWait();
                    } catch (Exception e){
                        e.printStackTrace();
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
                        if (liveForegroundImageView != null && !isFrozen){
                            liveForegroundImageView.setImage(image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Foreground Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load foreground with that name from project resource folder.");
                        alert.showAndWait();
                    } catch (Exception e){
                        e.printStackTrace();
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
                        iae.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Music Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load music with that name from project resource folder.");
                        alert.showAndWait();
                    } catch (UnsupportedOperationException | MediaException e){
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Incompatible Music");
                        alert.setHeaderText(null);
                        alert.setContentText("Selected music is not compatible with this program.");
                        alert.showAndWait();
                    }


                }
            }
        });
        sceneList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sceneList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 != null){
                    // Clears preview and live images and audio in case a scene has 2 or 1 item.
                    backgroundImage.setImage(null);
                    foregroundImage.setImage(null);
                    if (liveBackgroundImageView != null && !isFrozen){
                        liveBackgroundImageView.setImage(null);
                    }
                    if(liveForegroundImageView != null && !isFrozen){
                        liveForegroundImageView.setImage(null);
                    }
                    if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING){
                        mediaPlayer.pause();
                    }

                    String sceneName = sceneList.getSelectionModel().getSelectedItem();

                    JSONObject jsonObject;
                    try (FileReader fileReader = new FileReader("./Scenes/" + sceneName); Scanner scanner = new Scanner(fileReader)){
                        StringBuilder sb = new StringBuilder();
                        while (scanner.hasNextLine()){
                            sb.append(scanner.nextLine());
                        }

                        jsonObject = new JSONObject(sb.toString());
                        // Selects the scene items from the lists.
                        if (jsonObject.has("background")){
                            backgroundList.getSelectionModel().select((String) jsonObject.get("background"));
                        }
                        if (jsonObject.has("foreground")){
                            foregroundList.getSelectionModel().select((String) jsonObject.get("foreground"));
                        }
                        if (jsonObject.has("music")) {
                            musicList.getSelectionModel().select((String) jsonObject.get("music"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Scene import error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load scene from project resource folder.");
                        alert.showAndWait();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @FXML
    public void onLoadBackground(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import background");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(mainStage);
            if (sourceFile == null) {
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get("./Backgrounds/" + sourceFile.getName());
            Files.copy(sourcePath, destinationPath);

            onRefreshResourcesClicked();
        } catch (AccessDeniedException ade) {
            ade.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "Check the permissions for source and destination and make sure your anti-virus " +
                    "is not denying access. You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        } catch(FileAlreadyExistsException fileAlreadyExistsException){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File exists");
            alert.setHeaderText(null);
            alert.setContentText("A file with that name already exists in the resource directory.");
            alert.showAndWait();
        } catch (IOException ioe){
            ioe.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        }
    }

    @FXML
    public void onLoadForeground(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import foreground");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(mainStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get("./Foregrounds/" + sourceFile.getName());
            Files.copy(sourcePath, destinationPath);

            onRefreshResourcesClicked();
        } catch (AccessDeniedException ade) {
            ade.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "Check the permissions for source and destination and make sure your anti-virus " +
                    "is not denying access. You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        } catch(FileAlreadyExistsException fileAlreadyExistsException){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File exists");
            alert.setHeaderText(null);
            alert.setContentText("A file with that name already exists in the resource directory.");
            alert.showAndWait();
        } catch (IOException ioe){
            ioe.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        }
    }

    @FXML
    public void onLoadMusic(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import music");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(mainStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get("./Music/" + sourceFile.getName());
            Files.copy(sourcePath, destinationPath);

            onRefreshResourcesClicked();
        } catch (AccessDeniedException ade) {
            ade.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Access Denied");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "Check the permissions for source and destination and make sure your anti-virus " +
                    "is not denying access. You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        } catch(FileAlreadyExistsException fileAlreadyExistsException){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File exists");
            alert.setHeaderText(null);
            alert.setContentText("A file with that name already exists in the resource directory.");
            alert.showAndWait();
        } catch (IOException ioe){
            ioe.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Import Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a copy of the file into the resource directory. " +
                    "You can copy resources directly into the project resource folder "
                    + "then click 'File > Reload resources'");
            alert.showAndWait();
        }
    }

    @FXML
    public void onStartPresenter(){
        if (displayWindow == null){
            displayWindow = new Stage();
            displayWindow.setMinWidth(300);
            displayWindow.setMinHeight(300);
            displayWindow.initModality(Modality.NONE);
            displayWindow.setTitle("Tabletop Presenter - Audience Display");
            displayWindow.setOnCloseRequest(windowEvent -> onEndPresenter());

            StackPane stackPane = new StackPane();
            stackPane.setPrefHeight(600);
            stackPane.setPrefWidth(600);
            stackPane.setMinWidth(300);
            stackPane.setMinHeight(300);
            stackPane.setMaxHeight(Double.POSITIVE_INFINITY);
            stackPane.setMaxWidth(Double.POSITIVE_INFINITY);
            stackPane.setStyle("-fx-background-color: black");

            Scene displayScene = new Scene(stackPane);
            displayScene.setFill(Color.DARKGRAY);

            displayWindow.setScene(displayScene);

            liveBackgroundImageView = new ImageView();
            liveBackgroundImageView.fitWidthProperty().bind(displayWindow.widthProperty());
            liveBackgroundImageView.fitHeightProperty().bind(displayWindow.heightProperty());
            liveBackgroundImageView.setPreserveRatio(true);
            if (backgroundImage.getImage() != null) {
                Image liveBackgroundImage = backgroundImage.getImage();
                liveBackgroundImageView.setImage(liveBackgroundImage);
            }

            stackPane.getChildren().add(liveBackgroundImageView);

            liveForegroundImageView = new ImageView();
            liveForegroundImageView.fitWidthProperty().bind(displayWindow.widthProperty());
            liveForegroundImageView.fitHeightProperty().bind(displayWindow.heightProperty());
            liveForegroundImageView.setPreserveRatio(true);
            if (foregroundImage.getImage() != null) {
                Image liveForegroundImage = foregroundImage.getImage();
                liveForegroundImageView.setImage(liveForegroundImage);
            }

            stackPane.getChildren().add(liveForegroundImageView);

            displayWindow.show();
            
            // Update menu items
            startPresenterItem.setDisable(true);
            endPresenterItem.setDisable(false);
            freezePresenterItem.setDisable(false);
            unfreezePresenterItem.setDisable(true);
        }
    }

    @FXML
    public void onAboutClicked(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("About");
        dialog.setContentText("Tabletop Presenter by Ryan Ong\n" +
                "Version 1.0\n" +
                "https://github.com/rhinotechie/Tabletop-Presenter.git");

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    @FXML
    public void onUsersGuideClicked(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("User's Guide");
        dialog.setContentText(
                "Import background/foreground images and music by either using 'File > Import [Resource Type]'" +
                        "or copy resources directly to the program resource directories.\n\n" +
                "Select up to one background, one foreground, and music for a scene.\n\n" +
                "Choose 'Presenter > Start Presentation' to open the presenter window for your secondary monitor.\n\n" +
                "Selecting a resource updates the live window unless you select 'Presenter > Freeze presentation'.\n\n" +
                "Save the preview screen's background, foreground and/or music as a scene with 'Scene > Save scene'");

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }



    @FXML
    public void onEndPresenter(){
        if (displayWindow != null){
            displayWindow.close();
            displayWindow = null;
        }

        // Update menu items
        startPresenterItem.setDisable(false);
        endPresenterItem.setDisable(true);
        freezePresenterItem.setDisable(true);
        unfreezePresenterItem.setDisable(true);
    }

    @FXML
    public void onFreezePresenter(){
        this.isFrozen = true;
        frozenLabel.setVisible(true);
        freezePresenterItem.setDisable(true);
        unfreezePresenterItem.setDisable(false);
    }

    @FXML
    public void onUnFreezePresenter(){
        this.isFrozen = false;
        frozenLabel.setVisible(false);
        freezePresenterItem.setDisable(false);
        unfreezePresenterItem.setDisable(true);

        if(liveBackgroundImageView != null){
            liveBackgroundImageView.setImage(backgroundImage.getImage());
        }
        if(liveForegroundImageView != null){
            liveForegroundImageView.setImage(foregroundImage.getImage());
        }
    }

    // Reloads the resource lists.
    // Re-selects the items that were unselected.
    @FXML
    public void onRefreshResourcesClicked(){
        // Saves previous selections (if any)
        String prevBackground = null;
        String prevForeGround = null;
        String prevMusic = null;

        if(!backgroundList.getSelectionModel().isEmpty()){
            prevBackground = backgroundList.getSelectionModel().getSelectedItem();
        }
        if(!foregroundList.getSelectionModel().isEmpty()){
            prevForeGround = foregroundList.getSelectionModel().getSelectedItem();
        }
        if(!musicList.getSelectionModel().isEmpty()){
            prevMusic = musicList.getSelectionModel().getSelectedItem();
        }

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

        // Restores previous selections (if any)
        // Saves previous selections (if any)
        if (prevBackground != null){
            backgroundList.getSelectionModel().select(prevBackground);
        }
        if (prevForeGround != null){
            foregroundList.getSelectionModel().select(prevForeGround);
        }
        if (prevMusic != null){
            musicList.getSelectionModel().select(prevMusic);
        }
    }

    // Clears preview and live background and unselects the resource from the list.
    @FXML
    public void onBackgroundClearedClicked(){
        backgroundImage.setImage(null);
        backgroundList.getSelectionModel().clearSelection();

        if (liveBackgroundImageView != null && !isFrozen){
            liveBackgroundImageView.setImage(null);
        }
    }

    // Clears preview and live foreground and unselects the resource from the list.
    @FXML
    public void onForegroundClearedClicked(){
        foregroundImage.setImage(null);
        foregroundList.getSelectionModel().clearSelection();

        if (liveForegroundImageView != null && !isFrozen){
            liveForegroundImageView.setImage(null);
        }
    }

    // Clears preview and live backgrounds, foregrounds, stops music, and unselects the resources from the lists.
    @FXML
    public void onAllClearedClicked(){
        backgroundImage.setImage(null);
        backgroundList.getSelectionModel().clearSelection();
        foregroundImage.setImage(null);
        foregroundList.getSelectionModel().clearSelection();

        if (liveBackgroundImageView != null && !isFrozen){
            liveBackgroundImageView.setImage(null);
        }
        if (liveForegroundImageView != null && !isFrozen){
            liveForegroundImageView.setImage(null);
        }

        sceneList.getSelectionModel().clearSelection();

        musicList.getSelectionModel().clearSelection();
        if (mediaPlayer != null){
            this.onPauseClicked();
        }
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Nothing Selected");
            alert.setHeaderText(null);
            alert.setContentText("No resources selected so a scene was not saved.");
            alert.showAndWait();
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
        String sceneFileName = this.showNewSceneDialog();
        if (sceneFileName == null || sceneFileName.isEmpty()){
            return;
        }

        // Creates a new file and writes the json contents to it.
        try {
            File newFile = new File(".\\Scenes\\" + sceneFileName + ".json");
            if (!newFile.createNewFile()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File creation error");
                alert.setHeaderText(null);
                alert.setContentText("A scene already exists with that name.");
                alert.showAndWait();
            } else {
                FileWriter fileWriter = new FileWriter(newFile.getAbsoluteFile());
                if (jsonObject.toString() == null) {
                    throw new IOException("Could not convert json to String format before writing it to file.");
                }
                fileWriter.write(jsonObject.toString());
                fileWriter.close();
            }
        } catch (FileAlreadyExistsException fileAlreadyExistsException){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File creation error");
            alert.setHeaderText(null);
            alert.setContentText("A scene already exists with that name.");
            alert.showAndWait();
        } catch (IOException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File creation error");
            alert.setHeaderText(null);
            alert.setContentText("Could not create this scene in the project directory.");
            alert.showAndWait();
        } catch (Exception e){
            e.printStackTrace();
        }

        onRefreshResourcesClicked();
    }

    // Displays dialog for naming a new scene.
    @FXML
    public String showNewSceneDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("newSceneDialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            // Grabs the name from the text field and returns it.
            NewSceneDialogController newSceneDialogController = fxmlLoader.getController();
            return newSceneDialogController.getSceneName();
        } else {
            return null;
        }
    }

    @FXML
    public void onExitClicked(){
        Platform.exit();
    }
}