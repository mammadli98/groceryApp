package swtGrocery.javafx.controller;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import swtGrocery.api.contract.*;
import swtGrocery.api.controller.*;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Controller
@SpringBootApplication
@ComponentScan("swtGrocery")
@Scope("singleton")
public class PastPurchasesViewController {

  @Autowired
  PastPurchaseItemController pastPurchaseItemController;

  ObservableList<PastPurchaseItemDTO> listOfPastPurchasesForTable = FXCollections.observableArrayList();

  @FXML
  TableView<PastPurchaseItemDTO> table;

  @FXML
  TableColumn<PastPurchaseItemDTO, String> name = new TableColumn<>("Name");

  @FXML
  TableColumn<PastPurchaseItemDTO, String> unit = new TableColumn<>("Unit");

  @FXML
  TableColumn<PastPurchaseItemDTO, String> quantity = new TableColumn<>(
    "Quantity"
  );

  @FXML
  TableColumn<PastPurchaseItemDTO, String> date = new TableColumn<>("Date");

  @FXML
  TableColumn<PastPurchaseItemDTO, String> category = new TableColumn<>(
    "Category"
  );

  @FXML
  private DatePicker endDatePicker;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  Button search;

  @FXML
  Button endSearch;

  @FXML
  TextField itemName;

  CatalogueViewController catalogueViewController;

  public void setCatalogueViewController(
    CatalogueViewController catalogueViewController
  ) {
    this.catalogueViewController = catalogueViewController;
  }

  /**
   * initial constructor called through @FXML at start of application. Fetches all items from the database and displays them in the listview.
   * The table is sorted by date in descending order.
   */
  @FXML
  public void initialize() {
    setCatalogueTable();

    date.setComparator(date.getComparator().reversed());
    table.getSortOrder().add(date);
    table.refresh();

    // Set date picker limits
    setDatePickerLimits();
  }

  /**
   * Fill the table view with the items and units
   */
  private void setCatalogueTable() {
    table.setEditable(false);
    table.getItems().clear();
    name.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().getItemName())
    );
    unit.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().getItemUnit())
    );
    quantity.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          String.valueOf(cellData.getValue().getItemQuantity())
        )
    );
    category.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          String.valueOf(cellData.getValue().getCategory())
        )
    );
    date.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          String.valueOf(cellData.getValue().getItemPurchaseDate())
        )
    );
    listOfPastPurchasesForTable.setAll(
      pastPurchaseItemController.pastPurchaseItems()
    );
    table.setItems(listOfPastPurchasesForTable);
    table.getSelectionModel().selectFirst();
    table.getColumns().clear();
    table.getColumns().addAll(List.of(name, date, quantity, unit, category));
  }

  /**
   * Import items with user's .csv file.
   */
  @FXML
  private void importPastPurchaseList(MouseEvent event)
    throws GenericServiceException {
    // Create a file chooser
    FileChooser fileChooser = new FileChooser();

    // Set extension filter
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
      "CSV files (*.csv)",
      "*.csv"
    );
    fileChooser.getExtensionFilters().add(extFilter);

    // Show the file chooser dialog
    File selectedFile = fileChooser.showOpenDialog(new Stage());

    // Check if a file was selected
    if (selectedFile != null) {
      // Print the path of the selected file
      pastPurchaseItemController.importPastPurchaseList(
        Path.of(selectedFile.getAbsolutePath())
      );
      initialize();
      this.catalogueViewController.alignListOfItemDTOWithDB();
      this.catalogueViewController.populateUnitDropDownMenu();
      this.catalogueViewController.populateCategoryDropDownMenuWithMissingCategories();
      this.catalogueViewController.listViewController.setAprioriLibrary(
          pastPurchaseItemController.runAprioriAlgorithm()
        );
      this.catalogueViewController.listViewController.initialize();
    }
  }

  /**
   * Set limits on date pickers to enforce the date range.
   */
  private void setDatePickerLimits() {
    startDatePicker.setDayCellFactory(
      picker ->
        new DateCell() {
          @Override
          public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);
            setDisable(
              empty ||
              (
                endDatePicker.getValue() != null &&
                date.isAfter(endDatePicker.getValue())
              ) ||
              date.isAfter(LocalDate.now())
            );
          }
        }
    );

    endDatePicker.setDayCellFactory(
      picker ->
        new DateCell() {
          @Override
          public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);
            setDisable(
              empty ||
              (
                startDatePicker.getValue() != null &&
                date.isBefore(startDatePicker.getValue())
              ) ||
              date.isAfter(LocalDate.now())
            );
          }
        }
    );
  }

  public void searchInPurchaseList(MouseEvent mouseEvent)
    throws GenericServiceException {
    List<PastPurchaseItemDTO> listOfSearchedPurchaseList = new ArrayList<>();

    try {
      if (!itemName.getText().isEmpty()) {
        listOfSearchedPurchaseList = searchByNameOrDate();
      } else if (
        startDatePicker.getValue() != null || endDatePicker.getValue() != null
      ) {
        listOfSearchedPurchaseList = searchByDate();
      } else {
        ViewControllerInterface.showUserAlert(
          "No Search Criteria",
          "Please enter an item name or select a date range to perform the search."
        );
      }

      table.setEditable(false);
      table.getItems().clear();
      //Map the item name to the first column
      name.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getItemName())
      );
      //Map the unit name to the second column
      unit.setCellValueFactory(
        cellData -> new SimpleStringProperty(cellData.getValue().getItemUnit())
      );
      //Map the quantity to the third column
      quantity.setCellValueFactory(
        cellData ->
          new SimpleStringProperty(
            String.valueOf(cellData.getValue().getItemQuantity())
          )
      );
      //Map the date to the fourth column
      date.setCellValueFactory(
        cellData ->
          new SimpleStringProperty(
            String.valueOf(cellData.getValue().getItemPurchaseDate())
          )
      );
      category.setCellValueFactory(
        cellData ->
          new SimpleStringProperty(
            String.valueOf(cellData.getValue().getCategory())
          )
      );
      //Add the list to be displayed to the table view
      listOfPastPurchasesForTable.setAll(listOfSearchedPurchaseList);
      table.setItems(listOfPastPurchasesForTable);
      table.getSelectionModel().selectFirst();
      //Add columns to the table
      table.getColumns().clear();
      table.getColumns().addAll(List.of(name, quantity, unit, category, date));
    } catch (GenericServiceException e) {
      ViewControllerInterface.showUserAlert(
        "No Matching Items",
        "No items match the provided criteria. Please refine your search or try different filters."
      );
    }
  }

  public void switchFromSearchResultToList(MouseEvent mouseEvent) {
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    itemName.clear();
    setCatalogueTable();
  }

  private List<PastPurchaseItemDTO> searchByNameOrDate()
    throws GenericServiceException {
    if (
      startDatePicker.getValue() != null && endDatePicker.getValue() != null
    ) {
      return pastPurchaseItemController.searchByNameAndDate(
        itemName.getText(),
        startDatePicker.getValue(),
        endDatePicker.getValue()
      );
    } else if (
      startDatePicker.getValue() != null && endDatePicker.getValue() == null
    ) {
      // Handle startDatePicker != null, endDatePicker == null
      endDatePicker.setValue(startDatePicker.getValue());
      return pastPurchaseItemController.searchByNameAndDate(
        itemName.getText(),
        startDatePicker.getValue(),
        endDatePicker.getValue()
      );
    } else if (
      startDatePicker.getValue() == null && endDatePicker.getValue() != null
    ) {
      // Handle startDatePicker == null, endDatePicker != null
      startDatePicker.setValue(endDatePicker.getValue());
      return pastPurchaseItemController.searchByNameAndDate(
        itemName.getText(),
        startDatePicker.getValue(),
        endDatePicker.getValue()
      );
    } else {
      return pastPurchaseItemController.searchByName(itemName.getText());
    }
  }

  private List<PastPurchaseItemDTO> searchByDate()
    throws GenericServiceException {
    if (
      startDatePicker.getValue() != null && endDatePicker.getValue() != null
    ) {
      return pastPurchaseItemController.searchByDate(
        startDatePicker.getValue(),
        endDatePicker.getValue()
      );
    } else if (startDatePicker.getValue() != null) {
      endDatePicker.setValue(startDatePicker.getValue());
    } else if (endDatePicker.getValue() != null) {
      startDatePicker.setValue(endDatePicker.getValue());
    }
    return pastPurchaseItemController.searchByDate(
      startDatePicker.getValue(),
      endDatePicker.getValue()
    );
  }
}
