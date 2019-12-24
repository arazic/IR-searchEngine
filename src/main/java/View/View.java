package View;//package PACKAGE_NAME;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

/**
 * View class is part of mvs design pattern- this charge on the gui part
 */
public class View extends Observable {
    @FXML
    public javafx.scene.control.TextField txtfld_corpusPath;
    public javafx.scene.control.TextField txtfld_postingPath;
    public javafx.scene.control.CheckBox c_steeming;
    public javafx.scene.control.Button showDictionary;
    public javafx.scene.control.Button reset;
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
           // System.out.println(corpusFile.getAbsolutePath());
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
           // System.out.println(corpusFile.getAbsolutePath());
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
            if(c_steeming.isSelected()){
                isStemming=true;}
            else{
                isStemming=false;
            }
            setChanged();
            notifyObservers("loadDictionary");
        }
    }

    public void reset(){
        setChanged();
        notifyObservers("reset");
    }


    public void startEngine(){
        Boolean start= true;
        if(txtfld_corpusPath.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the corpus path is empty!\n");
            alert.showAndWait();
            start= false;
         }

        if(txtfld_postingPath.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the posting path is empty!\n");
            alert.showAndWait();
            start= false;
        }

        if(start) {
            if (c_steeming.isSelected()){
                isStemming = true;}
            else {
                isStemming=false;
            }

            reset.setDisable(false);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CONFIRMATION!");
            alert.setHeaderText("the engine start...\n");
            alert.showAndWait();

            setChanged();
            notifyObservers("startEngine");
        }
    }


    public void SetLoadDictionarySuccessfully(TreeMap<String, String> tm) {
        if(tm!=null){
            this.loadDictionarySuccessfully =true;
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CONFIRMATION!");
            alert.setHeaderText("The dictionary was successfully loaded!\n");
            alert.showAndWait();
            showDictionary.setDisable(false);
            termsDictionary= tm;
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("The dictionary was not loaded!\n\n");
            alert.showAndWait();
        }

    }

    public void show_dictionary() {

        indexerDictionary= new ListView<>();
        ObservableList<String> items= FXCollections.observableArrayList();

        for(Map.Entry<String,String> entry : termsDictionary.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String []s= value.split("!");
            items.add(key + ", "+ s[1]);
        }

        indexerDictionary.setItems(items);

            final VBox vbox = new VBox();
            vbox.setSpacing(5);
            vbox.setPadding(new Insets(10, 0, 0, 10));
            vbox.getChildren().addAll(indexerDictionary);

            Scene scene= new Scene(vbox,700, 450);
            Stage stage= new Stage();
            stage.setTitle("Dictionary");

            stage.setScene(scene);
            stage.show();
    }

    public void finishEngineMessage(int totalDocs, int terms, String totalTime) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION!");
        alert.setHeaderText("The engine finished!\nTotal documents:"+totalDocs+"\n"+
        "Unique terms in corpus: "+ terms+"\n"+ "Total time: "+ totalTime);
        alert.showAndWait();
    }

    public void SetResetSuccessfully() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMATION!");
        alert.setHeaderText("The engine reset succeeded!\n");
        alert.showAndWait();
        reset.setDisable(true);
    }
}
