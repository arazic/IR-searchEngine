import java.util.Observable;
import java.util.Observer;

public class Controller extends Observable implements Observer {
    private Model model;
    private View view;


    @Override
    public void update(Observable o, Object arg) {
//        if (o == view){
//
//        }
//        else if( o == model){
//
//        }
    }


    public void setViewModel(View view, Model model) {
    this.view= view;
    this.model=model;
    }
}
