package swtGrocery.javafx.controller;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

import com.sun.javafx.application.PlatformImpl;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import swtGrocery.api.contract.*;
import swtGrocery.api.controller.*;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Controller
@SpringBootApplication
@ComponentScan("swtGrocery")
@Scope("singleton")
public class ListViewController {

  @Autowired
  GroceryListController groceryListController;

  @Autowired
  AssociationGroceryListController associationGroceryListController;

  @Autowired
  PastPurchaseItemController pastPurchaseItemController;

  @Autowired
  ItemController itemController;

  @Autowired
  UnitController unitController;

  @Autowired
  AssociationItemUnitController associationItemUnitController;

  Tab selectedGroceryList;

  @FXML
  private TextField listNameText;

  @FXML
  private TabPane groceryListPane;

  private ObservableList<String> listOfSuggestion = FXCollections.observableArrayList();

  @FXML
  TableView<String> table;

  @FXML
  TableColumn<String, String> item = new TableColumn<>("Item");

  @FXML
  TableColumn<String, String> unit = new TableColumn<>("Unit");

  @FXML
  TableColumn<String, String> quantity = new TableColumn<>("Quantity");

  @FXML
  TableColumn<String, String> accept = new TableColumn<>("Accept");

  @FXML
  TableColumn<String, String> reject = new TableColumn<>("Reject");

  Map<Object, Object> aprioriLibrary = new HashMap<>();

  List<TableView<AssociationGroceryListDTO>> allTablesInGroceryList = new ArrayList<>();
  PastPurchasesViewController pastPurchasesViewController;
  private PlatformImpl Platform;

  public ListViewController(
    PastPurchasesViewController pastPurchasesViewController
  ) {
    this.pastPurchasesViewController = pastPurchasesViewController;
  }

  /**
   * initial constructor called through @FXML at start of application. Fetches all items from the database and displays them in the listview.
   */
  @FXML
  public void initialize() throws GenericServiceException {
    this.alignGroceryListsWithDB();
    aprioriLibrary = pastPurchaseItemController.runAprioriAlgorithm();
    setSuggestionTable();
  }

  public void setAprioriLibrary(Map<Object, Object> aprioriLibrary) {
    this.aprioriLibrary = aprioriLibrary;
  }

  public Map<Object, Object> getAprioriLibrary() {
    return aprioriLibrary;
  }

  public void addToListOfSuggestion(String itemToSuggest) {
    this.listOfSuggestion.add(itemToSuggest);
    setSuggestionTable();
  }

  /**
   * Creates a new GroceryList in Backend and update view.
   */
  @FXML
  private void createGroceryList() throws GenericServiceException {
    //get list name from GUI
    String listName = listNameText.getText();
    //check if given name is empty
    if (listName.isEmpty()) {
      ViewControllerInterface.showUserAlert(
        "Invalid Entry",
        "Please add a name to the list"
      );
    } else {
      //create list in backend
      try {
        groceryListController.create(listName);
      } catch (GenericServiceException e) {
        ViewControllerInterface.showUserAlert(
          "Invalid Entry",
          "There is a list with this name"
        );
      }
      //update view to show the new list
      alignGroceryListsWithDB();
    }
  }

  /**
   * Creates columns for the table, and map parameters of @AssociationGroceryListDTO class to them.
   *
   * @param tableList to add new columns to
   * @see AssociationGroceryListDTO
   */
  private void setGroceryListTable(
    TableView<AssociationGroceryListDTO> tableList
  ) {
    //create 6 columns for the table
    TableColumn<AssociationGroceryListDTO, String> itemNameInList = new TableColumn<>(
      "Name"
    );
    TableColumn<AssociationGroceryListDTO, String> unitInList = new TableColumn<>(
      "Unit"
    );
    TableColumn<AssociationGroceryListDTO, String> quantityOfItem = new TableColumn<>(
      "Quantity"
    );
    TableColumn<AssociationGroceryListDTO, Boolean> isItemPurchased = new TableColumn<>(
      "Select"
    );
    TableColumn<AssociationGroceryListDTO, String> categoryOfItem = new TableColumn<>(
      "Category"
    );
    categoryOfItem.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          cellData
            .getValue()
            .getAssociationItemUnit()
            .getItem()
            .getItemCategory()
        )
    );

    TableColumn<AssociationGroceryListDTO, AssociationGroceryListDTO> delete = new TableColumn<>(
      "Remove Item"
    );

    tableList.setEditable(true);
    tableList.getItems().clear();

    //Mapping of the name of item to the table
    itemNameInList.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          cellData.getValue().getAssociationItemUnit().getItem().getItemName()
        )
    );
    //Mapping of the unit of item to the table
    unitInList.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          cellData.getValue().getAssociationItemUnit().getUnit().getUnitName()
        )
    );

    //Mapping of the quantity of item to the table
    quantityOfItem.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(
          String.valueOf(cellData.getValue().getQuantity())
        )
    );
    quantityOfItem.setCellFactory(TextFieldTableCell.forTableColumn());
    handelChangingQuantity(quantityOfItem);

    setItemPurchasedColumn(isItemPurchased);

    setDeleteColumn(delete);
    //add columns to the table
    tableList
      .getColumns()
      .addAll(
        List.of(
          isItemPurchased,
          itemNameInList,
          quantityOfItem,
          unitInList,
          categoryOfItem,
          delete
        )
      );
  }

  /**
   * Set the value(boolean) and the graphic(checkbox) for the given column
   *
   * @param isItemPurchased column to be set
   */
  private void setItemPurchasedColumn(
    TableColumn<AssociationGroceryListDTO, Boolean> isItemPurchased
  ) {
    isItemPurchased.setCellValueFactory(
      //Map the boolean value in entity to a BooleanProperty
      f -> {
        final BooleanProperty selected = new SimpleBooleanProperty(
          f.getValue().isPurchased()
        );
        //Listener to save the checking/unchecking of the checkbox
        selected.addListener(
          (obs, wasSelected, isSelected) -> {
            try {
              AssociationGroceryListDTO associationGroceryListDTO = f.getValue();
              associationGroceryListController.setPurchased(
                associationGroceryListDTO,
                isSelected
              );
              alignCurrentTab();
            } catch (GenericServiceException e) {
              throw new RuntimeException(e);
            }
          }
        );
        return selected;
      }
    );

    isItemPurchased.setCellFactory(p -> new CheckBoxTableCell<>());
  }

  /**
   * Set the graphic(button) for the given column
   *
   * @param delete column to be set
   */
  private void setDeleteColumn(
    TableColumn<AssociationGroceryListDTO, AssociationGroceryListDTO> delete
  ) {
    delete.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue())
    );
    delete.setCellFactory(
      param ->
        new TableCell<>() {
          private final Button deleteButton = new Button("X");

          /**
           * Updates the cell whenever the "isPurchased" value change and perform the deletion
           * @param associationGroceryListDTO The new item for the cell.
           * @param empty whether this cell represents data from the list. If it
           *        is empty, then it does not represent any domain data, but is a cell
           *        being used to render an "empty" row.
           */
          @Override
          protected void updateItem(
            AssociationGroceryListDTO associationGroceryListDTO,
            boolean empty
          ) {
            super.updateItem(associationGroceryListDTO, empty);
            if (associationGroceryListDTO == null) {
              setGraphic(null);
              return;
            } else if (
              associationGroceryListDTO.isPurchased() && getTableRow() != null
            ) {
              //If an item is purchased, change row color and remove the button
              getTableRow().setStyle("-fx-background-color:#9a9696");
              setGraphic(null);
              return;
            }
            //Delete the item when clicking delete
            deleteButton.setOnAction(
              event -> {
                AssociationGroceryListDTO itemToDelete = getTableView()
                  .getItems()
                  .get(getIndex());
                try {
                  associationGroceryListController.deleteItemAndUnitFromGroceryList(
                    itemToDelete
                  );
                  alignCurrentTab();
                } catch (GenericServiceException e) {
                  throw new RuntimeException(e);
                }
              }
            );
            setGraphic(deleteButton);
          }
        }
    );
  }

  /**
   * Adds a new Tab to the Grocery list view.
   * Takes a GroceryListDTO and create a new tab with new table with the value of the list
   *
   * @param list contains the content to be displayed in the tab
   * @return the created tab
   */
  private Tab addTitledTab(GroceryListDTO list) {
    Tab newTab = new Tab(list.getName());
    TableView<AssociationGroceryListDTO> tableList = new TableView<>();
    tableList.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    allTablesInGroceryList.add(tableList);
    //map list content to the new table
    setGroceryListTable(tableList);
    try {
      //List with all association to be displayed
      List<AssociationGroceryListDTO> associationGroceryListDTO = associationGroceryListController.findByGroceryList(
        list
      );
      if (
        associationGroceryListDTO == null || associationGroceryListDTO.isEmpty()
      ) {} else {
        ObservableList<AssociationGroceryListDTO> listOfItemsInList = FXCollections.observableArrayList(
          associationGroceryListDTO
        );
        //fill the table with the list of associations
        tableList.setItems(listOfItemsInList);
        tableList.getSortOrder().add(tableList.getColumns().get(0));
      }
    } catch (GenericServiceException e) {
      //Notification
    }

    //Add table to the Tab
    newTab.setContent(tableList);
    handelChangingTabEvent(newTab);
    // Add the Tab to the Accordion
    groceryListPane.getTabs().add(newTab);
    return newTab;
  }

  /**
   * Update the value of @selectedGroceryList when the selection changes
   */
  private void handelChangingTabEvent(Tab newTab) {
    newTab.setOnSelectionChanged(
      (
        event -> {
          // Get the text (name) of the clicked TitledPane
          selectedGroceryList = newTab;
        }
      )
    );
  }

  /**
   * Update the value of quantity in back- and frontend when the user add new value
   */
  private void handelChangingQuantity(
    TableColumn<AssociationGroceryListDTO, String> quantityOfItem
  ) {
    quantityOfItem.setOnEditCommit(
      event -> {
        //Save the new value only if it's a number
        String value = event.getNewValue() != null &&
          event.getNewValue().matches("[0-9]+")
          ? event.getNewValue()
          : event.getOldValue();
        try {
          int newQuantity = Integer.parseInt(value);
          //get the associationGroceryListDTO in which the quantity will be updated
          AssociationGroceryListDTO associationGroceryListDTO =
            (
              event
                .getTableView()
                .getItems()
                .get(event.getTablePosition().getRow())
            );
          //update value in backend
          associationGroceryListController.updateItemAndUnitQuantity(
            associationGroceryListDTO,
            newQuantity
          );
          //update table view
          alignCurrentTab();
        } catch (GenericServiceException | NumberFormatException e) {
          ViewControllerInterface.showUserAlert(
            "Invalid Entry",
            "Please enter a number in quantity"
          );
        }
      }
    );
  }

  /**
   * Update GroceryList TableView to match the backend
   * This method clears all tabs and creates new one and reselect the last selected tab
   */
  void alignGroceryListsWithDB() throws GenericServiceException {
    String nameOfSelectedTab = "";
    if (this.groceryListPane == null) {
      return;
    }
    if (groceryListPane.getSelectionModel().getSelectedItem() != null) {
      //save the name of the last selected tab
      nameOfSelectedTab =
        groceryListPane.getSelectionModel().getSelectedItem().getText();
    } else {
      selectedGroceryList = null;
    }
    groceryListPane.getTabs().clear();
    allTablesInGroceryList.clear();
    //Create a list of all grocery lists in backend
    List<GroceryListDTO> groceryLists = groceryListController.groceryLists();
    //Create new tab for each list
    for (GroceryListDTO list : groceryLists) {
      //Creat and select a Tab if the list name equals the last selected tab name
      if (list.getName().equals(nameOfSelectedTab)) {
        groceryListPane.getSelectionModel().select(addTitledTab(list));
      } else {
        addTitledTab(list);
      }
    }
  }

  /**
   * Refreshes the table of the current tab by creates new table to match the DB.
   */
  public void alignCurrentTab() throws GenericServiceException {
    Tab tab = groceryListPane.getSelectionModel().getSelectedItem();
    if (tab != null) {
      TableView<AssociationGroceryListDTO> tableView = (TableView<AssociationGroceryListDTO>) tab.getContent();
      allTablesInGroceryList.remove(tableView);
      try {
        List<AssociationGroceryListDTO> associationGroceryListDTO = associationGroceryListController.findByGroceryList(
          groceryListController.findByName(tab.getText())
        );
        createNewTable(associationGroceryListDTO, tab);
      } catch (GenericServiceException e) {
        alignGroceryListsWithDB();
      }
    }
  }

  /**
   * Creates new Table and add it to the given Tab
   *
   * @param associationGroceryListDTO list of content to be added to the new table
   * @param tab                       in which the table should be added
   */
  private void createNewTable(
    List<AssociationGroceryListDTO> associationGroceryListDTO,
    Tab tab
  ) {
    TableView<AssociationGroceryListDTO> newTable = new TableView<>();

    newTable.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    allTablesInGroceryList.add(newTable);
    //Create new columns to the table
    setGroceryListTable(newTable);
    ObservableList<AssociationGroceryListDTO> listOfItemsInList = FXCollections.observableArrayList(
      associationGroceryListDTO
    );
    //set list which contains the content to be displayed in the new table
    newTable.setItems(listOfItemsInList);
    newTable.getSortOrder().add(newTable.getColumns().get(0));
    tab.setContent(newTable);
  }

  /**
   * Deletes the whole Grocery List and his Tab/Table
   */
  @FXML
  private void deleteGroceryList() {
    if (selectedGroceryList != null) {
      TableView<AssociationGroceryListDTO> tableView = (TableView<AssociationGroceryListDTO>) selectedGroceryList.getContent();
      //Get all associations from the table
      ObservableList<AssociationGroceryListDTO> items = tableView.getItems();
      //Stop deletion if a purchased item exists in the list to be deleted
      for (AssociationGroceryListDTO associationGroceryListDTO : items) {
        if (associationGroceryListDTO.isPurchased()) {
          ViewControllerInterface.showUserAlert(
            "Invalid Operation",
            "Please move purchased items to Past Purchase List before deleting this list"
          );
          return;
        }
      }
      try {
        groceryListController.delete(
          groceryListController.findByName(selectedGroceryList.getText())
        );
        alignGroceryListsWithDB();
      } catch (GenericServiceException e) {
        ViewControllerInterface.showUserAlert(
          "Invalid Operation",
          "There is no list"
        );
      }
    } else {
      ViewControllerInterface.showUserAlert(
        "Invalid Operation",
        "There is no list"
      );
    }
  }

  /**
   * Passes purchased items to Past purchased list and delete them from the current list.
   * A notification with purchased items will appear after deleting.
   * This method is triggered by the Button @Complete_Grocery_List.
   */
  @FXML
  private void completeGroceryList() throws GenericServiceException {
    //Align grocery list to get the current value "Purchased" from items.
    alignGroceryListsWithDB();
    StringBuilder s = new StringBuilder("{");
    List<AssociationGroceryListDTO> listOfPurchasedItems = new ArrayList<>();
    if (selectedGroceryList != null) {
      try {
        TableView<AssociationGroceryListDTO> tableView = (TableView<AssociationGroceryListDTO>) selectedGroceryList.getContent();
        ObservableList<AssociationGroceryListDTO> items = tableView.getItems();
        for (AssociationGroceryListDTO associationGroceryListDTO : items) {
          if (associationGroceryListDTO.isPurchased()) {
            //Purchased items will be added to a list to delete
            listOfPurchasedItems.add(associationGroceryListDTO);
            pastPurchaseItemController.create(
              associationGroceryListDTO
                .getAssociationItemUnit()
                .getItem()
                .getItemName(),
              associationGroceryListDTO
                .getAssociationItemUnit()
                .getUnit()
                .getUnitName(),
              associationGroceryListDTO.getQuantity(),
              associationGroceryListDTO
                .getAssociationItemUnit()
                .getItem()
                .getItemCategory()
              // category
            );
            s.append(associationGroceryListDTO).append("; ");
          }
        }
        s.append("}");
        deleteListOfItemsFromGroceryList(listOfPurchasedItems);
        ViewControllerInterface.showUserAlert(
          "This List is completed",
          "Those items will be added to Past Purchase List" + s
        );
      } catch (GenericServiceException e) {
        ViewControllerInterface.showUserAlert(
          "Invalid Operation",
          "There is no list"
        );
      }
    } else {
      ViewControllerInterface.showUserAlert(
        "Invalid Operation",
        "There is no list"
      );
    }
    pastPurchasesViewController.initialize();
  }

  /**
   * Handles the sorting of items by category when the sort button is clicked.
   *
   * @throws GenericServiceException if there is an error during the sorting process
   */
  @FXML
  private void sortItemsByCategory() throws GenericServiceException {
    if (selectedGroceryList != null) {
      GroceryListDTO currentList = groceryListController.findByName(
        selectedGroceryList.getText()
      );
      if (currentList != null) {
        try {
          List<AssociationGroceryListItemUnit> sortedItems = associationGroceryListController.getSortedItemsByCategoryForGroceryList(
            currentList.getId()
          );
          ObservableList<AssociationGroceryListDTO> sortedObservableList = convertToObservableList(
            sortedItems
          );
          updateTableView(sortedObservableList);
        } catch (GenericServiceException e) {
          ViewControllerInterface.showUserAlert(
            "Invalid Operation",
            "Error sorting items"
          );
        }
      }
    }
  }

  /**
   * Converts a list of sorted AssociationGroceryListItemUnit into an ObservableList of AssociationGroceryListDTO.
   *
   * @param sortedItems The sorted list of AssociationGroceryListItemUnit.
   * @return An ObservableList of AssociationGroceryListDTO.
   */
  private ObservableList<AssociationGroceryListDTO> convertToObservableList(
    List<AssociationGroceryListItemUnit> sortedItems
  ) {
    List<AssociationGroceryListDTO> associationGroceryListDTOS = sortedItems
      .stream()
      .map(
        assocUnit ->
          new AssociationGroceryListDTO(
            assocUnit.getId(),
            assocUnit.getAssociationItemUnit(),
            assocUnit.getGroceryList(),
            assocUnit.getQuantity(),
            assocUnit.getisPurchased()
          )
      )
      .collect(Collectors.toList());
    return FXCollections.observableArrayList(associationGroceryListDTOS);
  }

  /**
   * Updates the TableView with the given ObservableList.
   *
   * @param sortedObservableList The ObservableList to be set in the TableView.
   */
  private void updateTableView(
    ObservableList<AssociationGroceryListDTO> sortedObservableList
  ) {
    TableView<AssociationGroceryListDTO> tableView = findTableViewForSelectedList();
    if (tableView != null) {
      tableView.setItems(sortedObservableList);
      tableView.refresh();
    }
  }

  /**
   * Finds the TableView corresponding to the currently selected list.
   *
   * @return The TableView for the selected list, or null if no list is selected.
   */
  private TableView<AssociationGroceryListDTO> findTableViewForSelectedList() {
    Tab tab = groceryListPane.getSelectionModel().getSelectedItem();
    if (tab != null) {
      return (TableView<AssociationGroceryListDTO>) tab.getContent();
    }
    return null;
  }

  /**
   * Takes a list of @AssociationGroceryListDTO and delete all associations from the current table
   *
   * @param listOfPurchasedItems list of items to be deleted
   * @throws GenericServiceException if a given association is not found in Database
   */
  private void deleteListOfItemsFromGroceryList(
    List<AssociationGroceryListDTO> listOfPurchasedItems
  )
    throws GenericServiceException {
    if (!listOfPurchasedItems.isEmpty()) {
      //Convert the list to ListIterator, so we can delete while iterating
      for (AssociationGroceryListDTO listOfPurchasedItem : listOfPurchasedItems) {
        //Delete all associations from DB
        associationGroceryListController.deleteItemAndUnitFromGroceryList(
          listOfPurchasedItem
        );
      }
      //Align the current table with the DB after deleting
      alignCurrentTab();
    }
  }

  public Tab getCurrentSelectedGroceryList() {
    return selectedGroceryList;
  }

  public void setSuggestionTable() {
    item.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().split("---")[0])
    );
    unit.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().split("---")[1])
    );
    quantity.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().split("---")[2])
    );
    quantity.setCellFactory(TextFieldTableCell.forTableColumn());
    handelChangingQu(quantity);
    reject.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue())
    );
    reject.setCellFactory(
      param ->
        new TableCell<>() {
          private final Button refuseButton = new Button("X");

          @Override
          protected void updateItem(String string, boolean empty) {
            super.updateItem(string, empty);
            if (string == null) {
              setGraphic(null);
              return;
            }
            //Delete the item when clicking delete
            refuseButton.setOnAction(
              event -> {
                String item = getTableView().getItems().get(getIndex());
                listOfSuggestion.remove(item);
              }
            );
            setGraphic(refuseButton);
          }
        }
    );
    accept.setCellValueFactory(
      param -> new ReadOnlyObjectWrapper<>(param.getValue())
    );
    accept.setCellFactory(
      param ->
        new TableCell<>() {
          private final Button acceptButton = new Button("✓");

          @Override
          protected void updateItem(String string, boolean empty) {
            super.updateItem(string, empty);
            if (string == null) {
              setGraphic(null);
              return;
            }
            //Delete the item when clicking delete
            acceptButton.setOnAction(
              event -> {
                String item = getTableView().getItems().get(getIndex());
                try {
                  ItemDTO itemDTO = itemController.findByName(
                    item.split("---")[0]
                  );
                  UnitDTO unitDTO = unitController.findByName(
                    item.split("---")[1]
                  );
                  AssociationItemUnitDTO associationItemUnitDTO = associationItemUnitController.findByItemAndUnit(
                    itemDTO,
                    unitDTO
                  );
                  GroceryListDTO groceryListDTO = groceryListController.findByName(
                    getCurrentSelectedGroceryList().getText()
                  );
                  try {
                    associationGroceryListController.addItemAndUnitToGroceryList(
                      associationItemUnitDTO,
                      groceryListDTO,
                      Integer.parseInt(item.split("---")[2])
                    );
                  } catch (GenericServiceException e) {
                    ViewControllerInterface.showUserAlert(
                      "Invalid Entry",
                      "This item already exists in this grocery list"
                    );
                  }
                  alignCurrentTab();
                  listOfSuggestion.remove(item);
                } catch (GenericServiceException e) {
                  throw new RuntimeException(e);
                }
              }
            );
            setGraphic(acceptButton);
          }
        }
    );
    try {
      listOfSuggestion.setAll(pastPurchaseItemController.suggestions());
      table.setEditable(true);
      table.setItems(listOfSuggestion);
      table.getSelectionModel().selectFirst();
      table.getColumns().clear();
      table.getColumns().addAll(List.of(item, quantity, unit, accept, reject));
    } catch (GenericServiceException e) {
      System.out.println("This is no suggestion");
    }
  }

  private void handelChangingQu(TableColumn<String, String> quantityOfItem) {
    quantityOfItem.setOnEditCommit(
      event -> {
        //Save the new value only if it's a number
        String value = event.getNewValue() != null &&
          event.getNewValue().matches("[0-9]+")
          ? event.getNewValue()
          : event.getOldValue();
        try {
          int newQuantity = Integer.parseInt(value);
          //get the associationGroceryListDTO in which the quantity will be updated
          String string =
            (
              event
                .getTableView()
                .getItems()
                .get(event.getTablePosition().getRow())
            );
          //update value in backend
          //update table view
          String newString =
            string.split("---")[0] +
            "---" +
            string.split("---")[1] +
            "---" +
            newQuantity;
          listOfSuggestion.remove(string);
          listOfSuggestion.add(newString);
        } catch (NumberFormatException e) {
          ViewControllerInterface.showUserAlert(
            "Invalid Entry",
            "Please enter a number in quantity"
          );
        }
      }
    );
  }

  public void suggestItem() throws GenericServiceException {
    Tab tab = groceryListPane.getSelectionModel().getSelectedItem();
    this.listOfSuggestion.removeAll();
    List<AssociationGroceryListDTO> associationGroceryListDTOs = associationGroceryListController.findByGroceryList(
      groceryListController.findByName(tab.getText())
    );
    List<String> items = new ArrayList<>();
    for (AssociationGroceryListDTO associationGroceryListDTO : associationGroceryListDTOs) {
      items.add(
        associationGroceryListDTO
          .getAssociationItemUnit()
          .getItem()
          .getItemName()
      );
    }
    Set<String> suggestedItems = pastPurchaseItemController.suggestItem(
      this.aprioriLibrary,
      items
    );
    if (!suggestedItems.isEmpty()) {
      /*
       *
       * 首先去掉弹窗，然后在建议tabel中对应的listOfSuggestion中添加新的字符串
       * */
      List<ItemDTO> temps = itemController.items();
      ItemDTO itemDTO;
      boolean exist;
      for (String suggestedItem : suggestedItems) {
        exist = false;
        for (String s : listOfSuggestion) {
          String[] strings = s.split("---");
          if (strings[0].equals(suggestedItem)) {
            exist = true;
            break;
          }
        }

        if (!exist) {
          itemDTO = null;
          for (ItemDTO temp : temps) {
            if (
              temp
                .getItemName()
                .toLowerCase()
                .equals(suggestedItem.toLowerCase())
            ) {
              itemDTO = temp;
              break;
            }
          }
          if (itemDTO != null) {
            List<UnitDTO> itemUnits = associationItemUnitController.getItemUnits(
              itemDTO
            );
            if (!itemUnits.isEmpty()) {
              listOfSuggestion.add(
                String.format(
                  "%s---%s---%d",
                  suggestedItem,
                  itemUnits.get(0).getUnitName(),
                  1
                )
              );
            }
          }
        }
      }
    }
  }

  private void refreshSuggestionTable() {
    table.setEditable(true);
    table.setItems(listOfSuggestion);
    table.getSelectionModel().selectFirst();
  }
}
