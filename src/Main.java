import Controller.Controller;
import Model.Model;
import View.View;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public View view ;
    public Controller controller;
    public Model model;

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Engine Machine");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("resources/view.fxml").openStream());
        Scene scene = new Scene(root, 1000, 400);
        scene.getStylesheets().add(getClass().getResource("resources/MenuStyle.css").toExternalForm());
        view = fxmlLoader.getController() ;
        primaryStage.setScene(scene);

        Controller controller= new Controller();
        view.addObserver(controller);

        Model model= new Model();
        model.addObserver(controller);

        controller.setViewModel(view,model);
        primaryStage.show();



    }


    public static void main(String[] args) {
        launch(args);
    }


}
