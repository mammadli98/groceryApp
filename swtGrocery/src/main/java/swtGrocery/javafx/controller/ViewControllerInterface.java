package swtGrocery.javafx.controller;

import java.util.LinkedList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import swtGrocery.backend.services.exceptions.GenericServiceException;

/**
 * Interface for all viewcontrollers.
 * Uses the following annotations: @Controller
 *
 * @SpringBootApplication
 * @ComponentScan("swtGrocery")
 * @Scope("singleton") Autowire the Itemcontroller itemcontroller; in the attributes of your controller.
 */

public interface ViewControllerInterface {
  /**
   * Initializes the controller. Setup view and connect model.
   */
  void initialize() throws GenericServiceException;

  /**
   * CREATE.
   *
   * @throws GenericServiceException
   */
  void addItemToCV() throws GenericServiceException;

  /**
   * Structure the alert for the user. Already has a default implementation for compatibility reasons.
   *
   * @param title
   * @param text
   */
  static void showUserAlert(String title, String text) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(text);
    alert.showAndWait();
  }

  /**
   * Prompts the user with a choicedialogue.
   */
  static String readUserChoiceFromDialogue(LinkedList<String> choices) {
    ChoiceDialog<String> dialog = new ChoiceDialog<>(
      choices.getFirst(),
      choices
    );
    dialog.setTitle("Choose Category");
    dialog.setHeaderText(
      "Choose the category you want to add the item with to the grocery list."
    );
    dialog.setContentText("Category:");
    dialog.showAndWait();
    return dialog.getSelectedItem();
  }

  static String readUserInputFromTextInputDialogue(
    String title,
    String header,
    String content
  ) {
    TextInputDialog dialog = new TextInputDialog("1");
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    dialog.setContentText(content);

    dialog.showAndWait();
    boolean status = true;
    while (status) {
      try {
        status = false;
        Integer.parseInt(dialog.getResult());
        if (Integer.parseInt(dialog.getResult()) <= 0) {
          status = true;
          dialog.setHeaderText(
            header + "\nPlease enter a quanity larger than 0."
          );
          dialog.showAndWait();
        }
      } catch (NumberFormatException e) {
        status = true;
        dialog.setHeaderText(
          header + "\nPlease enter a valid number (as integer)."
        );
        dialog.showAndWait();
      }
    }
    return dialog.getResult();
  }

  /**
   * UPDATE.
   *
   * @throws GenericServiceException
   */
  void modifyEntry() throws GenericServiceException;

  /**
   * DELETE.
   *
   * @throws GenericServiceException
   */
  void deleteEntry() throws GenericServiceException;
}
