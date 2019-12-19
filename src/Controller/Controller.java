package Controller;

import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import Model.Model;
import View.View;

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
                    if(model.loadDictionary(view.loadDic,view.isStemming))
                        view.SetLoadDictionarySuccessfully(true);
                    else
                        view.SetLoadDictionarySuccessfully(false);
                    break;
                case "showDictionary":
                    model.showDictionary();
                    break;
                case "reset":
                    model.reset();
                    break;
            }
        }
        else if( o == model){
            switch((String) arg) {
                case "showDictionary":
                    view.setDictionary(((Model)o).getTerms());
                    break;

            }

        }
    }


    public void setViewModel(View view, Model model) {
    this.view= view;
    this.model=model;
    }
}
