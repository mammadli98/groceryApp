package swtGrocery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App extends Application {

  private ConfigurableApplicationContext springContext;
  private FXMLLoader fxmlLoader;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void init() throws Exception {
    springContext = SpringApplication.run(App.class);
    fxmlLoader = new FXMLLoader();
    fxmlLoader.setControllerFactory(springContext::getBean);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    fxmlLoader.setLocation(getClass().getResource("/fxml/mainView.fxml"));
    Parent rootNode = fxmlLoader.load();
    // Tip: Here, be careful with the primaryStage.setResizable setting.
    // Monitor resolution varies, and setting it to not be resizable
    // can lead to problems on some operating systems, for example Kubuntu 22.04.

    primaryStage.setTitle("swtGrocery");
    primaryStage.setScene(new Scene(rootNode, 800, 600));
    primaryStage.show();
  }

  @Override
  public void stop() {
    springContext.close();
  }
}
