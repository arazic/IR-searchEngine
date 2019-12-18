package View;//package PACKAGE_NAME;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.File;
import java.util.Observable;


public class View extends Observable {
    @FXML
    public javafx.scene.control.TextField txtfld_corpusPath;
    public javafx.scene.control.TextField txtfld_postingPath;
    public javafx.scene.control.CheckBox c_steeming;
    public boolean isSteeming;
    public String loadDic;

    public void browser_corsus(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = chooser.showDialog(null);

        if(selectedDirectory!=null){
            txtfld_corpusPath.clear();
            File corpusFile=  selectedDirectory.getAbsoluteFile();
            txtfld_corpusPath.appendText(corpusFile.getAbsolutePath());
            System.out.println(corpusFile.getAbsolutePath());
        }


    }
    public void browser_posting(){
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = chooser.showDialog(null);

        if(selectedDirectory!=null){
            txtfld_postingPath.clear();
            File corpusFile=  selectedDirectory.getAbsoluteFile();
            txtfld_postingPath.appendText(corpusFile.getAbsolutePath());
            System.out.println(corpusFile.getAbsolutePath());
        }
    }

    public void load_dictionary(){

        if(txtfld_postingPath.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the posting path is empty!\n");
            alert.showAndWait();
        }
        else{
            loadDic = txtfld_postingPath.getText();
            System.out.println(loadDic);
            if(c_steeming.isSelected())
                isSteeming=true;
            setChanged();
            notifyObservers("loadDictionary");
        }
    }

    public void show_dictionary(){

    }

    public void resetAll(){

    }



    public void startEngine(){
        if(txtfld_corpusPath.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the corpus path is empty!\n");
            alert.showAndWait();
         }
        if(txtfld_postingPath.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the posting path is empty!\n");
            alert.showAndWait();
        }

        if(c_steeming.isSelected())
            isSteeming=true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION!");
        alert.setHeaderText("the engine start...\n");
        alert.showAndWait();

        setChanged();
        notifyObservers("startEngine");
    }


}
