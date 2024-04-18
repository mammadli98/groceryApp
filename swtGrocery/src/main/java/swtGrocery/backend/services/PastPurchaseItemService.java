package swtGrocery.backend.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.PastPurchaseItem;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.repositories.PastPurchaseItemRepository;
import swtGrocery.backend.repositories.UnitRepository;
import swtGrocery.backend.services.Alghorithms.AprioriAlgorithm;
import swtGrocery.backend.services.exceptions.GenericServiceException;
import swtGrocery.javafx.controller.ViewControllerInterface;

@Service
public class PastPurchaseItemService {

  @Autowired
  private PastPurchaseItemRepository pastPurchaseItemRepository;

  @Autowired
  private UnitRepository unitRepository;

  @Autowired
  private AssociationItemUnitService associationItemUnitService;

  @Autowired
  private ItemService itemService;

  private List<String> unitList;
  private List<PastPurchaseItem> purchases;
  private List<String> columnNames = Arrays.asList(
    "name",
    "date",
    "quantity",
    "unit",
    "category"
  );

  public List<String> getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(String... columnNames) {
    this.columnNames = Arrays.asList(columnNames);
  }

  // this is used to determine the os when initializing and setting the correct split variable
  private String csvSplit;

  // determine OS and set the correct split variable
  private String os = System.getProperty("os.name").toLowerCase();

  private void setCSVSplit() {
    if (os.contains("win")) {
      csvSplit = ";";
    } else {
      csvSplit = ";";
    }
  }

  private String getCsvSplit() {
    return csvSplit;
  }

  /**
   * Retrieves a list of all past purchase items from the database.
   *
   * @return a list of all past purchase items objects
   */
  public List<PastPurchaseItem> pastPurchaseItems() {
    return pastPurchaseItemRepository.findAll();
  }

  /**
   * Creates a new past purchase item with the given name and unit and quantity and retrieves machine local date
   * then persists it in the database.
   *
   * @param itemName     the name of the new past purchase item
   * @param itemUnit     the unit of the new past purchase item
   * @param itemQuantity the quantity of the new past purchase item
   *
   * @return the created and persisted PastPurchaseItem object
   */
  public PastPurchaseItem create(
    String itemName,
    String itemUnit,
    int itemQuantity,
    String itemCategory
  )
    throws GenericServiceException {
    if (itemName.isEmpty() || itemName.trim().isEmpty()) {
      throw new GenericServiceException("Item Name is empty.");
    }
    if (itemUnit.isEmpty() || itemUnit.trim().isEmpty()) {
      throw new GenericServiceException("Item Unit is empty.");
    }
    if (itemQuantity <= 0) {
      throw new GenericServiceException("Quantity cannot be negative.");
    }
    if (itemCategory.isEmpty() || itemCategory.trim().isEmpty()) {
      throw new GenericServiceException("Category cannot be empty");
    }
    return createWithDate(
      itemName,
      itemUnit,
      itemQuantity,
      itemCategory,
      LocalDate.now()
    );
  }

  /**
   * Creates a new past purchase item with the given name, unit, quantity, and purchase date, and persists it in the database.
   *
   * @param itemName     the name of the new past purchase item
   * @param itemUnit     the unit of the new past purchase item
   * @param itemQuantity the quantity of the new past purchase item
   * @param date         the purchase date of the new past purchase item
   *
   * @return the created and persisted PastPurchaseItem object
   *
   * @throws GenericServiceException if there is an issue creating the item
   */
  public PastPurchaseItem createWithDate(
    String itemName,
    String itemUnit,
    int itemQuantity,
    String itemCategory,
    LocalDate date
  )
    throws GenericServiceException {
    try {
      PastPurchaseItem pastPurchaseItem = new PastPurchaseItem();
      pastPurchaseItem.setItemName(itemName);
      pastPurchaseItem.setItemUnit(itemUnit);
      pastPurchaseItem.setItemQuantity(itemQuantity);
      pastPurchaseItem.setItemPurchaseDate(date);
      pastPurchaseItem.setCategory(itemCategory);
      return pastPurchaseItemRepository.save(pastPurchaseItem);
    } catch (NullPointerException e) {
      throw new GenericServiceException(
        "NullPointerException occurred in ItemService create method."
      );
    }
  }

  /**
   * Deletes the specified PastPurchaseItem from the database.
   *
   * @param pastPurchaseItem the Item to be deleted
   */
  public void delete(PastPurchaseItem pastPurchaseItem)
    throws GenericServiceException {
    if (pastPurchaseItem == null) {
      throw new GenericServiceException(
        "The PastPurchaseItem argument passed is null."
      );
    }
    pastPurchaseItemRepository.delete(pastPurchaseItem);
  }

  /**
   * Retrieves an PastPurchaseItem from the database by its unique identifier (ID).
   *
   * @param itemId the unique identifier of the PastPurchaseItem to retrieve
   *
   * @return the retrieved PastPurchaseItem
   *
   * @throws GenericServiceException if no PastPurchaseItem is found in DB with the provided ID
   */
  public PastPurchaseItem findById(Long itemId) throws GenericServiceException {
    Optional<PastPurchaseItem> pastPurchaseItems = pastPurchaseItemRepository.findById(
      itemId
    );
    if (pastPurchaseItems.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for PastPurchaseItem with ID: \"" + itemId + "\""
      );
    }
    return pastPurchaseItems.get();
  }

  /**
   * Imports a list of past purchase items from a CSV file and adds them to the database.
   *
   * @param pathToList the path to the CSV file containing past purchase items
   */
  public void importPastPurchaseList(Path pathToList) {
    this.unitList = new ArrayList<>();
    unitRepository.findAll().forEach(unit -> unitList.add(unit.getUnitName()));

    String csvFile = pathToList.toString();
    setCSVSplit();
    String csvSplitBy = this.csvSplit;

    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      // Read the first line (header)
      String headerLine = br.readLine();

      // Check if the header line is valid
      if (isValidHeader(headerLine)) {
        pastPurchaseItemRepository.deleteAll();
        List<Object> invalidLines = new ArrayList<>();

        int lineNumber = 2;
        // Process the rest of the lines
        String line;
        while ((line = br.readLine()) != null) {
          String[] data = line.split(csvSplitBy);
          Map<String, String> columnMap = new HashMap<>();

          // Populate the map with column names and their corresponding values
          for (
            int i = 0;
            i < Math.min(this.columnNames.size(), data.length);
            i++
          ) {
            columnMap.put(this.columnNames.get(i), data[i]);
          }

          String validationMessage = validateLine(columnMap);
          if (validationMessage.equals("Valid")) {
            String name = Utilities.capitalizeFirstLetter(
              columnMap.get("name")
            );
            String unit = columnMap.get("unit");
            int quantity = Integer.parseInt(columnMap.get("quantity"));
            String category = Utilities.capitalizeFirstLetter(
              columnMap.get("category")
            );
            LocalDate date = LocalDate.parse(columnMap.get("date"));

            associationItemUnitService.create(
              name,
              Collections.singletonList(unit),
              category
            ); //TODO: Change category to be dynamic
            createWithDate(name, unit, quantity, category, date);
          } else {
            invalidLines.add("Line " + lineNumber + ": " + validationMessage);
          }
          lineNumber++;
        }
        if (!invalidLines.isEmpty()) {
          StringBuilder stringBuilder = new StringBuilder();

          // Append each element to the StringBuilder with a newline character
          for (Object invalidLine : invalidLines) {
            stringBuilder.append(invalidLine).append("\n");
          }

          // Convert StringBuilder to String
          String result = stringBuilder.toString();
          ViewControllerInterface.showUserAlert(
            "Given lines are Invalid",
            result
          );
        }
      } else {
        ViewControllerInterface.showUserAlert(
          "Invalid header line",
          "Make sure the headers are in the correct order."
        );
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (GenericServiceException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Checks if the header line is valid.
   *
   * @param headerLine the header to be checked
   */
  private boolean isValidHeader(String headerLine) {
    StringBuilder expectedHeader = new StringBuilder();
    for (String header : this.columnNames) {
      expectedHeader.append(header + this.csvSplit);
    }
    String fileHeader = expectedHeader.toString();
    return headerLine.equals(fileHeader);
  }

  /**
   * Checks if the given line is valid.
   *
   * @param columnMap the line(as Map)to be checked
   */
  private String validateLine(Map<String, String> columnMap) {
    // Check if the map has the expected keys
    if (
      columnMap
        .keySet()
        .containsAll(
          Arrays.asList("name", "date", "quantity", "unit", "category")
        )
    ) {
      // Validate the line
      if (!isValidDate(columnMap.get("date"))) {
        return "Invalid date format at column 'date'";
      } else if (!isNumeric(columnMap.get("quantity"))) {
        return "Quantity is not a valid number at column 'quantity'";
      } else if (!isValidUnit(columnMap.get("unit"))) {
        return "Invalid unit at column 'unit'";
      } else {
        // Line is valid
        return "Valid";
      }
    } else {
      // Line is incomplete
      return "Incomplete line. Expected columns: 'name', 'date', 'quantity', 'unit', 'category'";
    }
  }

  /**
   * Checks if the given String is a number
   *
   * @param str the string to be checked
   */
  private boolean isNumeric(String str) {
    try {
      Double.parseDouble(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Checks if the given string is a date.
   *
   * @param date the string to be checked
   */
  private boolean isValidDate(String date) {
    try {
      LocalDate.parse(date);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  /**
   * Checks if the given string is a valid unit: Match one of the predefined units
   *
   * @param unit the string to be checked
   */
  private boolean isValidUnit(String unit) {
    return this.unitList.contains(unit);
  }

  public List<PastPurchaseItem> searchByName(String searchedName)
    throws GenericServiceException {
    List<PastPurchaseItem> searchedItem = pastPurchaseItemRepository
      .findAll()
      .stream()
      .filter(item -> item.getItemName().toLowerCase().contains(searchedName))
      .collect(Collectors.toList());

    if (searchedItem.isEmpty()) {
      throw new GenericServiceException(
        "No result found with: \"" + searchedName + "\""
      );
    }

    runAprioriAlgorithm();
    return searchedItem;
  }

  public List<PastPurchaseItem> searchByDate(
    LocalDate startDate,
    LocalDate endDate
  )
    throws GenericServiceException {
    List<PastPurchaseItem> searchedItem = pastPurchaseItemRepository
      .findAll()
      .stream()
      .filter(
        item ->
          item.getItemPurchaseDate().isAfter(startDate.minusDays(1)) &&
          item.getItemPurchaseDate().isBefore(endDate.plusDays(1))
      )
      .collect(Collectors.toList());

    if (searchedItem.isEmpty()) {
      throw new GenericServiceException(
        "No result found with: \"" + startDate + endDate + "\""
      );
    }

    return searchedItem;
  }

  public List<PastPurchaseItem> searchByNameAndDate(
    String searchedName,
    LocalDate startDate,
    LocalDate endDate
  )
    throws GenericServiceException {
    List<PastPurchaseItem> searchedItem = pastPurchaseItemRepository
      .findAll()
      .stream()
      .filter(
        item ->
          item.getItemPurchaseDate().isAfter(startDate.minusDays(1)) &&
          item.getItemPurchaseDate().isBefore(endDate.plusDays(1)) &&
          item.getItemName().toLowerCase().contains(searchedName)
      )
      .collect(Collectors.toList());

    if (searchedItem.isEmpty()) {
      throw new GenericServiceException(
        "No result found with: \"" + startDate + endDate + "\""
      );
    }

    return searchedItem;
  }

  public Map<Object, Object> runAprioriAlgorithm() {
    AprioriAlgorithm aprioriAlghorithm = new AprioriAlgorithm();
    return aprioriAlghorithm.runAprioriAlgorithm(
      pastPurchaseItemRepository.findAll()
    );
  }

  public Set<String> suggestItem(
    Map<Object, Object> aprioriLibrary,
    List<String> items
  ) {
    AprioriAlgorithm aprioriAlghorithm = new AprioriAlgorithm();
    return aprioriAlghorithm.createAssociationRules(aprioriLibrary, items);
  }

  /**
   * Generates a list of recommendations. it uses the purchases saved in the database
   * to calculate frequency (in day) of each purchase and suggest items based on this frequency and current date.
   * Same items with different units are handled like two totally different items in this method
   * If an item has a frequency of 7 days, suggestions of this item span from day 7 - minSuggestingDays
   * to day 7 + maxSuggestingDays.
   * @return List of string recommendations in format "itemName---itemUnit---itemQuantity"
   */
  public List<String> generateRecommendations() {
    //Number of weeks that the algorithm considers, starting from the last purchase date.
    //Purchases older than this time frame are not considered.
    int timeFRAME = 26;
    //Minimum number of times an item must be purchased to be considered in suggestions.
    //e.g. if set to 2, items purchased less than 2 times are excluded.
    int minOCCURRENCES = 3;
    //Minimum number of days before an item's purchase frequency to provide suggestions.
    int minSuggestingDays = 1;
    //Maximum number of days after an item's purchase frequency to provide suggestions.
    int maxSuggestingDays = 3;

    List<String> recommendations = new ArrayList<>();
    //Contains frequency of purchasing each item
    Map<String, Integer> itemFrequency = new HashMap<>();
    //Contains the total quantity of each item
    Map<String, Integer> itemQuantity = new HashMap<>();

    purchases = pastPurchaseItemRepository.findAll();
    LocalDate lastInteractionDate = getLastPurchaseDate();

    if (purchases == null) {
      return recommendations;
    }
    for (PastPurchaseItem purchase : purchases) {
      if (
        checkIfPastPurchaseExist(purchase.getItemName(), purchase.getItemUnit())
      ) {
        //If the item is purchased within the timeframe it will be added to the maps
        if (
          purchase
            .getItemPurchaseDate()
            .isAfter(lastInteractionDate.minusWeeks(timeFRAME))
        ) {
          itemFrequency.put(
            String.join("---", purchase.getItemName(), purchase.getItemUnit()),
            //if the key exists already in the map, increase the value by 1
            itemFrequency.getOrDefault(
              String.join(
                "---",
                purchase.getItemName(),
                purchase.getItemUnit()
              ),
              0
            ) +
            1
          );
          itemQuantity.put(
            String.join("---", purchase.getItemName(), purchase.getItemUnit()),
            //if the key exists already in the map, increase the old value by the new quantity
            itemQuantity.getOrDefault(
              String.join(
                "---",
                purchase.getItemName(),
                purchase.getItemUnit()
              ),
              0
            ) +
            purchase.getItemQuantity()
          );
        }
      }
    }

    for (Map.Entry<String, Integer> entry : itemFrequency.entrySet()) {
      //Check if the item is purchased enough to be considered by the algorithm
      if (entry.getValue() >= minOCCURRENCES) {
        Set<LocalDate> set = new HashSet<>();
        int frequency;
        //Create a set for each item with all its purchase dates
        //Set are used to ignore duplicates
        for (PastPurchaseItem purchase : purchases) {
          if (
            String
              .join("---", purchase.getItemName(), purchase.getItemUnit())
              .equals(entry.getKey())
          ) {
            set.add(purchase.getItemPurchaseDate());
          }
        }
        //Set the number of occurrences as the size of set
        //so that item purchased twice in the same day will be considered as 1 occurrence
        int numberOfOccurrences = set.size();
        LocalDate leastDate = Collections.max(set);
        if (leastDate != null) {
          //frequency is the difference between first and last date in the list divided by the number of occurrences
          frequency =
            (
              (int) ChronoUnit.DAYS.between(Collections.min(set), leastDate) + 1
            ) /
            (numberOfOccurrences - 1);
          if (
            ChronoUnit.DAYS.between(leastDate, lastInteractionDate) >=
            frequency -
            minSuggestingDays &&
            leastDate.compareTo(lastInteractionDate) <=
            frequency +
            maxSuggestingDays
          ) {
            //Add the item to recommendation if the difference between last interaction and last purchase date (+/-) equals the frequency
            recommendations.add(
              String.join(
                "---",
                entry.getKey(),
                //Quantity is the total quantity divided by the occurrences
                String.valueOf(
                  Math.max(
                    1,
                    itemQuantity.getOrDefault(entry.getKey(), 1) /
                    numberOfOccurrences
                  )
                )
              )
            );
          }
        }
      }
    }
    return recommendations;
  }

  /**
   * Checks if the past purchase still exists in the catalogue
   *
   * @param name     item name
   * @param unitName item unit
   *
   * @return true if the item still exist in the catalogue
   */
  private boolean checkIfPastPurchaseExist(String name, String unitName) {
    Item item;
    try {
      item = itemService.findByName(name);
    } catch (GenericServiceException e) {
      return false;
    }
    try {
      for (Unit unit : associationItemUnitService.getItemUnits(item)) {
        if (unit.getUnitName().equals(unitName)) {
          return true;
        }
      }
      return false;
    } catch (GenericServiceException e) {
      return false;
    }
  }

  /**
   * Returns the date of the last purchase Item in the database
   *
   * @return date of last purchase item
   */
  private LocalDate getLastPurchaseDate() {
    LocalDate lastPurchaseDate = null;
    if (purchases != null) {
      for (PastPurchaseItem purchase : purchases) {
        if (
          lastPurchaseDate == null ||
          purchase.getItemPurchaseDate().isAfter(lastPurchaseDate)
        ) {
          lastPurchaseDate = purchase.getItemPurchaseDate();
        }
      }
    } else {
      LocalDate.now();
    }
    return lastPurchaseDate;
  }
}
