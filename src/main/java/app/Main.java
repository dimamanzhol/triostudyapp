package app;

import controllers.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.DataManager;

public class Main extends Application {

    private DataManager dataManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            dataManager = new DataManager();
            dataManager.loadData();

            MainController mainController = new MainController(dataManager);

            VBox root = mainController.getView();
            Scene scene = new Scene(root, 800, 600);

            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("Visual Study Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest(event -> {
                dataManager.saveData();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}