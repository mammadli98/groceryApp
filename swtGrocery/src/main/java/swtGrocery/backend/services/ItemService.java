package swtGrocery.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.repositories.ItemRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Service
public class ItemService {

  @Autowired
  private ItemRepository itemRepository;

  /**
   * Retrieves a list of all items from the database.
   *
   * @return a list of all Item objects
   */
  public List<Item> items() {
    return itemRepository.findAll();
  }

  /**
   * Creates a new Item with the given name and unit and persists it in the database.
   *
   * @param itemName     the name of the new item
   * @param itemCategory
   *
   * @return the created and persisted Item object
   */
  public Item create(String itemName, String itemCategory)
    throws GenericServiceException {
    if (itemName.isEmpty() || itemName.trim().isEmpty()) {
      throw new GenericServiceException("Item Name is empty.");
    }

    try {
      Item item = new Item();
      item.setName(Utilities.capitalizeFirstLetter(itemName));
      item.setCategory(Utilities.capitalizeFirstLetter(itemCategory));
      item.setItemQuantity(-1);
      return itemRepository.save(item);
    } catch (IllegalArgumentException illegal) { //TODO: null cannot be passed as new item is created - no need
      // to catch exception
      System.out.println(
        illegal +
        ": " +
        " showed up in ItemService create method. " +
        "Default Item Apples, kg will be created"
      );
      Item item = new Item(); //TODO: are we still creating a default item when creation didnt work? - Refactor
      item.setName("Apples");
      item.setItemQuantity(-1);
      return itemRepository.save(item);
    }
  }

  /**
   * Check if item is not exist in DB
   *
   * @param itemName the name of the item to check
   *
   * @return true if item not exist in DB
   */
  public boolean checkIfItemNotExist(String itemName) {
    String modifiedItemName = Utilities.capitalizeFirstLetter(itemName);
    return itemRepository
      .findAll()
      .stream()
      .noneMatch(item -> item.getItemName().equals(modifiedItemName));
  }

  /**
   * Deletes the specified Item from the database.
   *
   * @param item the Item to be deleted
   */
  public void delete(Item item) { //Tested
    itemRepository.delete(item);
  }

  /**
   * Updates the name and unit of the specified Item and saves it to the database.
   *
   * @param item     the Item to be updated
   * @param itemName the new name for the Item
   */
  public void updateItemName(Item item, String itemName)
    throws GenericServiceException {
    if (itemName.isEmpty()) {
      throw new GenericServiceException("Item name cannot be empty");
    }
    item.setName(itemName);
    itemRepository.save(item);
  }

  /**
   * Updates the category of the specified Item and saves it to the database.
   *
   * @param item         the Item to be updated
   * @param itemCategory the new category for the Item
   */
  public void updateItemCategory(Item item, String itemCategory)
    throws GenericServiceException {
    if (itemCategory.isEmpty()) {
      throw new GenericServiceException(
        "Item category is required to generate an item."
      );
    }
    item.setCategory(itemCategory);
    itemRepository.save(item);
  }

  /**
   * Retrieves an Item from the database by its unique identifier (ID).
   *
   * @param itemId the unique identifier of the Item to retrieve
   *
   * @return the retrieved Item
   *
   * @throws GenericServiceException if no Item is found with the provided ID
   */
  public Item findById(Long itemId) throws GenericServiceException {
    Optional<Item> items = itemRepository.findById(itemId);
    if (items.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Item with ID: \"" + itemId + "\""
      );
    }
    return items.get();
  }

  public Item findByName(String itemName) throws GenericServiceException {
    String modifiedItemName = Utilities.capitalizeFirstLetter(itemName);
    List<Item> items = itemRepository
      .findAll()
      .stream()
      .filter(
        item ->
          Utilities
            .capitalizeFirstLetter(item.getItemName())
            .equals(modifiedItemName)
      )
      .collect(Collectors.toList());

    if (items.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Item with name: \"" + modifiedItemName + "\""
      );
    }
    return items.get(0);
  }

  /**
   * Retrieves a list of Items from the database by its name.
   *
   * @param searchedName the name of the Item to retrieve by search
   *
   * @return the retrieved Item
   *
   * @throws GenericServiceException if no Item is found with the provided name
   */
  public List<Item> searchByName(String searchedName)
    throws GenericServiceException {
    List<Item> searchedItem = itemRepository
      .findAll()
      .stream()
      .filter(
        item ->
          item.getItemName().toLowerCase().contains(searchedName.toLowerCase())
      )
      .collect(Collectors.toList());

    if (searchedItem.isEmpty()) {
      throw new GenericServiceException(
        "No result found with: \"" + searchedName + "\""
      );
    }

    return searchedItem;
  }

  public List<Item> findByCategory(String searchCategory)
    throws GenericServiceException {
    if (searchCategory.isEmpty() || searchCategory.trim().isEmpty()) {
      throw new GenericServiceException("Category cannot be empty");
    }
    List<Item> searchedItem = itemRepository
      .findAll()
      .stream()
      .filter(
        item ->
          item
            .getItemCategory()
            .toLowerCase()
            .contains(searchCategory.toLowerCase())
      )
      .collect(Collectors.toList());
    return searchedItem;
  }
}
