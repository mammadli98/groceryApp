package swtGrocery.javafx.controller;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javax.swing.text.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import swtGrocery.api.contract.*;
import swtGrocery.api.controller.*;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Controller
@SpringBootApplication
@ComponentScan("swtGrocery")
@Scope("singleton")
public class CatalogueViewController implements ViewControllerInterface {

  @Autowired
  ItemController itemController;

  @Autowired
  AssociationItemUnitController associationItemUnitController;

  @Autowired
  UnitController unitController;

  @Autowired
  GroceryListController groceryListController;

  @Autowired
  AssociationGroceryListController associationGroceryListController;

  @Autowired
  PastPurchaseItemController pastPurchaseItemController;

  Tab selectedGroceryList;

  @FXML
  private TextField addingField = new TextField();

  @FXML
  private ChoiceBox<String> selectItem;

  @FXML
  private Button findByCategoryButton;

  private String selectedItemFromSelectItemBox;

  @FXML
  private ChoiceBox<String> selectUnit;

  private String selectedItemUnitFromSelectUnitBox;

  @FXML
  private int quantity;

  private String curSelected;

  // TODO: move out of controller and persist either in config file or DB
  private ObservableList<String> listOfCategories = FXCollections.observableArrayList(
    "Beverage",
    "Bread",
    "Dairy",
    "Fruit",
    "Meat",
    "Vegetables",
    "Condiments & Spices",
    "Snacks",
    "Fish & Seafood",
    "Frozen Food",
    "Canned Food",
    "Other"
  );

  @FXML
  private ComboBox<String> selectCategory = new ComboBox<>();

  @FXML
  private ComboBox<String> selectCategorytoFilter = new ComboBox<>();

  // If listOfItems is changed stringItems can be aligned using alignListOfItemDTOWithDB() again. This is done
  // because bindings are fragile and ListView needs string elements to be displayed in the frontend properly.
  private ObservableList<ItemDTO> listOfItems = FXCollections.observableArrayList();
  private ObservableList<UnitDTO> listOfUnits = FXCollections.observableArrayList();
  private ObservableList<CheckMenuItem> checkMenuItems = FXCollections.observableArrayList();

  @FXML
  private MenuButton unitDropDownSelectionMenu = new MenuButton();

  @FXML
  private Button endSearch = new Button();

  @FXML
  private TextField searchingBar = new TextField();

  private ObservableList<ItemDTO> listOfSearchResult = FXCollections.observableArrayList();

  @FXML
  TableView<ItemDTO> table;

  @FXML
  TableColumn<ItemDTO, String> itemName = new TableColumn<>("Name");

  @FXML
  TableColumn<ItemDTO, String> itemUnit = new TableColumn<>("Unit");

  @FXML
  TableColumn<ItemDTO, String> itemCategory = new TableColumn<>("Category");

  ObservableList<ItemDTO> listOfCatalogueTable = FXCollections.observableArrayList();

  ListViewController listViewController;

  private int doubleClickDelay = 300;

  /**
   * Prompts the user with a choicedialogue.
   *
   * @param choices List of choices for the user to select
   *
   * @returns string with user choice
   */
  public static String readUserChoiceFromDialogue(LinkedList<String> choices) {
    ChoiceDialog<String> dialog = new ChoiceDialog<>(
      choices.getFirst(),
      choices
    );
    dialog.setTitle("Choose Unit");
    dialog.setHeaderText(
      "Choose the unit you want to add the item with to the grocery list."
    );
    dialog.setContentText("Unit:");
    dialog.showAndWait();
    return dialog.getSelectedItem();
  }

  public int getDoubleClickDelay() {
    return doubleClickDelay;
  }

  public void setDoubleClickDelay(int doubleClickDelay) {
    this.doubleClickDelay = doubleClickDelay;
  }

  public CatalogueViewController(ListViewController listViewController) {
    this.listViewController = listViewController;
    PastPurchasesViewController pastPurchasesViewController =
      this.listViewController.pastPurchasesViewController;
    pastPurchasesViewController.setCatalogueViewController(this);
  }

  /**
   * initial constructor called through @FXML at start of application. Fetches all items from the database and displays them in the listview.
   */
  @FXML
  public void initialize() throws GenericServiceException {
    if (this.unitController.units().isEmpty()) {
      this.unitController.create("kilogram");
      this.unitController.create("gram");
      this.unitController.create("liter");
      this.unitController.create("milliliter");
      this.unitController.create("piece");
    }
    // fill up observable list of categories if these are not given by default
    populateCategoryDropDownMenuWithMissingCategories();
    alignListOfItemDTOWithDB();
    this.listOfUnits.setAll(this.unitController.units());
    this.populateUnitDropDownMenu();
    this.listViewController.alignGroceryListsWithDB();
    //this.populateItemDropDownMenu();
    populateCategoriestoFilter();
    this.setCatalogueTable();
    this.selectCategory.setItems(this.listOfCategories);
    this.addingField.textProperty()
      .addListener(
        (observableValue, s, t1) -> {
          try {
            this.setUnitSelectionInDropdown();
            this.populateComboBoxWithSelectedItemCategory();
          } catch (GenericServiceException e) {
            System.out.println(
              "Generic Service Exception: " +
              e +
              "The unit could not be set in the dropdown " +
              "menu."
            );
          }
        }
      );
  }

  /**
   * Method binding for mouse clicked distinguishing double click vs single click. Double click will move item
   * directly to the grocery list.
   */
  @FXML
  public void handleMouseClicked(MouseEvent event)
    throws GenericServiceException, InterruptedException {
    // Thread is put to sleep to leave time to the user to double click on the item.
    Thread.sleep(doubleClickDelay);
    if (event.getClickCount() == 1) {
      populateAddingFieldsWithTableSelection();
    } else if (event.getClickCount() == 2) {
      populateAddingFieldsWithTableSelection();
      // set the grocery to list selection such that the associationitemunitlist can be added to the current
      // tabbed list
      this.selectedItemFromSelectItemBox = this.addingField.getText();
      // if there is only one unit associated with the item, then add it directly to the grocery list
      if (
        this.associationItemUnitController.getItemUnits(
            itemController.findByName(this.addingField.getText())
          )
          .size() ==
        1
      ) {
        this.selectedItemUnitFromSelectUnitBox =
          this.associationItemUnitController.getItemUnits(
              itemController.findByName(this.addingField.getText())
            )
            .get(0)
            .getUnitName();
      } else {
        this.selectedItemUnitFromSelectUnitBox =
          ViewControllerInterface.readUserChoiceFromDialogue(
            this.associationItemUnitController.getItemUnits(
                this.itemController.findByName(this.addingField.getText())
              )
              .stream()
              .map(
                unitDTO -> {
                  try {
                    this.selectedItemUnitFromSelectUnitBox =
                      unitDTO.getUnitName();
                  } catch (NullPointerException e) {
                    throw new RuntimeException(e);
                  }
                  return unitDTO.getUnitName();
                }
              )
              .collect(Collectors.toCollection(LinkedList::new))
          );
      }
      this.quantity =
        Integer.parseInt(
          ViewControllerInterface.readUserInputFromTextInputDialogue(
            "Enter Quantity",
            "Please enter the quantity of the item you want to add to the list.",
            "Quantity:"
          )
        );
      addToGroceryList();
    }
  }

  /**
   * Searches for the currently available pastpurchase categories.
   *
   * @return List<String> of lower case representation of all pastPurchaseCategories; returns null if PastPurchase
   * list is empty
   */
  private List<String> getAllCategoriesFromPastPurchaseList() {
    List<PastPurchaseItemDTO> pastPurchases = pastPurchaseItemController.pastPurchaseItems();
    List<String> pastPurchaseCategories = new ArrayList<>();
    if (pastPurchases.isEmpty()) {
      return null;
    }

    for (PastPurchaseItemDTO pastPurchase : pastPurchases) {
      pastPurchaseCategories.add(pastPurchase.getCategory().toLowerCase());
    }
    return pastPurchaseCategories;
  }

  /**
   * Updates the category observable list (to update the category dropdown) with elements in the past purchase list
   * but not yet in the dropdown.
   */
  public void populateCategoryDropDownMenuWithMissingCategories() {
    List<String> pastPurchaseCategories = getAllCategoriesFromPastPurchaseList();
    try {
      if (pastPurchaseCategories == null) {} else {
        pastPurchaseCategories.forEach(
          category -> {
            if (
              !listOfCategories.contains(
                Utilities.capitalizeFirstLetter(category)
              )
            ) {
              listOfCategories.add(Utilities.capitalizeFirstLetter(category));
            }
          }
        );
      }
    } catch (NullPointerException ignored) {}
  }

  /**
   * Fill the table view with the items and units
   */
  private void setCatalogueTable() {
    table.setEditable(true);
    table.getItems().clear();
    //add items to the list to be displayed
    listOfCatalogueTable.addAll(itemController.items());
    //Map the item name to the first column
    itemName.setCellValueFactory(
      cellData -> new SimpleStringProperty(cellData.getValue().getItemName())
    );
    itemCategory.setCellValueFactory(
      cellData ->
        new SimpleStringProperty(cellData.getValue().getItemCategory())
    );
    //Map the unit name to the second column
    itemUnit.setCellValueFactory(
      cellData -> {
        try {
          return new SimpleStringProperty(
            listOfUnitsToDisplay(
              associationItemUnitController.getItemUnits(cellData.getValue())
            )
          );
        } catch (GenericServiceException e) {
          throw new RuntimeException(e);
        }
      }
    );
    //Add the list to be displayed to the table view
    table.setItems(listOfCatalogueTable);
    table.getSelectionModel().selectFirst();
    //Add columns to the table
    table.getColumns().addAll(List.of(itemName, itemUnit, itemCategory));
  }

  /**
   * Take a list of UnitDTO and return a String to be displayed
   * String is in this format: {Unit1, Unit2, ...}
   *
   * @param list list of units of an item
   *
   * @return string to be displayed in the column of units in catalogue table
   */
  private String listOfUnitsToDisplay(List<UnitDTO> list) {
    StringBuilder s = new StringBuilder();
    for (UnitDTO unitDTO : list) {
      s.append(unitDTO.getUnitName()).append("; ");
    }
    //s.replace(s.length() - 2, s.length() - 1, "}");
    return s.toString();
  }

  // CRUD Methods ListView

  /**
   * This Method populates the dropdown menu with the checkMenuItems reflecting the units names from the database.
   * Throws a
   * NullPointerException if the unit is null.
   **/
  public void populateUnitDropDownMenu() {
    this.checkMenuItems.clear();
    for (UnitDTO unit : this.listOfUnits) {
      try {
        CheckMenuItem unitValuesToShow = new CheckMenuItem(unit.getUnitName());
        this.checkMenuItems.add(unitValuesToShow);
      } catch (NullPointerException e) {
        Logger
          .getLogger("populateUnitDropDownMenu")
          .info("UnitDTO is null. Unit is not added to the list.");
      }
    }
    this.unitDropDownSelectionMenu.getItems().setAll(this.checkMenuItems);
  }

  /**
   * This method adds an item to the database and updates the listview.
   * Throws a GenericServiceException if the item is null.
   */
  @FXML
  public void addItemToCV() throws GenericServiceException {
    String itemName = this.addingField.getText();
    if (itemName == null || itemName.isEmpty()) {
      ViewControllerInterface.showUserAlert(
        "No name is entered",
        "Please add a name to the unit"
      );
      return;
    }

    List<String> selectedUnits =
      this.checkMenuItems.stream()
        .filter(CheckMenuItem::isSelected)
        .map(MenuItem::getText)
        .collect(Collectors.toList());
    if (selectedUnits.isEmpty()) {
      ViewControllerInterface.showUserAlert(
        "No unit is selected",
        "Please select at least one unit"
      );
      return;
    }

    String selectedCategory = selectCategory.getValue();
    if (
      selectedCategory == null ||
      selectedCategory.isEmpty() ||
      selectedCategory.trim().isEmpty()
    ) {
      ViewControllerInterface.showUserAlert(
        "No category is selected",
        "Please select a category"
      );
      return;
    }

    if (checkIfItemNameExistsInDB(itemName, false)) {
      return;
    }

    try {
      this.associationItemUnitController.create(
          itemName,
          selectedUnits,
          selectedCategory
        );
      System.out.println("Item added to database");
    } catch (GenericServiceException g) {
      ViewControllerInterface.showUserAlert(
        "Invalid Entry",
        "Please enter a valid item name."
      );
    }
    this.alignListOfItemDTOWithDB();
    this.table.requestFocus();
    this.table.getSelectionModel().selectFirst();
    populateAddingFieldWithItemSelection();
  }

  /**
   * This method selects the units associated with the item in the dropdown menu.
   * Throws a GenericServiceException if the item is null.
   */
  @FXML
  private void setUnitSelectionInDropdown() throws GenericServiceException {
    // change request #86 unselect the dropdown menu items when the textfield is empty
    if (enteredItemNameNotKnownEntry()) {
      uncheckMenuItems(this.checkMenuItems);
    } else {
      ItemDTO itemNameUserInput;
      try {
        itemNameUserInput = itemController.findByName(this.curSelected);
      } catch (GenericServiceException g) {
        uncheckMenuItems(this.checkMenuItems);
        return;
      }
      List<String> associationUnits =
        this.associationItemUnitController.getItemUnits(itemNameUserInput)
          .stream()
          .map(UnitDTO::getUnitName)
          .collect(Collectors.toList());
      this.checkMenuItems.forEach(
          checkMenuItem -> checkMenuItem.setSelected(false)
        );
      for (CheckMenuItem unit : this.checkMenuItems) {
        if (associationUnits.contains(unit.getText())) {
          unit.setSelected(true);
        }
      }
    }
  }

  /*
    Checks if the adding fields entry for an item name is a known entry in the database.
     */
  private boolean enteredItemNameNotKnownEntry() {
    return itemController
      .items()
      .stream()
      .noneMatch(
        itemDTO ->
          itemDTO.getItemName().equalsIgnoreCase(this.addingField.getText())
      );
  }

  /*
    Sets all menu item selections of the observable to false
     */
  private static void uncheckMenuItems(
    ObservableList<CheckMenuItem> checkMenuItems
  ) {
    checkMenuItems.forEach(checkMenuItem -> checkMenuItem.setSelected(false));
  }

  /**
   * This method deletes an item from the database and updates the listview.
   * Throws a GenericServiceException if the item is null.
   */
  @FXML
  public void deleteEntry() throws GenericServiceException {
    try {
      this.associationItemUnitController.deleteItem(
          itemController.findByName(this.curSelected)
        );
      this.alignListOfItemDTOWithDB();
      this.table.requestFocus();
      this.table.getSelectionModel().selectFirst();
      populateAddingFieldWithItemSelection();
    } catch (GenericServiceException g) {
      ViewControllerInterface.showUserAlert(
        "No Item Selected",
        "Please select an item from the listview to delete it."
      );
    }

    this.listViewController.alignGroceryListsWithDB();
  }

  /**
   * This method modifies the units associated with the item in the database and updates the listview. Connection
   * to backend.
   * Throws a GenericServiceException if the item is null.
   */
  private void modifyItemUnits() throws GenericServiceException {
    try {
      List<String> selectedUnitStrings =
        this.checkMenuItems.stream()
          .filter(CheckMenuItem::isSelected)
          .map(MenuItem::getText)
          .collect(Collectors.toList());
      if (selectedUnitStrings.isEmpty()) {
        ViewControllerInterface.showUserAlert(
          "Unit Not Selected",
          "Please select at least one unit"
        );
        return;
      }
      List<UnitDTO> selectedUnits = selectedUnitStrings
        .stream()
        .map(
          unitName -> {
            try {
              return this.unitController.findByName(unitName);
            } catch (GenericServiceException e) {
              throw new RuntimeException(e);
            }
          }
        )
        .collect(Collectors.toList());
      this.associationItemUnitController.updateUnits(
          itemController.findByName(this.curSelected),
          selectedUnits
        );
    } catch (GenericServiceException g) {
      //alert user
    }
  }

  /**
   * This method modifies the name of the item in the database and updates the listview. Connection to backend.
   * Throws a GenericServiceException if the item is null.
   */
  private void modifyItemName() throws GenericServiceException {
    if (checkIfItemNameExistsInDB(this.addingField.getText(), true)) { //is used to prevent a user adding several times the
      // same item to the database. The suppressAlert is set to true, so that the user can only modify the
      // items units. The modification of the name will be stopped.
      return;
    }
    try {
      System.out.println("Name: " + this.curSelected);
      this.itemController.updateItemName(
          itemController.findByName(this.curSelected),
          this.addingField.getText()
        );
    } catch (GenericServiceException g) {
      ViewControllerInterface.showUserAlert(
        "Item Not Found",
        "Please Try Again"
      );
    }
  }

  private void modifyItemCategory() throws GenericServiceException {
    try {
      System.out.println("Category:" + this.curSelected);
      this.itemController.updateItemCategory(
          itemController.findByName(this.curSelected),
          this.selectCategory.getValue()
        );
    } catch (GenericServiceException g) {
      ViewControllerInterface.showUserAlert(
        "Item Not Found",
        "Please Try Again"
      );
    }
  }

  /**
   * This method modifies the item in the database and updates the listview.
   * Throws a GenericServiceException if the item is null.
   */
  @FXML
  public void modifyEntry() throws GenericServiceException {
    if (curSelected == null) {
      ViewControllerInterface.showUserAlert(
        "No Item Selected",
        "Please select an item from the listview."
      );
      return;
    }
    // Unit and Category is modified first. Once item name is changed the item is not found anymore.
    modifyItemUnits();
    modifyItemCategory();
    modifyItemName();
    this.listViewController.alignGroceryListsWithDB(); // necessary to keep grocery catalogue and grocery list in
    // sync
    //populateItemDropDownMenu();
    this.alignListOfItemDTOWithDB();
    this.table.requestFocus();
    populateAddingFieldWithItemSelection();
  }

  /**
   * This method populates the textField with the selected item in the listview.
   * Throws a GenericServiceException if the item is null.
   */
  private void populateAddingFieldWithItemSelection()
    throws GenericServiceException {
    if (this.table.getSelectionModel().getSelectedItem() != null) {
      this.curSelected =
        this.table.getSelectionModel().getSelectedItem().getItemName();
      this.addingField.setText(this.curSelected);
      this.setUnitSelectionInDropdown();
    }
  }

  /**
   * This method populates the comboBox with the selected item in the listview.
   * If the itemname from the associatied textfield does not match any DB entry the combobox is cleared.
   * Throws a GenericServiceException if the item is null.
   */
  private void populateComboBoxWithSelectedItemCategory()
    throws GenericServiceException {
    if (enteredItemNameNotKnownEntry()) {
      this.selectCategory.getSelectionModel().clearSelection();
    } else if (this.table.getSelectionModel().getSelectedItem() != null) {
      String category = itemController
        .findByName(this.curSelected)
        .getItemCategory();
      this.selectCategory.setValue(category);
    }
  }

  @FXML
  private void populateAddingFieldsWithTableSelection()
    throws GenericServiceException {
    try {
      populateAddingFieldWithItemSelection();
      populateComboBoxWithSelectedItemCategory();
    } catch (GenericServiceException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method updates the Observable list of strings from the Observable list of itemDTOs.
   * Bindings.bindContentBidirectional(this.listOfItems, this.stringItems) is not used because it is rather unstable and difficult to handle.
   */
  public void alignListOfItemDTOWithDB() {
    int lastSelected = table.getSelectionModel().getSelectedIndex();
    endSearch.setVisible(false);
    this.listOfItems.setAll(itemController.items());
    listOfCatalogueTable.clear();
    listOfCatalogueTable.addAll(itemController.items());
    table.setItems(listOfCatalogueTable);
    table.refresh();
    table.getSelectionModel().select(lastSelected);
  }

  /**
   * Checks if the name of the item is already existing in the backend. If so user is alerted and false is
   * returned. Otherwise, true is returned.
   */
  private boolean checkIfItemNameExistsInDB(
    String enteredName,
    boolean suppressAlert
  ) {
    this.listOfItems =
      FXCollections.observableArrayList(itemController.items());
    List<ItemDTO> existingItems = listOfItems
      .stream()
      .filter(itemDTO -> itemDTO.getItemName().equalsIgnoreCase(enteredName))
      .collect(Collectors.toList());
    if (!(existingItems.isEmpty())) {
      if (!suppressAlert) {
        ViewControllerInterface.showUserAlert(
          "Item Exists in Database",
          String.format(
            "\"%s\" does already exist in the catalogue. You " +
            "cannot add the same element more than once to the catalogue.",
            enteredName.toUpperCase()
          )
        );
      }
      return true;
    }
    return false;
  }

  /**
   * add all available items to the item dropdown in AddToLIst Box
   */
  public void populateItemDropDownMenu() {
    this.selectItem.getItems().clear();
    List<ItemDTO> itemDTOS = itemController.items();
    for (ItemDTO item : itemDTOS) {
      try {
        this.selectItem.getItems().add(item.getItemName());
        selectItem.setOnAction(
          event -> {
            String selectedItem = selectItem.getValue();
            this.selectedItemFromSelectItemBox = selectedItem;
            try {
              populateItemUnitDropDownMenu(selectedItem);
            } catch (GenericServiceException e) {
              throw new RuntimeException(e);
            }
          }
        );
      } catch (NullPointerException e) {
        Logger
          .getLogger("populateUnitDropDownMenu")
          .info("UnitDTO is null. Unit is not added to the list.");
      }
    }
  }

  /**
   * Add all available units of the selected item to the dropdown in AddToList Box
   *
   * @param itemName Name of selected item
   */
  public void populateItemUnitDropDownMenu(String itemName)
    throws GenericServiceException {
    if (itemName == null) {
      selectUnit.setDisable(true);
      return;
    }
    this.selectUnit.setDisable(false);
    this.selectUnit.getItems().clear();
    List<UnitDTO> listOfItemUnits = associationItemUnitController.getItemUnits(
      itemController.findByName(itemName)
    );
    for (UnitDTO unit : listOfItemUnits) {
      try {
        this.selectUnit.getItems().add(unit.getUnitName());
        selectUnit.setOnAction(
          event ->
            this.selectedItemUnitFromSelectUnitBox = selectUnit.getValue()
        );
      } catch (NullPointerException e) {
        Logger
          .getLogger("populateUnitDropDownMenu")
          .info("UnitDTO is null. Unit is not added to the list.");
      }
    }
  }

  /**
   * Create a list of all items(As strings) provided by @ItemController.searchItemByName
   * That can then be displayed on the screen.
   * This method pass a String text provided by the TextField @searchingBar
   * to @ItemController.searchItemByName and get a List of item DTOs.
   * The provided String text must contain the name or at least part of the name of
   * the item to be searched for.
   * If the user make any action on search results, the action will be performed normally
   * and the view changes to the whole catalogue again.
   *
   * @see ItemController
   */
  @FXML
  private void searchInCatalogue() {
    String nameToSearch;
    //clear all involved lists to guarantee that no conflict occurs.
    listOfSearchResult.clear();
    //Check the input before use it
    if (searchingBar.getText().isEmpty()) {
      ViewControllerInterface.showUserAlert(
        "Invalid Entry",
        "Please enter a keyword"
      );
      return;
    } else {
      nameToSearch = searchingBar.getText();
    }

    //Try to the method to search for the item with the provided name
    //If the backend return a null value of throw an exception a notification will be displayed
    try {
      List<ItemDTO> searchResult = itemController.searchByName(nameToSearch);
      if (searchResult == null) {
        ViewControllerInterface.showUserAlert(
          "Invalid Entry",
          "No match! Please try with another keyword"
        );
      }
      setListOfSearchResult(FXCollections.observableArrayList(searchResult));
    } catch (GenericServiceException e) {
      ViewControllerInterface.showUserAlert(
        "Invalid Entry",
        "No match! Please try with another keyword"
      );
    }
    setListOfSearchResult(sort(getListOfSearchResult(), nameToSearch));
    table.setItems(listOfSearchResult);
    table.requestFocus();
    table.getSelectionModel().selectFirst();
    endSearch.setVisible(true);
  }

  private ObservableList<ItemDTO> getListOfSearchResult() {
    return listOfSearchResult;
  }

  private void setListOfSearchResult(
    ObservableList<ItemDTO> listOfSearchResult
  ) {
    this.listOfSearchResult = listOfSearchResult;
  }

  /**
   * Takes a list of ItemDTO and sort it based on a given String Keyword the ItemDTOs names.
   * We split the list in 4 categories. Each category will be sorted internally
   * in ascending order based on the length of each item's name.
   * The first one contains the items that their name equals to the keyword. It will be the first list added to the returned list.
   * The second contains the items that their name starts with the keyword. It will be the second list added to the returned list.
   * The third contains the items that their name ends with the keyword. It will be the third list added to the returned list.
   * The forth contains other items. It will be the last added to the returned list.
   *
   * @param listOfSearchResult list to be sorted
   * @param nameToSearch       keyword to sort based on
   *
   * @return a sorted list
   */
  private ObservableList<ItemDTO> sort(
    ObservableList<ItemDTO> listOfSearchResult,
    String nameToSearch
  ) {
    //set a list for each category
    ObservableList<ItemDTO> sortedList = FXCollections.observableArrayList();
    ObservableList<ItemDTO> listOfItemsStartsWithKeyword = FXCollections.observableArrayList();
    ObservableList<ItemDTO> listOfOtherItems = FXCollections.observableArrayList();
    ObservableList<ItemDTO> listOfItemsEndsWithKeyword = FXCollections.observableArrayList();

    //Sorting
    for (ItemDTO itemDTO : listOfSearchResult) {
      //the results that match the keyword are the first
      if (itemDTO.getItemName().equalsIgnoreCase(nameToSearch)) {
        sortedList.add(itemDTO);
        //the name that doesn't match the keyword but starts with it will be in the 2. place
      } else if (
        itemDTO
          .getItemName()
          .toLowerCase()
          .startsWith(nameToSearch.toLowerCase())
      ) {
        listOfItemsStartsWithKeyword.add(itemDTO);
        //the name that doesn't match the keyword but ends with it will be in the 3. place
      } else if (
        itemDTO.getItemName().toLowerCase().endsWith(nameToSearch.toLowerCase())
      ) {
        listOfItemsEndsWithKeyword.add(itemDTO);
        //any other search result will be in the end.
      } else {
        listOfOtherItems.add(itemDTO);
      }
    }
    //sort the list of each category in ascending order(based on the length of each item's name)
    listOfItemsStartsWithKeyword.sort(
      Comparator.comparingInt(itemDTO -> itemDTO.getItemName().length())
    );
    listOfOtherItems.sort(
      Comparator.comparingInt(itemDTO -> itemDTO.getItemName().length())
    );
    listOfItemsEndsWithKeyword.sort(
      Comparator.comparingInt(itemDTO -> itemDTO.getItemName().length())
    );
    //merge all the list in a final one
    sortedList.addAll(listOfItemsStartsWithKeyword);
    sortedList.addAll(listOfItemsEndsWithKeyword);
    sortedList.addAll(listOfOtherItems);

    return sortedList;
  }

  /**
   * Switch from the list shown after each search to the complete Catalogue
   */
  @FXML
  private void switchFromSearchResultToCatalogue() {
    //Call the initial method to pull everything from the DB
    alignListOfItemDTOWithDB();
  }

  /**
   * Adds an Item from Grocery catalogue to Grocery list.
   * The user specify the item, one unit, and the quantity.
   * The item will be added to last selected list.
   */

  @FXML
  private void addToGroceryList() throws GenericServiceException {
    selectedGroceryList = listViewController.getCurrentSelectedGroceryList();
    if (
      selectedItemFromSelectItemBox != null &&
      selectedItemUnitFromSelectUnitBox != null &&
      selectedGroceryList != null
    ) {
      ItemDTO itemDTO = itemController.findByName(
        selectedItemFromSelectItemBox
      );
      UnitDTO unitDTO = unitController.findByName(
        selectedItemUnitFromSelectUnitBox
      );
      AssociationItemUnitDTO associationItemUnitDTO = associationItemUnitController.findByItemAndUnit(
        itemDTO,
        unitDTO
      );

      GroceryListDTO groceryListDTO = groceryListController.findByName(
        selectedGroceryList.getText()
      );

      try {
        //Create new association in backend with the list of last selected tab and the given item/unit
        associationGroceryListController.addItemAndUnitToGroceryList(
          associationItemUnitDTO,
          groceryListDTO,
          this.quantity
        );
        listViewController.suggestItem();
      } catch (NumberFormatException e) {
        ViewControllerInterface.showUserAlert(
          "Invalid Quantity",
          "Please enter Quantity(As number)"
        );
      } catch (GenericServiceException e) {
        //suggest to increase the quantity
        quantityIncrementNotification(
          this.quantity,
          associationGroceryListController.findByItemUnitAndGroceryList(
            associationItemUnitDTO,
            groceryListDTO
          )
        );
      }
      //update the last selected tab to display the new added item
      this.listViewController.alignCurrentTab();
    } else {
      if (selectedGroceryList == null) {
        ViewControllerInterface.showUserAlert(
          "Invalid Operation",
          "Please add a list first"
        );
      } else {
        ViewControllerInterface.showUserAlert(
          "Invalid Operation",
          "Please select an Item, a Unit and a Quantity"
        );
      }
    }
  }

  // TODO: write docstring
  public void quantityIncrementNotification(
    int enteredQuantity,
    AssociationGroceryListDTO associationGroceryListDTO
  ) {
    Alert alert = new Alert(Alert.AlertType.NONE);
    alert.setHeaderText(null);
    alert.setTitle("Item exists in this list");
    alert.setContentText(
      "Do You want to increase the quantity by " + enteredQuantity + " ?"
    );

    ButtonType increaseQuantity = new ButtonType(
      "Increase by " + enteredQuantity
    );
    ButtonType Close = new ButtonType("Close");

    alert.getButtonTypes().setAll(List.of(increaseQuantity, Close));
    // Listen for the Alert to close and get the result
    alert.setOnCloseRequest(
      e -> {
        ButtonType result = alert.getResult();
        if (result != null && result == increaseQuantity) {
          if (associationGroceryListDTO.isPurchased()) {
            alert.close();
            ViewControllerInterface.showUserAlert(
              "Invalid Operation",
              "You can not increase the quantity of a purchased item"
            );
            return;
          }
          try {
            associationGroceryListController.updateItemAndUnitQuantity(
              associationGroceryListDTO,
              associationGroceryListDTO.getQuantity() + enteredQuantity
            );
            this.listViewController.alignCurrentTab();
          } catch (GenericServiceException ex) {
            System.out.println("Item not found");
          } finally {
            alert.close();
          }
        } else {
          alert.close();
        }
      }
    );

    alert.show();
  }

  private void populateCategoriestoFilter() {
    try {
      // Use the same list of categories from Christian
      ObservableList<String> categoryList = FXCollections.observableArrayList(
        "Beverage",
        "Bread",
        "Dairy",
        "Fruit",
        "Meat",
        "Vegetables",
        "Condiments & Spices",
        "Snacks",
        "Fish & Seafood",
        "Frozen Food",
        "Canned Food",
        "Other"
      );
      selectCategorytoFilter.setItems(categoryList);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method retrieves a list of items that belong to the given category.
   * The returned list is represented as a collection of Items-objects.
   */
  @FXML
  public void findByCategorytoFilter() {
    try {
      String selectedCategory = selectCategorytoFilter.getValue();

      if (selectedCategory != null && !selectedCategory.isEmpty()) {
        List<ItemDTO> listOfSearchedItems = itemController.findByCategory(
          selectedCategory
        );

        setListOfSearchResult(
          FXCollections.observableArrayList(listOfSearchedItems)
        );
        setListOfSearchResult(sort(getListOfSearchResult(), selectedCategory));
        table.setItems(getListOfSearchResult());
        table.requestFocus();
        table.getSelectionModel().selectFirst();
        endSearch.setVisible(true);
      } else {
        ViewControllerInterface.showUserAlert(
          "Invalid Category",
          "Please select a valid category to perform the search."
        );
      }
    } catch (GenericServiceException e) {
      ViewControllerInterface.showUserAlert(
        "Invalid Entry",
        "No match! Please try with another category."
      );
    }
  }
}
