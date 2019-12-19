package View;//package PACKAGE_NAME;

import Controller.Controller;
import Model.Model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;


public class View extends Observable {
    @FXML
    public javafx.scene.control.TextField txtfld_corpusPath;
    public javafx.scene.control.TextField txtfld_postingPath;
    public javafx.scene.control.CheckBox c_steeming;
    public javafx.scene.control.Button showDictionary;
    public javafx.scene.control.ListView indexerDictionary;
    public boolean isStemming;
    public String loadDic;
    public boolean loadDictionarySuccessfully;
    public TreeMap<String,String> termsDictionary;


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
                isStemming=true;
            setChanged();
            notifyObservers("loadDictionary");
        }
    }

    public void reset(){
        setChanged();
        notifyObservers("reset");
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
            isStemming=true;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION!");
        alert.setHeaderText("the engine start...\n");
        alert.showAndWait();

        setChanged();
        notifyObservers("startEngine");
    }


    public void SetLoadDictionarySuccessfully(boolean ans) {
        this.loadDictionarySuccessfully =ans;
        if(ans){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CONFIRMATION!");
            alert.setHeaderText("The dictionary was successfully loaded!\n");
            alert.showAndWait();
            showDictionary.setDisable(false);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("The dictionary was not loaded!\n\n");
            alert.showAndWait();
        }

    }

    public void show_dictionary() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root = fxmlLoader.load(getClass().getResource("resources/indexerDictionary.fxml").openStream());
            Scene scene = new Scene(root, 650, 600);
            Stage showStage= new Stage();
            showStage.setTitle("Dictionary");
            scene.getStylesheets().add(getClass().getResource("resources/MenuStyle.css").toExternalForm());
            showStage.setScene(scene);
            indexerDictionary= (javafx.scene.control.ListView) scene.lookup("#indexerDictionary");
            ObservableList<String> items= FXCollections.observableArrayList();

            for(Map.Entry<String,String> entry : termsDictionary.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                items.add(key + value);
            }

            indexerDictionary.setItems(items);
            showStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setDictionary(TreeMap<String,String> terms) {
        this.termsDictionary=terms;
    }
}
