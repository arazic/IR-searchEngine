package View;//package PACKAGE_NAME;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * View class is part of mvs design pattern- this charge on the gui part
 */
public class View extends Observable {
    @FXML
    public javafx.scene.control.TextField txtfld_corpusPath;
    public javafx.scene.control.TextField txtfld_postingPath;
    public javafx.scene.control.CheckBox c_steeming;
    public javafx.scene.control.CheckBox c_steeming_q;
    public javafx.scene.control.CheckBox c_semantic;
    public javafx.scene.control.Button showDictionary;
    public javafx.scene.control.Button reset;
    public javafx.scene.control.Button search;
    public javafx.scene.control.Button browserQueries;
    public javafx.scene.control.Button runTxtQueries;
    public javafx.scene.control.TextField txtfld_query;
    public javafx.scene.control.TextField txtfld_txtQueries;
    public javafx.scene.control.ListView indexerDictionary;

    public Accordion docs_ans;
    public boolean isStemming;
    public boolean isSemantic;
    public String loadDic;
    public String currentQuery;
    public String queriesPath;
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

    public void browser_queries(ActionEvent actionEvent) {
        FileChooser chooser= new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = chooser.showOpenDialog(null);

        if(selectedDirectory!=null){
            txtfld_txtQueries.clear();
            File corpusFile=  selectedDirectory.getAbsoluteFile();
            txtfld_txtQueries.appendText(corpusFile.getAbsolutePath());
        }
    }

    public void run_query(ActionEvent actionEvent) {

        if(txtfld_query.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("please insert query!\n");
            alert.showAndWait();
        }
        else
        {
            currentQuery= txtfld_query.getText();
            if(c_steeming.isSelected())
                isStemming=true;
            else
                isStemming=false;

            if(c_semantic.isSelected())
                isSemantic =true;
            else
                isSemantic =false;

            setChanged();
            notifyObservers("runQuery");
        }
    }

    public void run_txtQueries(ActionEvent actionEvent) {
        if(txtfld_txtQueries.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("please insert query!\n");
            alert.showAndWait();
        }
        else
        {
            queriesPath= txtfld_txtQueries.getText();
            if(c_steeming.isSelected())
                isStemming=true;
            else
                isStemming=false;

            if(c_semantic.isSelected())
                isSemantic =true;
            else
                isSemantic =false;

            setChanged();
            notifyObservers("runQueries");
        }

    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    public void notPreparedToSearch() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Be Attention");
        alert.setHeaderText("The dictionary was not loaded! Unable to run query!\n\n\n");
        alert.showAndWait();
    }



    public void showEngineAnswers(LinkedList<Pair<String,String>> currTop50) {
        docs_ans=null;
        TitledPane[] tps;
        Scene scene = search.getScene();
        Stage stage = (Stage) search.getScene().getWindow();
        if(currTop50==null)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("There are no document related to the query, check if the spelling is correct or try different query !\n\n\n");
            alert.showAndWait();
            return;
        }
        tps = new TitledPane[currTop50.size()];
        for (int i = 0; i < currTop50.size(); i++) {
            docs_ans = (Accordion) scene.lookup("#docAns");
            Button Bt = new Button("show entities from doc num "+(i+1)+" " + currTop50.get(i).getKey());
            Bt.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Top Entities");
                        String [] title= StringUtils.split(Bt.getText()," ");
                        String entities= currTop50.get(Integer.parseInt(title[5])-1).getValue();
                        if(entities!=null) {
                            entities = StringUtils.replace(entities, "|", " ,");
                            if(entities!="" && entities.charAt(0)==' '&& entities.charAt(1)==',')
                                entities= StringUtils.substring(entities,2);
                            entities = StringUtils.substring(entities, 0, entities.length() - 1);
                            alert.setHeaderText("Top Entities in " + title[6] + " :" + entities);
                            alert.showAndWait();
                        }
                    }
                });

            GridPane GP = new GridPane();
            GP.add(Bt, 1, 0);
            tps[i] = new TitledPane(currTop50.get(i).getKey(), GP);
        }
        if (tps.length > 0) {
            for (int i=0; i<50;i++){
                if(docs_ans.getPanes().size()!=0)
                  docs_ans.getPanes().remove(49-i);
            }
            docs_ans.getPanes().addAll(tps);
            docs_ans.setExpandedPane(tps[0]);
        }
        stage.setScene(scene);
        stage.show();
    }


}
