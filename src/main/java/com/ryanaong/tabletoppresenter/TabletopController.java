package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

import org.controlsfx.control.StatusBar;
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
    public MenuItem startMusicItem;
    @FXML
    public MenuItem pauseMusicItem;
    @FXML
    public MenuItem stopMusicItem;
    @FXML
    public BorderPane mainBorderPane;

    public MediaPlayer mediaPlayer;
    public MenuItem startPresenterItem;
    public MenuItem endPresenterItem;
    public MenuItem freezePresenterItem;
    public MenuItem unfreezePresenterItem;
    public Label frozenLabel;
    public StatusBar statusBar;
    public TitledPane sceneTitlePane;
    public Label presenterStatus;
    public CheckBox stretchBackgroundCheckBox;
    public Slider foregroundScaleSlider;
    public ColorPicker colorPicker;
    public MenuItem restartMusicItemItem;
    public MenuItem resumeMusicItem;
    private Stage mainStage;
    private Stage displayWindow;
    private ImageView liveBackgroundImageView;
    private ImageView liveForegroundImageView;
    private boolean isFrozen = false;

    public void initialize(){
        // Ensures project directories are created if they aren't already
        onRefreshResourcesClicked();

        // Enables click behavior for item lists.
        backgroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        backgroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    // Updates preview window with clicked resource
                    String backgroundName = backgroundList.getSelectionModel().getSelectedItem();

                    // Do nothing since nothing is selected
                    if (backgroundName == null){
                        return;
                    }

                    Image image;
                    try (FileInputStream fileInputStream = new FileInputStream("./Backgrounds/" + backgroundName)) {
                        image = new Image(fileInputStream);
                        backgroundImage.setImage(image);
                        if (liveBackgroundImageView != null && !isFrozen){
                            liveBackgroundImageView.setImage(image);
                        }
                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Background Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load background from project resource folder.");
                        alert.showAndWait();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        backgroundList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY){
                    onDeleteResource("Backgrounds");
                }
            }
        });
        foregroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        foregroundList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            // Updates preview window with clicked resource
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (t1 != null){
                    String foregroundName = foregroundList.getSelectionModel().getSelectedItem();

                    // Do nothing since nothing is selected
                    if (foregroundName == null){
                        return;
                    }

                    Image image;
                    try (FileInputStream fileInputStream = new FileInputStream("./Foregrounds/" + foregroundName)) {
                        image = new Image(fileInputStream);
                        foregroundImage.setImage(image);
                        if (liveForegroundImageView != null && !isFrozen){
                            liveForegroundImageView.setImage(image);
                        }
                    } catch (IOException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Foreground Load Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Unable to load foreground from project resource folder.");
                        alert.showAndWait();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        foregroundList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY){
                    onDeleteResource("Foregrounds");
                }
            }
        });
        musicList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        musicList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY){
                    onDeleteResource("Music");
                }
            }
        });
        sceneList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sceneList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if(t1 != null){
                    // Clears preview and live images and audio in case a scene has 1 or 2 items.
                    backgroundImage.setImage(null);
                    foregroundImage.setImage(null);
                    if (liveBackgroundImageView != null && !isFrozen){
                        liveBackgroundImageView.setImage(null);
                    }
                    if(liveForegroundImageView != null && !isFrozen){
                        liveForegroundImageView.setImage(null);
                    }

                    clearMediaPlayer();

                    // Deselects all normal resource names to be reselected later
                    backgroundList.getSelectionModel().clearSelection();
                    foregroundList.getSelectionModel().clearSelection();
                    musicList.getSelectionModel().clearSelection();

                    // Updates background, foreground, and music from selected scene.
                    String sceneName = sceneList.getSelectionModel().getSelectedItem();
                    JSONObject jsonObject;
                    try (FileReader fileReader = new FileReader("./Scenes/" + sceneName); Scanner scanner = new Scanner(fileReader)){
                        StringBuilder sb = new StringBuilder();
                        while (scanner.hasNextLine()){
                            sb.append(scanner.nextLine());
                        }

                        // Selects the scene items from the lists.
                        jsonObject = new JSONObject(sb.toString());
                        if (jsonObject.has("background")){
                            backgroundList.getSelectionModel().select((String) jsonObject.get("background"));
                        }
                        if (jsonObject.has("foreground")){
                            foregroundList.getSelectionModel().select((String) jsonObject.get("foreground"));
                        }
                        if (jsonObject.has("music")) {
                            musicList.getSelectionModel().select((String) jsonObject.get("music"));
                            playMusic();
                        }
                    } catch (IOException e) {
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
        sceneList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY){
                    onDeleteResource("Scenes");
                }
            }
        });
        foregroundScaleSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            // Changes foreground scale of live/preview windows
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                onRatioSliderChanged();
            }
        });
        colorPicker.setValue(Color.GRAY);
        colorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            // Changes background color of live/preview windows
            public void handle(ActionEvent actionEvent) {
                if (colorPicker.getValue() != null) {
                    GraphicsContext gc = previewCanvas.getGraphicsContext2D();
                    gc.setFill(colorPicker.getValue());
                    gc.fillRect(0, 0, 400, 400);

                    if (displayWindow != null){
                        displayWindow.getScene().setFill(colorPicker.getValue());
                    }
                }
            }
        });
    }

    @FXML
    // Toggle stretch/non-stretched backgrounds for preview and live windows.
    public void onStretchClicked(){
        backgroundImage.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
        if (liveBackgroundImageView != null && !isFrozen){
            liveBackgroundImageView.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
        }
    }

    @FXML
    // Changes scale of foreground image according to slider
    public void onRatioSliderChanged(){
        if (foregroundImage != null){
            foregroundImage.setScaleX(foregroundScaleSlider.getValue());
            foregroundImage.setScaleY(foregroundScaleSlider.getValue());
        }
        if (liveForegroundImageView != null && !isFrozen){
            liveForegroundImageView.setScaleX(foregroundScaleSlider.getValue());
            liveForegroundImageView.setScaleY(foregroundScaleSlider.getValue());
        }
    }

    @FXML
    // When the user selects the import background option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
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

            // Refreshes resource so the new file can be seen.
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
    // When the user selects the import foreground option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
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

            // Refreshes resource so the new file can be seen.
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

    // When the user selects the import music option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
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

            // Refreshes resource so the new file can be seen.
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
    // When the user selects the Start Presenter option in the MenuBar, the live window opens and mirrors the preview
    // window, updates MenuBar items, and updates status bar.
    public void onStartPresenter(){
        if (displayWindow == null){
            // Stage configuration
            displayWindow = new Stage();
            displayWindow.setMinWidth(300);
            displayWindow.setMinHeight(300);
            displayWindow.initModality(Modality.NONE);
            displayWindow.setTitle("Tabletop Presenter - Audience Display");
            displayWindow.setOnCloseRequest(windowEvent -> onEndPresenter());

            // StackPane configuration
            StackPane stackPane = new StackPane();
            stackPane.setPrefHeight(600);
            stackPane.setPrefWidth(600);
            stackPane.setMinWidth(300);
            stackPane.setMinHeight(300);
            stackPane.setMaxHeight(Double.POSITIVE_INFINITY);
            stackPane.setMaxWidth(Double.POSITIVE_INFINITY);
            stackPane.setStyle("-fx-background-color: transparent");

            Scene displayScene = new Scene(stackPane);

            // Mirrors preview's background color
            if (colorPicker.getValue() != null){
                displayScene.setFill(colorPicker.getValue());
            }

            displayWindow.setScene(displayScene);

            // Mirrors preview's background
            liveBackgroundImageView = new ImageView();
            liveBackgroundImageView.fitWidthProperty().bind(displayWindow.widthProperty());
            liveBackgroundImageView.fitHeightProperty().bind(displayWindow.heightProperty());
            if (backgroundImage.getImage() != null) {
                Image liveBackgroundImage = backgroundImage.getImage();
                liveBackgroundImageView.setImage(liveBackgroundImage);

                // Determine whether to stretch or keep image ratio.
                liveBackgroundImageView.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
            }

            stackPane.getChildren().add(liveBackgroundImageView);

            // Mirrors preview's foreground
            liveForegroundImageView = new ImageView();
            liveForegroundImageView.fitWidthProperty().bind(displayWindow.widthProperty());
            liveForegroundImageView.fitHeightProperty().bind(displayWindow.heightProperty());
            liveForegroundImageView.setPreserveRatio(true);
            if (foregroundImage.getImage() != null) {
                Image liveForegroundImage = foregroundImage.getImage();
                liveForegroundImageView.setImage(liveForegroundImage);
                liveForegroundImageView.setScaleX(foregroundScaleSlider.getValue());
                liveForegroundImageView.setScaleY(foregroundScaleSlider.getValue());
            }
            stackPane.getChildren().add(liveForegroundImageView);

            // Shows window after the mirrored images are loaded
            displayWindow.show();
            
            // Update menu items
            startPresenterItem.setDisable(true);
            endPresenterItem.setDisable(false);
            freezePresenterItem.setDisable(false);
            unfreezePresenterItem.setDisable(true);

            presenterStatus.setText("Presenting");
        }
    }

    @FXML
    // When the presenter window is open and the user ends the presentation either by closing the window or selecting
    // the End Presentation option in the MenuBar, the window is closed (if open), only the Start Presentation option
    // is enabled in the MenuBar, and the StatusBar is updated. Clears frozen status.
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

        // Clears frozen status
        isFrozen = false;
        frozenLabel.setVisible(false);

        // Updates status bar
        presenterStatus.setText("Not presenting");
    }

    @FXML
    // When the presenter window is open and the user selects Freeze Presentation in the MenuBar, the live window no
    // longer updates whenever the preview window is updated until it is unfrozen and a frozen status text is displayed.
    public void onFreezePresenter(){
        this.isFrozen = true;

        // Reveals frozen text in the top left corner
        frozenLabel.setVisible(true);

        // Updates MenuBar items
        freezePresenterItem.setDisable(true);
        unfreezePresenterItem.setDisable(false);

        presenterStatus.setText("Presenter frozen");
    }

    @FXML
    // When the presenter window is open and the user selects Unfreeze Presentation in the MenuBar, the live window
    // immediately starts mirroring the preview window.
    public void onUnFreezePresenter(){
        this.isFrozen = false;

        // Hides frozen status text
        frozenLabel.setVisible(false);

        // Updates MenuBar items
        freezePresenterItem.setDisable(false);
        unfreezePresenterItem.setDisable(true);

        // Immediately mirrors to what is displayed in the preview window including ratio and foreground scale.
        if(liveBackgroundImageView != null){
            liveBackgroundImageView.setImage(backgroundImage.getImage());
            liveBackgroundImageView.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
        }
        if(liveForegroundImageView != null){
            liveForegroundImageView.setImage(foregroundImage.getImage());
            liveForegroundImageView.setScaleX(foregroundScaleSlider.getValue());
            liveForegroundImageView.setScaleY(foregroundScaleSlider.getValue());
        }

        presenterStatus.setText("Presenting");
    }

    @FXML
    // When user clicks About item in MenuBar, information about the program is displayed as a dialog.
    public void onAboutClicked(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("About");
        dialog.setContentText("Tabletop Presenter by Ryan Ong\n" +
                "Version 1.2\n" +
                "https://github.com/rhinotechie/Tabletop-Presenter.git");

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    @FXML
    // When user clicks About item in MenuBar, a user's guide about the program is displayed as a dialog.
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

    // Reloads the resource lists. Re-selects the items that were unselected.
    // Side-effect: Music restarts on load
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

        statusBar.setText("Music stopped");

        // Loads resources or makes empty directories if they don't exist.
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
            this.onPauseMusicClicked();
        }
    }

    // Plays currently selected music.
    private void playMusic(){
        String musicName = musicList.getSelectionModel().getSelectedItem();

        // Don't play music if nothing was selected.
        if (musicName == null){
            return;
        }

        clearMediaPlayer();

        try {
            Media media = new Media(new File("./Music/" + musicName).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
            mediaPlayer.play();

            // Updates media menu items
            startMusicItem.setDisable(false);
            pauseMusicItem.setDisable(false);
            resumeMusicItem.setDisable(true);
            stopMusicItem.setDisable(false);

            statusBar.setText("Music playing");
        } catch (IllegalArgumentException iae){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Music Load Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to load music from project resource folder.");
            alert.showAndWait();
        } catch (UnsupportedOperationException | MediaException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incompatible Music");
            alert.setHeaderText(null);
            alert.setContentText("Could not play selected music.");
            alert.showAndWait();
        }
    }

    // If music is playing, music gets paused.
    private void pauseMusic(){
        if (mediaPlayer == null){
            return;
        }

        switch(mediaPlayer.getStatus()){
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer();
                return;
            case PLAYING:
                mediaPlayer.pause();

                // Updates media menu items
                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                resumeMusicItem.setDisable(false);
                stopMusicItem.setDisable(false);

                statusBar.setText("Music paused");
                break;
        }
    }

    // If music is paused, the music continues to play or plays from beginning.
    private void resumeMusic(){
        if (mediaPlayer == null){
            return;
        }

        switch (mediaPlayer.getStatus()){
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer();
                return;
            case PAUSED:
                mediaPlayer.play();

                // Updates media menu items
                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(false);
                resumeMusicItem.setDisable(true);
                stopMusicItem.setDisable(false);

                statusBar.setText("Music playing");
                break;
        }
    }

    // If music is playing, the music gets stopped.
    private void stopMusic(){
        if (mediaPlayer == null){
            return;
        }

        switch (mediaPlayer.getStatus()){
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer();
                return;
            case PLAYING:
            case PAUSED:
            case STALLED:
                mediaPlayer.stop();
                clearMediaPlayer();
                break;
        }
    }

    // When the user clicks 'Start Music' in menu bar, music starts.
    @FXML
    public void onStartMusicClicked() {
        playMusic();
    }

    // When the user clicks 'Resume Music' in menu bar, music resumes.
    @FXML
    public void onResumeMusicClicked() {
        resumeMusic();
    }

    // When the user clicks 'Stop Music' in menu bar, music stops.
    @FXML
    public void onStopMusicClicked() {
        stopMusic();
    }

    // When the user clicks 'Pause Music' in menu bar, music pauses.
    @FXML
    public void onPauseMusicClicked(){
        pauseMusic();
    }

    // Resets media player and updates menu bar items to initial state
    // Do not set media player to null as the disposed status is useful for checking if resources are freed.
    public void clearMediaPlayer(){
        if (mediaPlayer != null){
            mediaPlayer.dispose();
        }

        // Updates media menu items
        startMusicItem.setDisable(false);
        pauseMusicItem.setDisable(true);
        resumeMusicItem.setDisable(true);
        stopMusicItem.setDisable(true);

        statusBar.setText("No music");
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
    // Displays file deletion dialog and deletes the target file from project directory.
    // Resource type should be capitalized and named as project folder
    public void onDeleteResource(String resourceType){
        if (Objects.equals(resourceType, "Music")){
            if (mediaPlayer == null) {
                deleteResource(resourceType);
            }

            switch (mediaPlayer.getStatus()) {
                case DISPOSED:
                    deleteResource(resourceType);
                    break;
                case PLAYING:
                case STALLED:
                case PAUSED:
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Turn off music");
                    alert.setHeaderText(null);
                    alert.setContentText("No music must be playing/paused before music deletion.");
                    alert.showAndWait();
                    break;
                case HALTED:
                case STOPPED:
                    Alert tryAgainAlert = new Alert(Alert.AlertType.INFORMATION);
                    tryAgainAlert.setTitle("Media player busy");
                    tryAgainAlert.setHeaderText(null);
                    tryAgainAlert.setContentText("Media player is busy. Try again in a few moments.");
                    tryAgainAlert.showAndWait();
                    clearMediaPlayer();
            }
        } else {
            deleteResource(resourceType);
        }
    }

    // Helper function for onDeleteResource
    private void deleteResource(String resourceType){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete resource");
        dialog.setContentText("Do you want to delete the resource in the project directory?");
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);

        // Asks user to confirm deletion.
        Optional<ButtonType> result = dialog.showAndWait();

        // Get the respective listView
        ListView<String> listView;
        switch(resourceType.toLowerCase()) {
            case "backgrounds":
            case "background":
                listView = backgroundList;
                break;
            case "foregrounds":
            case "foreground":
                listView = foregroundList;
                break;
            case "music":
                listView = musicList;
                break;
            case "scenes":
            case "scene":
                listView = sceneList;
                break;
            default:
                throw new RuntimeException("Resource type parameter invalid");
        }

        if (result.isPresent() && result.get() == ButtonType.YES){
            try {
                File file = new File("./" + resourceType + "/" + listView.getSelectionModel().getSelectedItem());
                if (file.delete()) {
                    switch(resourceType.toLowerCase()){
                        case "backgrounds":
                        case "background":
                            onBackgroundClearedClicked();
                            break;
                        case "foregrounds":
                        case "foreground":
                            onForegroundClearedClicked();
                            break;
                        case "music":
                            musicList.getSelectionModel().clearSelection();
                            break;
                        case "scenes":
                        case "scene":
                            sceneList.getSelectionModel().clearSelection();
                            break;
                    }
                    onRefreshResourcesClicked();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("File deletion error");
                    alert.setHeaderText(null);
                    alert.setContentText("Could not delete the file from the project directory.");
                    alert.showAndWait();
                }
            } catch (SecurityException securityException){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error");
                alert.setHeaderText(null);
                alert.setContentText("Could not delete the file from the project directory due to permissions." +
                        " Make sure your anti-virus isn't blocking file deletion.");
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error");
                alert.setHeaderText(null);
                alert.setContentText("Could not delete the file from the project directory.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void onExitClicked(){
        if (displayWindow != null){
            displayWindow.close();
        }
        Platform.exit();
    }
}