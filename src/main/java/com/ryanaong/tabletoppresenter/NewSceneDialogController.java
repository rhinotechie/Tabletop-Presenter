package com.ryanaong.tabletoppresenter;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class NewSceneDialogController {
    @FXML
    public TextField sceneTextField;

    public String getSceneName(){
        return sceneTextField.getText().trim();
    }
}
