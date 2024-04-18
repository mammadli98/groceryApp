package swtGrocery.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.entities.GroceryList;
import swtGrocery.backend.repositories.AssociationGroceryListItemUnitRepository;
import swtGrocery.backend.repositories.GroceryListRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Service
public class GroceryListService {

  @Autowired
  public GroceryListRepository groceryListRepository;

  @Autowired
  public AssociationGroceryListItemUnitRepository associationGroceryListItemUnitRepository;

  /**
   * Get all grocery lists from the database.
   *
   * @return a list of all grocery lists
   */
  public List<GroceryList> groceryLists() {
    return groceryListRepository.findAll();
  }

  /**
   * Create a new grocery list with the given name and persist it in the database.
   *
   * @param name the name of the new grocery list
   *
   * @return the created and persisted grocery list
   *
   * @throws GenericServiceException if the provided name is empty or already exists
   */
  public GroceryList create(String name) throws GenericServiceException {
    try {
      if (name.isEmpty() || name.trim().isEmpty()) {
        throw new GenericServiceException("List name cannot be empty");
      }

      if (!checkIfGroceryListNotExist(name)) {
        throw new GenericServiceException("List name already exists");
      }

      GroceryList groceryList = new GroceryList();
      groceryList.setName(name);
      return groceryListRepository.save(groceryList);
    } catch (NullPointerException e) { //TODO: why is there a null pointer exception here when generics are thrown?
      throw new GenericServiceException(
        "A null pointer exception occured and the grocery list could not be created.",
        e
      );
    }
  }

  /**
   * Update the name of the given grocery list and save it to the database.
   *
   * @param groceryList the grocery list to be updated
   * @param name        the new name for the grocery list
   *
   * @throws GenericServiceException if the provided name is empty
   */
  public void updateName(GroceryList groceryList, String name)
    throws GenericServiceException {
    if (name.isEmpty() || name.trim().isEmpty()) {
      throw new GenericServiceException("List name cannot be empty");
    }

    if (groceryList == null) {
      throw new GenericServiceException(
        "The GrocerList argument passed is null."
      );
    }
    groceryList.setName(name);
    groceryListRepository.save(groceryList);
  }

  /**
   * Delete the specified grocery list from the database.
   *
   * @param groceryList the grocery list to be deleted
   *
   * @throws GenericServiceException if an error occurs while deleting the grocery list
   */
  public void delete(GroceryList groceryList) throws GenericServiceException {
    if (groceryList == null) {
      throw new GenericServiceException(
        "The GrocerList argument passed is null."
      );
    }

    List<AssociationGroceryListItemUnit> associationGroceryLists = associationGroceryListItemUnitRepository.findByGroceryList(
      groceryList
    );
    associationGroceryListItemUnitRepository.deleteAll(associationGroceryLists);
    groceryListRepository.delete(groceryList);
  }

  /**
   * Find a grocery list by its unique identifier (ID).
   *
   * @param groceryListId the unique identifier of the grocery list to retrieve
   *
   * @return the retrieved grocery list
   *
   * @throws GenericServiceException if no grocery list is found with the provided ID
   */
  public GroceryList findById(Long groceryListId)
    throws GenericServiceException {
    if (groceryListId == null) {
      throw new GenericServiceException("The Long id argument passed is null.");
    }
    Optional<GroceryList> groceryList = groceryListRepository.findById(
      groceryListId
    );
    if (groceryList.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for List with ID: \"" + groceryList + "\""
      );
    }
    return groceryList.get();
  }

  /**
   * Find a grocery list by its name.
   *
   * @param listName the name of the grocery list to retrieve
   *
   * @return the retrieved grocery list
   *
   * @throws GenericServiceException if no grocery list is found with the provided name
   */
  public GroceryList findByName(String listName)
    throws GenericServiceException {
    if (listName.isEmpty() || listName.trim().isEmpty()) {
      throw new GenericServiceException(
        "The String listName argument passed is empty."
      );
    }
    List<GroceryList> groceryLists = groceryListRepository
      .findAll()
      .stream()
      .filter(groceryList -> groceryList.getName().equals(listName))
      .collect(Collectors.toList());

    if (groceryLists.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Grocery List with name: \"" + groceryLists + "\""
      );
    }
    return groceryLists.get(0);
  }

  /**
   * Check if a grocery list with the given name does not exist in the database.
   *
   * @param listName the name of the grocery list to check
   *
   * @return true if the grocery list does not exist, false otherwise
   */
  private boolean checkIfGroceryListNotExist(String listName) {
    String modifiedListName = listName;
    return groceryListRepository
      .findAll()
      .stream()
      .noneMatch(groceryList -> groceryList.getName().equals(modifiedListName));
  }
}
