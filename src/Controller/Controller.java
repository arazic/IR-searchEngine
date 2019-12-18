package Controller;

import java.util.Observable;
import java.util.Observer;
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
                    model.startEngine(view.txtfld_corpusPath.getText(),view.txtfld_postingPath.getText(),view.isSteeming);
                    break;
                case "loadDictionary":
                    model.loadDictionary(view.loadDic,view.isSteeming);
                    break;

            }
        }
        else if( o == model){

        }
    }


    public void setViewModel(View view, Model model) {
    this.view= view;
    this.model=model;
    }
}
