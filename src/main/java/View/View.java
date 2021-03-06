package View;//package PACKAGE_NAME;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
    public javafx.scene.control.Button showAllEntities;
    public javafx.scene.control.Button browserQueriesResult;
    public javafx.scene.control.TextField txtfld_query;
    public javafx.scene.control.TextField txtfld_txtQueries;
    public javafx.scene.control.TextField txtfld_txtQueriesResult;
    public javafx.scene.control.ChoiceBox cb_queryNum;
    public javafx.scene.control.Label queryNumLabel;
    public javafx.scene.control.ListView indexerDictionary;

    public Accordion docs_ans;
    public boolean isStemming;
    public boolean isSemantic;
    public String loadDicPosting;
    public String loadDicCorpus;
    public String currentQuery;
    public String queriesPath;
    public String queriesResultPath;
    public boolean loadDictionarySuccessfully;
    public TreeMap<String,String> termsDictionary;
    public HashMap<Integer, LinkedList<Pair<String, String>>> dataQueriesTop50;


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
        if(txtfld_corpusPath.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("the corpus path is empty!\n");
            alert.showAndWait();
        }
        else{
            loadDicPosting = txtfld_postingPath.getText();
            loadDicCorpus= txtfld_corpusPath.getText();
            //System.out.println(loadDicPosting);
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
            this.isStemming=c_steeming.isSelected();
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
            if(txtfld_txtQueriesResult.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Be Attention");
                alert.setHeaderText("Please enter a path to save the results!\n");
                alert.showAndWait();
            }
            else {
                queriesResultPath= txtfld_txtQueriesResult.getText();
                currentQuery = txtfld_query.getText();
                if (c_steeming.isSelected())
                    isStemming = true;
                else
                    isStemming = false;

                if (c_semantic.isSelected())
                    isSemantic = true;
                else
                    isSemantic = false;

                setChanged();
                notifyObservers("runQuery");
            }
        }
    }

    public void run_txtQueries(ActionEvent actionEvent) {
        if (txtfld_txtQueries.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("please insert query!\n");
            alert.showAndWait();
        } else {
            if (txtfld_txtQueriesResult.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Be Attention");
                alert.setHeaderText("Please enter a path to save the results!\n");
                alert.showAndWait();
            } else {
                queriesResultPath = txtfld_txtQueriesResult.getText();
                queriesPath = txtfld_txtQueries.getText();
                if (c_steeming.isSelected())
                    isStemming = true;
                else
                    isStemming = false;

                if (c_semantic.isSelected())
                    isSemantic = true;
                else
                    isSemantic = false;

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Congratulation!");
                alert.setHeaderText("The engine started working on your request ...\n\n\n");
                alert.showAndWait();

                setChanged();
                notifyObservers("runQueries");
            }

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
        ChoiceBox CB= (ChoiceBox) scene.lookup("#cb_queryNum");
        CB.setItems(FXCollections.observableArrayList(
                "query number 1")
        );
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
                       //     entities = StringUtils.replace(entities, "|", " ,");
                            entities = StringUtils.substring(entities, 0, entities.length() );
                            entities=cutFive(entities);
                            if(entities!="" && entities.charAt(0)==' '&& entities.charAt(1)==',')
                                entities= StringUtils.substring(entities,2);
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

    private String cutFive(String entities) {
        String []all= StringUtils.split(entities,"|");
        String ans= "";
        if(all.length>=5) {
            for (int i = 0; i < 5; i++) {
                if(i==4) {
                    ans = ans + all[i] ;
                }
                else{
                    ans = ans + all[i] + " ,";
                }
            }
            return ans;
        }
        else{
            for (int i = 0; i < all.length; i++) {
                if(i==all.length-1) {
                    ans = ans + all[i] ;
                }
                else{
                    ans = ans + all[i] + " ,";
                }
            }
            return ans;
        }
    }


    public void browser_queriesResults(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = chooser.showDialog(null);

        if(selectedDirectory!=null){
            txtfld_txtQueriesResult.clear();
            File corpusFile=  selectedDirectory.getAbsoluteFile();
            txtfld_txtQueriesResult.appendText(corpusFile.getAbsolutePath());
        }

    }

    public void alertFinishAndCreateAnswerDoc(HashMap<Integer, LinkedList<Pair<String, String>>> allQueriesTop50) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Congratulation!");
        alert.setHeaderText("Answer file was created!!\n\n\n");
        alert.showAndWait();

        if(allQueriesTop50==null){
            return;
        }
        dataQueriesTop50 = allQueriesTop50;
        docs_ans=null;
        TitledPane[] tps;
        Scene scene = search.getScene();
        Stage stage = (Stage) search.getScene().getWindow();
        ChoiceBox CB= (ChoiceBox) scene.lookup("#cb_queryNum");

        CB.setItems(FXCollections.observableArrayList(
                allQueriesTop50.keySet())
        );
        showAllEntities.setVisible(true);
        queryNumLabel.setVisible(true);
        cb_queryNum.setVisible(true);
        stage.setScene(scene);
        stage.show();


    }

    public void showEntities(ActionEvent actionEvent) {

        if(cb_queryNum.getValue().equals(""))
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Be Attention");
            alert.setHeaderText("Choose a query you want to see its results\n!\n\n\n");
            alert.showAndWait();
            return;
        }

        if(cb_queryNum.getValue().toString().equals("query number 1"))
            return;

        int query= Integer.parseInt(cb_queryNum.getValue().toString());
        docs_ans=null;
        TitledPane[] tps;
        Scene scene = search.getScene();
        Stage stage = (Stage) search.getScene().getWindow();


        tps = new TitledPane[dataQueriesTop50.get(query).size()];
        for (int i = 0; i < dataQueriesTop50.get(query).size(); i++) {
            docs_ans = (Accordion) scene.lookup("#docAns");
            Button Bt = new Button("show entities from doc num "+(i+1)+" " + dataQueriesTop50.get(query).get(i).getKey());
            Bt.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Top Entities");
                    String [] title= StringUtils.split(Bt.getText()," ");
                    String entities= dataQueriesTop50.get(query).get(Integer.parseInt(title[5])-1).getValue();
                    if(entities!=null) {
                        //     entities = StringUtils.replace(entities, "|", " ,");
                        entities = StringUtils.substring(entities, 0, entities.length() - 1);
                        entities=cutFive(entities);
                        if(entities!="" && entities.charAt(0)==' '&& entities.charAt(1)==',')
                            entities= StringUtils.substring(entities,2);
                        alert.setHeaderText("Top Entities in " + title[6] + " :" + entities);
                        alert.showAndWait();
                    }
                }
            });

            GridPane GP = new GridPane();
            GP.add(Bt, 1, 0);
            tps[i] = new TitledPane(dataQueriesTop50.get(query).get(i).getKey(), GP);
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
