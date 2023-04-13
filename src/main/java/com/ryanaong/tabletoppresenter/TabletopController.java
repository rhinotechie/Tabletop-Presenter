package com.ryanaong.tabletoppresenter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import org.controlsfx.control.StatusBar;
import org.json.*;


// Controller class for the TableTopApplication
public class TabletopController implements Initializable {


    // USAGE: RESOURCE_DIRECTORY + your sub-path
    public static final String RESOURCE_DIRECTORY = "." + FileSystems.getDefault().getSeparator() + "Resources" +
            FileSystems.getDefault().getSeparator();

    public static final String systemSeparator = FileSystems.getDefault().getSeparator();

    // Resource types and their corresponding directory names.
    // USAGE: When specifying directory names for a URL, use the directoryName of ResourceType/SoundType enums
    // for consistency.
    enum ResourceType {
        SCENE ("Scenes"),
        BACKGROUND ("Backgrounds"),
        FOREGROUND ("Foregrounds"),
        MUSIC ("Music"),
        AMBIANCE ("Ambiances"),
        SOUND_EFFECT ("Sound Effects");

        private final String directoryName;

        ResourceType(String directoryName) {
            this.directoryName = directoryName;
        }
    }

    // Only the sound-related resources
    enum SoundType {
        MUSIC ("Music"),
        AMBIANCE ("Ambiances"),
        SOUND_EFFECT("Sound Effects");

        private final String directoryName;

        SoundType(String directoryName) {
            this.directoryName = directoryName;
        }
    }

    private boolean isFrozen = false;
    private Stage hostingStage;

    public void setHostingStage(Stage hostingStage) {
        this.hostingStage = hostingStage;
    }

    // Data
    @FXML
    private ListView<String> sceneList;
    @FXML
    private ListView<String> foregroundList;
    @FXML
    private ListView<String> backgroundList;
    @FXML
    private ListView<String> musicList;
    @FXML
    public ListView<String> ambianceList;
    @FXML
    public ListView<String> soundEffectList;

    // Canvas
    @FXML
    private Canvas previewCanvas;
    @FXML
    private ImageView backgroundImage;
    @FXML
    private ImageView foregroundImage;
    @FXML
    private Label frozenLabel;

    // Menus
    @FXML
    private MenuItem startMusicItem;
    @FXML
    private MenuItem pauseMusicItem;
    @FXML
    private MenuItem stopMusicItem;
    @FXML
    private MenuItem startAmbianceItem;
    @FXML
    private MenuItem resumeAmbianceItem;
    @FXML
    private MenuItem pauseAmbianceItem;
    @FXML
    private MenuItem stopAmbianceItem;
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private MenuItem startPresenterItem;
    @FXML
    private MenuItem endPresenterItem;
    @FXML
    private MenuItem freezePresenterItem;
    @FXML
    private MenuItem unfreezePresenterItem;
    @FXML
    private MenuItem resumeMusicItem;

    // Status bar

    @FXML
    private StatusBar statusBar;
    @FXML
    private Label musicLabel;
    @FXML
    private Slider musicSlider;
    @FXML
    private Label ambianceLabel;
    @FXML
    private Slider ambianceSlider;
    @FXML
    private Label soundEffectLabel;
    @FXML
    private Slider soundEffectSlider;
    @FXML
    private Label presenterStatus;

    // UI Controls for tweaking appearance of live stage.
    @FXML
    private CheckBox stretchBackgroundCheckBox;
    @FXML
    private Slider foregroundScaleSlider;
    @FXML
    private ColorPicker colorPicker;

    // Window (Stage) that's to be visible to the audience
    @FXML
    private Stage liveStage;
    @FXML
    private ImageView liveBackgroundImageView;
    @FXML
    private ImageView liveForegroundImageView;

    // Sound
    private MediaPlayer musicPlayer;
    private MediaPlayer ambiancePlayer;
    private MediaPlayer soundEffectPlayer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Ensures project directories are created if they aren't already
        onRefreshResourcesClicked();

        // Enables click behavior for item lists.
        {
            sceneList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            sceneList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // If left-clicked the scene is loaded
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        // Clears preview images, live images, and audio.
                        backgroundImage.setImage(null);
                        foregroundImage.setImage(null);
                        if (liveBackgroundImageView != null && !isFrozen) {
                            liveBackgroundImageView.setImage(null);
                        }
                        if (liveForegroundImageView != null && !isFrozen) {
                            liveForegroundImageView.setImage(null);
                        }

                        // Deselects all normal resource names to be reselected later
                        backgroundList.getSelectionModel().clearSelection();
                        foregroundList.getSelectionModel().clearSelection();
                        musicList.getSelectionModel().clearSelection();
                        ambianceList.getSelectionModel().clearSelection();
                        soundEffectList.getSelectionModel().clearSelection();

                        clearMediaPlayer(SoundType.MUSIC);
                        clearMediaPlayer(SoundType.AMBIANCE);
                        clearMediaPlayer(SoundType.SOUND_EFFECT);

                        // Updates background, foreground, and sounds from selected scene.

                        String sceneName = sceneList.getSelectionModel().getSelectedItem();
                        JSONObject jsonObject;
                        try (FileReader fileReader = new FileReader(RESOURCE_DIRECTORY + ResourceType.SCENE.directoryName + "\\" + sceneName);
                             Scanner scanner = new Scanner(fileReader)) {
                            StringBuilder sb = new StringBuilder();
                            while (scanner.hasNextLine()) {
                                sb.append(scanner.nextLine());
                            }

                            // Selects the scene items from the lists.
                            jsonObject = new JSONObject(sb.toString());
                            if (jsonObject.has("background")) {
                                backgroundList.getSelectionModel().select((String) jsonObject.get("background"));
                                loadResourceFromSelectedItem(ResourceType.BACKGROUND, backgroundList.getSelectionModel().getSelectedItem());
                            }
                            if (jsonObject.has("foreground")) {
                                foregroundList.getSelectionModel().select((String) jsonObject.get("foreground"));
                                loadResourceFromSelectedItem(ResourceType.FOREGROUND, foregroundList.getSelectionModel().getSelectedItem());
                            }
                            if (jsonObject.has("music")) {
                                musicList.getSelectionModel().select((String) jsonObject.get("music"));
                                loadResourceFromSelectedItem(ResourceType.MUSIC, musicList.getSelectionModel().getSelectedItem());
                            }
                            if (jsonObject.has("ambiance")) {
                                ambianceList.getSelectionModel().select((String) jsonObject.get("ambiance"));
                                loadResourceFromSelectedItem(ResourceType.AMBIANCE, ambianceList.getSelectionModel().getSelectedItem());
                            }
                        } catch (IOException e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Scene import error");
                            alert.setHeaderText(null);
                            alert.setContentText("Unable to load scene from project resource folder.");
                            alert.showAndWait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        onDeleteResource(ResourceType.SCENE);
                        mouseEvent.consume();
                    }
                }
            });

            backgroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            backgroundList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // If left-clicked, the background is displayed.
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        // Updates preview window with clicked resource
                        loadResourceFromSelectedItem(ResourceType.BACKGROUND, backgroundList.getSelectionModel().getSelectedItem());
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        onDeleteResource(ResourceType.BACKGROUND);
                    }
                }
            });

            foregroundList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            foregroundList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        loadResourceFromSelectedItem(ResourceType.FOREGROUND, foregroundList.getSelectionModel().getSelectedItem());
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        onDeleteResource(ResourceType.FOREGROUND);
                        mouseEvent.consume();
                    }
                }
            });

            musicList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            musicList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // If left-clicked, the music plays.
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        loadResourceFromSelectedItem(ResourceType.MUSIC, musicList.getSelectionModel().getSelectedItem());
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        if (musicPlayer == null || musicPlayer.getStatus() == MediaPlayer.Status.DISPOSED) {
                            onDeleteResource(ResourceType.MUSIC);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Unable to delete file");
                            alert.setHeaderText(null);
                            alert.setContentText("Sound file is currently being locked by program. " +
                                    "Please wait a few moments until the sound effect is released.");
                            alert.showAndWait();
                            if (musicPlayer != null){
                                musicPlayer.dispose();
                            }
                        }
                        mouseEvent.consume();
                    }

                }
            });

            ambianceList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            ambianceList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // Plays ambiance if left-clicked.
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY){
                        loadResourceFromSelectedItem(ResourceType.AMBIANCE, ambianceList.getSelectionModel().getSelectedItem());
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        if (ambiancePlayer == null || ambiancePlayer.getStatus() == MediaPlayer.Status.DISPOSED){
                            onDeleteResource(ResourceType.AMBIANCE);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Unable to delete file");
                            alert.setHeaderText(null);
                            alert.setContentText("Sound file is currently being locked by program. " +
                                    "Please wait a few moments until the sound effect is released.");
                            alert.showAndWait();
                            if (ambiancePlayer != null){
                                ambiancePlayer.dispose();
                            }
                        }
                        mouseEvent.consume();
                    }
                }
            });

            soundEffectList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            soundEffectList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                // Plays sound effect if left-clicked.
                // If an item is right-clicked, a dialog asks the user for deletion and deletes it if confirmed.
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton() == MouseButton.PRIMARY ){
                        loadResourceFromSelectedItem(ResourceType.SOUND_EFFECT, soundEffectList.getSelectionModel().getSelectedItem());
                        mouseEvent.consume();
                    } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                        if (soundEffectPlayer == null || soundEffectPlayer.getStatus() == MediaPlayer.Status.DISPOSED){
                            onDeleteResource(ResourceType.SOUND_EFFECT);
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Unable to delete file");
                            alert.setHeaderText(null);
                            alert.setContentText("Sound file is currently being locked by program. " +
                                    "Please wait a few moments until the sound effect is released.");
                            alert.showAndWait();

                            if (soundEffectPlayer != null){
                                soundEffectPlayer.dispose();
                            }
                        }
                        mouseEvent.consume();
                    }
                }
            });
        }

        // Behavior for tweaking presenter appearance.
        {
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

                        if (liveStage != null) {
                            liveStage.getScene().setFill(colorPicker.getValue());
                        }
                    }
                    actionEvent.consume();
                }
            });
        }

        // Audio controls
        musicSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (musicPlayer != null){
                    musicPlayer.setVolume((double) t1);
                }
            }
        });
        ambianceSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (ambiancePlayer != null){
                    ambiancePlayer.setVolume((double) t1);
                }
            }
        });
        soundEffectSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                if (soundEffectPlayer != null){
                    soundEffectPlayer.setVolume((double) t1);
                }
            }
        });
    }

    // Reloads the resource lists. Re-selects the items that were unselected.
    @FXML
    private void onRefreshResourcesClicked(){
        // Saves previous selections (if any)

        String prevScene = null;
        if (!sceneList.getSelectionModel().isEmpty()){
            prevScene = sceneList.getSelectionModel().getSelectedItem();
        }

        String prevBackground = null;
        if(!backgroundList.getSelectionModel().isEmpty()){
            prevBackground = backgroundList.getSelectionModel().getSelectedItem();
        }

        String prevForeGround = null;
        if(!foregroundList.getSelectionModel().isEmpty()){
            prevForeGround = foregroundList.getSelectionModel().getSelectedItem();
        }

        String prevMusic = null;
        if(!musicList.getSelectionModel().isEmpty()){
            prevMusic = musicList.getSelectionModel().getSelectedItem();
        }

        String prevAmbiance = null;
        if(!ambianceList.getSelectionModel().isEmpty()){
            prevAmbiance = ambianceList.getSelectionModel().getSelectedItem();
        }

        // Creates main resource folder
        File resourcesDir = new File(RESOURCE_DIRECTORY);
        try {
            //noinspection ResultOfMethodCallIgnored
            resourcesDir.mkdir();
        } catch (SecurityException securityException){
            securityException.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Program Folder Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to create a folder for project resources due to a security exception. " +
                    "Maybe you don't have permissions or anti-virus software is blocking this?");
            alert.showAndWait();
        }

        // Loads resources or makes empty directories if they don't exist.

        File foregroundDir = new File(RESOURCE_DIRECTORY + ResourceType.FOREGROUND.directoryName);
        if (!foregroundDir.mkdir()) {
            String[] foregrounds = foregroundDir.list();
            foregroundList.getItems().setAll(foregrounds);
        }

        File backgroundDir = new File(RESOURCE_DIRECTORY + ResourceType.BACKGROUND.directoryName);
        if (!backgroundDir.mkdir()) {
            String[] backgrounds = backgroundDir.list();
            backgroundList.getItems().setAll(backgrounds);
        }

        File sceneDir = new File(RESOURCE_DIRECTORY + ResourceType.SCENE.directoryName);
        if (!sceneDir.mkdir()) {
            String[] scenes = sceneDir.list();
            sceneList.getItems().setAll(scenes);
        }

        File musicDir = new File(RESOURCE_DIRECTORY + ResourceType.MUSIC.directoryName);
        if (!musicDir.mkdir()) {
            String[] music = musicDir.list();
            musicList.getItems().setAll(music);
        }

        File ambianceDir = new File(RESOURCE_DIRECTORY + ResourceType.AMBIANCE.directoryName);
        if (!ambianceDir.mkdir()) {
            String[] ambiances = ambianceDir.list();
            ambianceList.getItems().setAll(ambiances);
        }

        File soundEffectDir = new File(RESOURCE_DIRECTORY + ResourceType.SOUND_EFFECT.directoryName);
        if (!soundEffectDir.mkdir()) {
            String[] soundEffects = soundEffectDir.list();
            soundEffectList.getItems().setAll(soundEffects);
        }

        // Restores previous selections (if any)
        if (prevScene != null){
            sceneList.getSelectionModel().select(prevScene);
        }

        if (prevBackground != null){
            backgroundList.getSelectionModel().select(prevBackground);
        }

        if (prevForeGround != null){
            foregroundList.getSelectionModel().select(prevForeGround);
        }

        if (prevMusic != null){
            musicList.getSelectionModel().select(prevMusic);
        }

        if (prevAmbiance != null){
            ambianceList.getSelectionModel().select(prevAmbiance);
        }
    }

    @FXML
    // When the user selects the import background option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
    private void onLoadBackground(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import background");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(hostingStage);
            if (sourceFile == null) {
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get(RESOURCE_DIRECTORY + ResourceType.BACKGROUND.directoryName +
                    systemSeparator + sourceFile.getName());
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
    private void onLoadForeground(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import foreground");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(hostingStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get(RESOURCE_DIRECTORY + ResourceType.FOREGROUND.directoryName +
                    systemSeparator + sourceFile.getName());
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
    private void onLoadMusic(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import music");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(hostingStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get(RESOURCE_DIRECTORY + ResourceType.MUSIC.directoryName + systemSeparator + sourceFile.getName());
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

    // When the user selects the import ambiance option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
    @FXML
    private void onLoadAmbiance(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import ambiance");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(hostingStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get(RESOURCE_DIRECTORY + ResourceType.AMBIANCE.directoryName +
                    systemSeparator + sourceFile.getName());
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

    // When the user selects the import sound effect option in the MenuBar, a file chooser opens and the selected file
    // gets imported into the respective project resource folder.
    @FXML
    private void onLoadSoundEffect(){
        // FileChooser setup
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Sound Effect");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3")
        );

        // Displays chooser and copies file to resource folder.
        try {
            File sourceFile = fileChooser.showOpenDialog(hostingStage);
            if (sourceFile == null){
                return;
            }
            Path sourcePath = Path.of(sourceFile.getPath());
            Path destinationPath = Paths.get(RESOURCE_DIRECTORY + ResourceType.SOUND_EFFECT.directoryName +
                    systemSeparator + sourceFile.getName());
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
    private void onStartPresenter(){
        if (liveStage == null){
            // Stage configuration
            liveStage = new Stage();
            liveStage.initOwner(hostingStage);
            liveStage.setMinWidth(300);
            liveStage.setMinHeight(300);
            liveStage.initModality(Modality.NONE);
            liveStage.setTitle("Tabletop Presenter - Audience Display");
            liveStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    TabletopController.this.onEndPresenter();
                }
            });

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

            liveStage.setScene(displayScene);

            // Mirrors preview's background
            liveBackgroundImageView = new ImageView();
            liveBackgroundImageView.fitWidthProperty().bind(liveStage.widthProperty());
            liveBackgroundImageView.fitHeightProperty().bind(liveStage.heightProperty());
            if (backgroundImage.getImage() != null) {
                Image liveBackgroundImage = backgroundImage.getImage();
                liveBackgroundImageView.setImage(liveBackgroundImage);

                // Determine whether to stretch or keep image ratio.
                liveBackgroundImageView.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
            }

            stackPane.getChildren().add(liveBackgroundImageView);

            // Mirrors preview's foreground
            liveForegroundImageView = new ImageView();
            liveForegroundImageView.fitWidthProperty().bind(liveStage.widthProperty());
            liveForegroundImageView.fitHeightProperty().bind(liveStage.heightProperty());
            liveForegroundImageView.setPreserveRatio(true);
            if (foregroundImage.getImage() != null) {
                Image liveForegroundImage = foregroundImage.getImage();
                liveForegroundImageView.setImage(liveForegroundImage);
                liveForegroundImageView.setScaleX(foregroundScaleSlider.getValue());
                liveForegroundImageView.setScaleY(foregroundScaleSlider.getValue());
            }
            stackPane.getChildren().add(liveForegroundImageView);

            // Shows window after the mirrored images are loaded
            liveStage.show();
            
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
    // is enabled in the MenuBar, and the StatusBar is updated. Clears frozen status. Stops sounds.
    private void onEndPresenter(){
        if (liveStage != null){
            liveStage.close();
            liveStage = null;
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

        // Stops all sounds
        stopSound(SoundType.MUSIC);
        stopSound(SoundType.AMBIANCE);
        stopSound(SoundType.AMBIANCE);
    }

    @FXML
    // When the presenter window is open and the user selects Freeze Presentation in the MenuBar, the live window no
    // longer updates whenever the preview window is updated until it is unfrozen and a frozen status text is displayed.
    private void onFreezePresenter(){
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
    private void onUnFreezePresenter(){
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
    private void onAboutClicked(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("About");
        dialog.setContentText("Tabletop Presenter by Ryan Ong\n" +
                "Version 1.4\n" +
                "https://github.com/rhinotechie/Tabletop-Presenter.git");

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    @FXML
    // When user clicks About item in MenuBar, a user's guide about the program is displayed as a dialog.
    private void onUsersGuideClicked(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("User's Guide");
        dialog.setContentText(
                "Import background/foreground images and music/ambiance/audio by either using 'File > Import [Resource Type]'" +
                        "or copy resources directly to the program resource sub-directories.\n\n" +
                "Select up to one background, one foreground, one audio, and one ambiance for a scene.\n\n" +
                "Choose 'Presenter > Start Presentation' to open the presenter window for your secondary monitor.\n\n" +
                "Selecting a resource updates the live window unless you select 'Presenter > Freeze presentation'.\n\n" +
                "Save the preview screen's background, foreground and/or music as a scene with 'Scene > Save scene'");

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }

    // Clears preview and live background and unselects the resource from the list.
    @FXML
    private void onBackgroundClearedClicked(){
        backgroundImage.setImage(null);
        backgroundList.getSelectionModel().clearSelection();

        if (liveBackgroundImageView != null && !isFrozen){
            liveBackgroundImageView.setImage(null);
        }
    }

    // Clears preview and live foreground and unselects the resource from the list.
    @FXML
    private void onForegroundClearedClicked(){
        foregroundImage.setImage(null);
        foregroundList.getSelectionModel().clearSelection();

        if (liveForegroundImageView != null && !isFrozen){
            liveForegroundImageView.setImage(null);
        }
    }

    // Clears preview and live backgrounds, foregrounds, stops music, and unselects the resources from the lists.
    @FXML
    private void onAllClearedClicked(){
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
        ambianceList.getSelectionModel().clearSelection();
        soundEffectList.getSelectionModel().clearSelection();

        clearMediaPlayer(SoundType.MUSIC);
        clearMediaPlayer(SoundType.AMBIANCE);
        clearMediaPlayer(SoundType.SOUND_EFFECT);
    }

    // When the user clicks 'Pause Music' in menu bar, music pauses.
    @FXML
    private void onPauseMusicClicked(){
        pauseSound(SoundType.MUSIC);
    }

    @FXML
    private void onSaveSceneClicked(){
        // Alert the user that nothing is selected so the scene is not saved.
        if (backgroundList.getSelectionModel().isEmpty() && foregroundList.getSelectionModel().isEmpty() &&
                musicList.getSelectionModel().isEmpty() && ambianceList.getSelectionModel().isEmpty()){
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
        if (!ambianceList.getSelectionModel().isEmpty())
        {
            jsonMap.put("ambiance", ambianceList.getSelectionModel().getSelectedItem());
        }
        JSONObject jsonObject = new JSONObject(jsonMap);

        // Prompts user for filename.
        String sceneFileName = this.showNewSceneDialog();
        if (sceneFileName == null || sceneFileName.isEmpty()){
            return;
        }

        // Creates a new file and writes the json contents to it.
        try {
            File newFile = new File(RESOURCE_DIRECTORY + ResourceType.SCENE.directoryName + systemSeparator +
                    sceneFileName + ".json");
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
        
        // Refreshes directories so new Scene can be seen
        onRefreshResourcesClicked();
    }

    // Displays dialog for naming a new scene.
    @FXML
    private String showNewSceneDialog(){
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
    private void onDeleteResource(ResourceType resourceType){
        switch (resourceType){
            case MUSIC:
                freePlayerBeforeDeletion(SoundType.MUSIC);
                break;
            case AMBIANCE:
                freePlayerBeforeDeletion(SoundType.AMBIANCE);
                break;
            case SOUND_EFFECT:
                freePlayerBeforeDeletion(SoundType.SOUND_EFFECT);
                break;
            case SCENE:
            case BACKGROUND:
            case FOREGROUND:
                deleteResource(resourceType);
                break;
            default:
        }
    }

    // Helper function for onDeleteResource
    // Ensures deletion only occurs if player is in a proper state.
    private void freePlayerBeforeDeletion(SoundType soundType){
        MediaPlayer mediaPlayer;
        switch(soundType){
            case MUSIC:
                mediaPlayer = musicPlayer;
                break;
            case AMBIANCE:
                mediaPlayer = ambiancePlayer;
                break;
            case SOUND_EFFECT:
                mediaPlayer = soundEffectPlayer;
                break;
            default:
                return;
        }

        // There's no media player to free up hence it's safe to delete the resource.
        if (mediaPlayer == null){
            if (soundType == SoundType.MUSIC) {
                deleteResource(ResourceType.MUSIC);
            } else if (soundType == SoundType.AMBIANCE) {
                deleteResource(ResourceType.AMBIANCE);
            } else {
                deleteResource(ResourceType.SOUND_EFFECT);
            }
            return;
        }

        switch (mediaPlayer.getStatus()) {
            case DISPOSED:
                // Ideal state. Ready for deletion.
                if (soundType == SoundType.MUSIC) {
                    deleteResource(ResourceType.MUSIC);
                } else if (soundType == SoundType.AMBIANCE) {
                    deleteResource(ResourceType.AMBIANCE);
                } else {
                    deleteResource(ResourceType.SOUND_EFFECT);
                }
                break;
            case PLAYING:
            case STALLED:
            case PAUSED:
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Turn off music");
                alert.setHeaderText(null);
                alert.setContentText("No audio must be playing/paused before audio deletion.");
                alert.showAndWait();
                break;
            case HALTED:
            case STOPPED:
                Alert tryAgainAlert = new Alert(Alert.AlertType.INFORMATION);
                tryAgainAlert.setTitle("Media player busy");
                tryAgainAlert.setHeaderText(null);
                tryAgainAlert.setContentText("Media player is busy. Try again in a few moments.");
                tryAgainAlert.showAndWait();

                // Disposes the player so the user can try again and delete the file.
                clearMediaPlayer(soundType);
        }
    }

    // Helper function for onDeleteResource
    // Resource is ready to be deleted
    private void deleteResource(ResourceType resourceType){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete resource");
        dialog.setContentText("Do you want to delete the resource from the project directory?");
        dialog.initOwner(mainBorderPane.getScene().getWindow());

        // Adds buttons to the dialog pane (since buttons can't be in the fxml file).
        dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);

        // Asks user to confirm deletion.
        Optional<ButtonType> result = dialog.showAndWait();

        // Get the respective listView
        ListView<String> listView;
        switch(resourceType) {
            case SCENE:
                listView = sceneList;
                break;
            case BACKGROUND:
                listView = backgroundList;
                break;
            case FOREGROUND:
                listView = foregroundList;
                break;
            case MUSIC:
                listView = musicList;
                break;
            case AMBIANCE:
                listView = ambianceList;
                break;
            case SOUND_EFFECT:
                listView = soundEffectList;
                break;
            default:
                return;
        }

        if (result.isPresent() && result.get() == ButtonType.YES){
            try {
                Files.delete(Path.of(RESOURCE_DIRECTORY + resourceType.directoryName + systemSeparator +
                        listView.getSelectionModel().getSelectedItem()));
            } catch (NoSuchFileException noSuchFileException) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error");
                alert.setHeaderText(null);
                alert.setContentText("File does not exist.");
                alert.showAndWait();
                noSuchFileException.printStackTrace();
                return;
            } catch (IOException ioException){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error (I/O)");
                alert.setHeaderText(null);
                alert.setContentText("Unable to delete file due to file I/O related error. Remove from resource" +
                        " directory.");
                alert.showAndWait();
                ioException.printStackTrace();
                return;
            } catch (SecurityException securityException) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error");
                alert.setHeaderText(null);
                alert.setContentText("Unable to delete file due to permissions." +
                        " Make sure your OS or anti-virus aren't blocking file deletion.");
                alert.showAndWait();
                return;
            } catch (Exception exception){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("File deletion error");
                alert.setHeaderText(null);
                alert.setContentText("Unable to delete file. Remove from resource directory.");
                alert.showAndWait();
                exception.printStackTrace();
                return;
            }

            switch(resourceType){
                case BACKGROUND:
                    onBackgroundClearedClicked();
                    break;
                case FOREGROUND:
                    onForegroundClearedClicked();
                    break;
                case MUSIC:
                    musicList.getSelectionModel().clearSelection();
                    break;
                case AMBIANCE:
                    ambianceList.getSelectionModel().clearSelection();
                    break;
                case SOUND_EFFECT:
                    soundEffectList.getSelectionModel().clearSelection();
                    break;
                case SCENE:
                    sceneList.getSelectionModel().clearSelection();
                    break;
            }

            // Refreshes directories so deleted item doesn't appear.
            onRefreshResourcesClicked();
        }
    }

    @FXML
    // Toggle stretch/non-stretched backgrounds for preview and live windows.
    private void onStretchClicked(){
        backgroundImage.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
        if (liveBackgroundImageView != null && !isFrozen){
            liveBackgroundImageView.setPreserveRatio(!stretchBackgroundCheckBox.isSelected());
        }
    }

    @FXML
    // Changes scale of foreground image according to slider
    private void onRatioSliderChanged(){
        if (foregroundImage != null){
            foregroundImage.setScaleX(foregroundScaleSlider.getValue());
            foregroundImage.setScaleY(foregroundScaleSlider.getValue());
        }
        if (liveForegroundImageView != null && !isFrozen){
            liveForegroundImageView.setScaleX(foregroundScaleSlider.getValue());
            liveForegroundImageView.setScaleY(foregroundScaleSlider.getValue());
        }
    }

    // Closes application when menu Exit option is clicked.
    @FXML
    private void onExitClicked(){
        Platform.exit();
    }

    // When the user clicks 'Start Music' in menu bar, music starts.
    @FXML
    private void onStartMusicClicked() {
        playSound(SoundType.MUSIC);
    }

    // When the user clicks 'Resume Music' in menu bar, music resumes.
    @FXML
    private void onResumeMusicClicked() {
        resumeSound(SoundType.MUSIC);
    }

    // When the user clicks 'Stop Music' in menu bar, music stops.
    @FXML
    private void onStopMusicClicked() {
        stopSound(SoundType.MUSIC);
    }

    @FXML
    private void onStartAmbianceClicked() {
        playSound(SoundType.AMBIANCE);
    }

    @FXML
    private void onResumeAmbianceClicked() {
        resumeSound(SoundType.AMBIANCE);
    }

    @FXML
    private void onPauseAmbianceClicked() {
        pauseSound(SoundType.AMBIANCE);
    }

    @FXML
    private void onStopAmbianceClicked(ActionEvent actionEvent) {
        stopSound(SoundType.AMBIANCE);
    }

    // Creates a new instance of specified media and media player and plays it.
    private void playSound(SoundType soundType){
        try {
            String soundName;
            switch (soundType){
                case MUSIC:
                    clearMediaPlayer(SoundType.MUSIC);

                    if (!musicList.getSelectionModel().isEmpty()){
                        soundName = musicList.getSelectionModel().getSelectedItem();
                        if (soundName == null) return;
                        Media media = new Media(new File(RESOURCE_DIRECTORY + soundType.directoryName +
                                systemSeparator + soundName).toURI().toString());
                        musicPlayer = new MediaPlayer(media);
                        musicPlayer.setAutoPlay(false);
                        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        musicPlayer.setVolume(musicSlider.getValue());
                        musicPlayer.play();

                        // Updates media menu items
                        startMusicItem.setDisable(true);
                        pauseMusicItem.setDisable(false);
                        resumeMusicItem.setDisable(true);
                        stopMusicItem.setDisable(false);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("No Music Selected");
                        alert.setHeaderText(null);
                        alert.setContentText("Select music from the left panel before playing.");
                        alert.showAndWait();
                        return;
                    }
                    break;
                case AMBIANCE:
                    clearMediaPlayer(SoundType.AMBIANCE);

                    if (!ambianceList.getSelectionModel().isEmpty()){
                        soundName = ambianceList.getSelectionModel().getSelectedItem();
                        if (soundName == null) return;
                        Media media = new Media(new File(RESOURCE_DIRECTORY + soundType.directoryName +
                                systemSeparator + soundName).toURI().toString());
                        ambiancePlayer = new MediaPlayer(media);
                        ambiancePlayer.setAutoPlay(false);
                        ambiancePlayer.setCycleCount(MediaPlayer.INDEFINITE);
                        ambiancePlayer.setVolume(ambianceSlider.getValue());
                        ambiancePlayer.play();

                        // Updates media menu items
                        startAmbianceItem.setDisable(true);
                        pauseAmbianceItem.setDisable(false);
                        resumeAmbianceItem.setDisable(true);
                        stopAmbianceItem.setDisable(false);
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("No Ambiance Selected");
                        alert.setHeaderText(null);
                        alert.setContentText("Select ambiance from the left panel before playing.");
                        alert.showAndWait();
                        return;
                    }
                    break;
                case SOUND_EFFECT:
                    clearMediaPlayer(SoundType.SOUND_EFFECT);

                    if (!soundEffectList.getSelectionModel().isEmpty()){
                        soundName = soundEffectList.getSelectionModel().getSelectedItem();
                        if (soundName == null) return;
                        Media media = new Media(new File(RESOURCE_DIRECTORY + soundType.directoryName +
                                systemSeparator + soundName).toURI().toString());
                        soundEffectPlayer = new MediaPlayer(media);
                        soundEffectPlayer.setCycleCount(0);
                        soundEffectPlayer.setAutoPlay(false);
                        soundEffectPlayer.setVolume(soundEffectSlider.getValue());
                        soundEffectPlayer.play();
                    } else {
                        return;
                    }
                    break;
                default:
            }
        } catch (IllegalArgumentException iae){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Sound Load Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to load sound from project resource folder.");
            alert.showAndWait();
        } catch (UnsupportedOperationException | MediaException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Incompatible Sound");
            alert.setHeaderText(null);
            alert.setContentText("Could not play selected sound.");
            alert.showAndWait();
        }
    }

    // If sound is playing, the sound gets paused.
    private void pauseSound(SoundType soundType){
        MediaPlayer mediaPlayer;
        switch(soundType){
            case MUSIC:
                mediaPlayer = musicPlayer;
                break;
            case AMBIANCE:
                mediaPlayer = ambiancePlayer;
                break;
            default:
                return; // Don't pause sound effects
        }

        if (mediaPlayer == null){
            // Resets media menu items
            if (soundType == SoundType.MUSIC){
                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                resumeMusicItem.setDisable(true);
                stopMusicItem.setDisable(true);
            } else {
                startAmbianceItem.setDisable(false);
                pauseAmbianceItem.setDisable(true);
                resumeAmbianceItem.setDisable(true);
                stopAmbianceItem.setDisable(true);
            }
            return;
        }

        switch(mediaPlayer.getStatus()){
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer(soundType);
                return;
            case PLAYING:
                mediaPlayer.pause();

                // Updates media menu items
                if (soundType == SoundType.MUSIC){
                    startMusicItem.setDisable(false);
                    pauseMusicItem.setDisable(true);
                    resumeMusicItem.setDisable(false);
                    stopMusicItem.setDisable(false);
                } else {
                    startAmbianceItem.setDisable(false);
                    pauseAmbianceItem.setDisable(true);
                    resumeAmbianceItem.setDisable(false);
                    stopAmbianceItem.setDisable(false);
                }
                
                break;
        }
    }

    // If music is paused, the music continues to play or plays from beginning.
    private void resumeSound(SoundType soundType){
        MediaPlayer mediaPlayer;
        switch(soundType){
            case MUSIC:
                mediaPlayer = musicPlayer;
                break;
            case AMBIANCE:
                mediaPlayer = ambiancePlayer;
                break;
            default:
                return; // Don't resume sound effects
        }

        if (mediaPlayer == null) {
            // Resets proper menu items
            if (soundType == SoundType.MUSIC) {
                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                resumeMusicItem.setDisable(true);
                stopMusicItem.setDisable(true);
            } else {
                startAmbianceItem.setDisable(false);
                pauseAmbianceItem.setDisable(true);
                resumeAmbianceItem.setDisable(true);
                stopAmbianceItem.setDisable(true);
            }
            return;
        }

        switch (mediaPlayer.getStatus()) {
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer(soundType);
                return;
            case PAUSED:
                mediaPlayer.play();

                // Updates proper menu items
                if (soundType == SoundType.MUSIC) {
                    startMusicItem.setDisable(false);
                    pauseMusicItem.setDisable(false);
                    resumeMusicItem.setDisable(true);
                    stopMusicItem.setDisable(false);
                } else {
                    startAmbianceItem.setDisable(false);
                    pauseAmbianceItem.setDisable(false);
                    resumeAmbianceItem.setDisable(true);
                    stopAmbianceItem.setDisable(false);
                }

                break;
        }
    }

    // If music is playing, the music gets stopped.
    private void stopSound(SoundType soundType){
        MediaPlayer mediaPlayer;
        switch (soundType){
            case MUSIC:
                mediaPlayer = musicPlayer;
                break;
            case AMBIANCE:
                mediaPlayer = ambiancePlayer;
                break;
            default:
                return; // Don't stop sound effects
        }

        if (mediaPlayer == null){
            // Resets proper menu items
            if (soundType == SoundType.MUSIC) {
                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                resumeMusicItem.setDisable(true);
                stopMusicItem.setDisable(true);
            } else {
                startAmbianceItem.setDisable(false);
                pauseAmbianceItem.setDisable(true);
                resumeAmbianceItem.setDisable(true);
                stopAmbianceItem.setDisable(true);
            }
            return;
        }

        switch (mediaPlayer.getStatus()){
            case HALTED:
            case DISPOSED:
                // Current media player is unusable.
                clearMediaPlayer(soundType);
                return;
            case PLAYING:
            case PAUSED:
            case STALLED:
                mediaPlayer.stop();

                // Updates proper menu items
                if (soundType == SoundType.MUSIC) {
                    startMusicItem.setDisable(false);
                    pauseMusicItem.setDisable(true);
                    resumeMusicItem.setDisable(true);
                    stopMusicItem.setDisable(true);
                } else {
                    startAmbianceItem.setDisable(false);
                    pauseAmbianceItem.setDisable(true);
                    resumeAmbianceItem.setDisable(true);
                    stopAmbianceItem.setDisable(true);
                }

                //clearMediaPlayer(soundType);
                break;
        }
    }

    // Disposes media player and updates menu bar items to initial state
    private void clearMediaPlayer(SoundType soundType){
        switch(soundType){
            case MUSIC:
                if (musicPlayer != null){
                    musicPlayer.dispose();
                    musicPlayer = null;
                }

                startMusicItem.setDisable(false);
                pauseMusicItem.setDisable(true);
                resumeMusicItem.setDisable(true);
                stopMusicItem.setDisable(true);

                break;
            case AMBIANCE:
                if (ambiancePlayer != null){
                    ambiancePlayer.dispose();
                    ambiancePlayer = null;
                }

                startAmbianceItem.setDisable(false);
                pauseAmbianceItem.setDisable(true);
                resumeAmbianceItem.setDisable(true);
                stopAmbianceItem.setDisable(true);

                break;
            case SOUND_EFFECT:
                if (soundEffectPlayer != null){
                    soundEffectPlayer.dispose();
                    soundEffectPlayer = null;
                }
                break;
            default:
        }
    }

    // Displays the selected resource that's selected from the corresponding list.
    private void loadResourceFromSelectedItem(ResourceType resourceType, String selectedItemName){
        // Do nothing since nothing is selected
        if (selectedItemName == null) {
            return;
        }

        Image image;
        try (FileInputStream fileInputStream = new FileInputStream(RESOURCE_DIRECTORY +
                resourceType.directoryName + systemSeparator + selectedItemName)) {
            switch(resourceType){
                case BACKGROUND:
                    image = new Image(fileInputStream);
                    backgroundImage.setImage(image);
                    if (liveBackgroundImageView != null && !isFrozen) {
                        liveBackgroundImageView.setImage(image);
                    }
                    break;
                case FOREGROUND:
                    image = new Image(fileInputStream);
                    foregroundImage.setImage(image);
                    if (liveForegroundImageView != null && !isFrozen) {
                        liveForegroundImageView.setImage(image);
                    }
                    break;
                case MUSIC:
                    playSound(SoundType.MUSIC);
                    break;
                case AMBIANCE:
                    playSound(SoundType.AMBIANCE);
                    break;
                case SOUND_EFFECT:
                    playSound(SoundType.SOUND_EFFECT);
                    break;
                default:
            }

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Resource Load Error");
            alert.setHeaderText(null);
            alert.setContentText("Unable to load resource from project resource folder.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}