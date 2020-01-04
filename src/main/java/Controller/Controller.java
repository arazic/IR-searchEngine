package Controller;

import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import Model.Model;
import View.View;


/**
 * Controller class is part of mvs design pattern- this charge on the connection between the view and the controller
 */
public class Controller extends Observable implements Observer {
    private Model model;
    private View view;


    @Override
    public void update(Observable o, Object arg) {
        if (o == view){
            switch((String) arg){
                case "startEngine":
                    model.startEngine(view.txtfld_corpusPath.getText(),view.txtfld_postingPath.getText(),view.isStemming);
                    break;
                case "loadDictionary":
                    TreeMap<String,String> tm=model.loadDictionary(view.loadDic,view.isStemming);
                    if(tm!=null) {
                        view.SetLoadDictionarySuccessfully(tm);
                    }
                    else
                        view.SetLoadDictionarySuccessfully(null);
                    break;
                case "showDictionary":
                    model.showDictionary();
                    break;
                case "reset":
                    model.reset();
                    break;
                case "runQuery":
                    model.runQuery(view.getCurrentQuery(), view.isStemming, view.isSemantic);
                    break;
                case "runQueries":
                    model.runQueriesFile(view.queriesPath, view.isStemming, view.isSemantic);
                    break;

            }
        }
        else if( o == model){
            switch((String) arg) {

                case "finishEngine":
                    view.finishEngineMessage(((Model)o).totalDocNum,((Model)o).getTerms().size(),((Model)o).totalTime);
                    break;
                case "resetSucceed":
                    view.SetResetSuccessfully();
                    break;
                case "notPreparedToSearch":
                    view.notPreparedToSearch();
                    break;
                case "answerSearch":
                    view.showEngineAnswers(model.currTop50);
                    break;

            }

        }
    }


    public void setViewModel(View view, Model model) {
    this.view= view;
    this.model=model;
    }
}
