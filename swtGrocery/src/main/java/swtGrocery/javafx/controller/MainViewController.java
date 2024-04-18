package swtGrocery.javafx.controller;

import javafx.fxml.FXML;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@SpringBootApplication
@ComponentScan("swtGrocery")
@Scope("singleton")
public class MainViewController {

  ListViewController listViewController;
  PastPurchasesViewController pastPurchasesViewController;
  CatalogueViewController catalogueViewController;

  @FXML
  public void initialize() {
    pastPurchasesViewController = new PastPurchasesViewController();
    listViewController = new ListViewController(pastPurchasesViewController);
    catalogueViewController = new CatalogueViewController(listViewController);
  }
}
