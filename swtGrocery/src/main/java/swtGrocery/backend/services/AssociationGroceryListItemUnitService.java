package swtGrocery.backend.services;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.GroceryList;
import swtGrocery.backend.repositories.AssociationGroceryListItemUnitRepository;
import swtGrocery.backend.repositories.AssociationItemUnitRepository;
import swtGrocery.backend.repositories.GroceryListRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Service
public class AssociationGroceryListItemUnitService {

  @Autowired
  public GroceryListRepository groceryListRepository;

  @Autowired
  public AssociationItemUnitRepository associationItemUnitRepository;

  @Autowired
  public AssociationGroceryListItemUnitRepository associationGroceryListItemUnitRepository;

  public List<AssociationGroceryListItemUnit> associationGroceryLists() {
    return associationGroceryListItemUnitRepository.findAll();
  }

  public void addItemAndUnitToGroceryList(
    AssociationItemUnit associationItemUnit,
    GroceryList groceryList,
    int quantity
  )
    throws GenericServiceException {
    if (
      associationGroceryListItemUnitRepository.existsByAssociationItemUnitAndGroceryList(
        associationItemUnit,
        groceryList
      )
    ) {
      throw new GenericServiceException(
        "Association already exists for the given AssociationItemUnit and GroceryList"
      );
    }

    AssociationGroceryListItemUnit associationGroceryListItemUnit = new AssociationGroceryListItemUnit();
    associationGroceryListItemUnit.setAssociationItemUnit(associationItemUnit);
    associationGroceryListItemUnit.setGroceryList(groceryList);
    associationGroceryListItemUnit.setQuantity(quantity);

    // Save the new AssociationGroceryListItemUnit
    associationGroceryListItemUnitRepository.save(
      associationGroceryListItemUnit
    );
  }

  public void deleteItemAndUnitFromGroceryList(
    AssociationGroceryListItemUnit associationGroceryListItemUnit
  ) {
    associationGroceryListItemUnitRepository.delete(
      associationGroceryListItemUnit
    );
  }

  public void updateItemAndUnitQuantity( //Tested
    AssociationGroceryListItemUnit associationGroceryListItemUnit,
    int quantity
  )
    throws GenericServiceException {
    associationGroceryListItemUnit.setQuantity(quantity);
    associationGroceryListItemUnitRepository.save(
      associationGroceryListItemUnit
    );
  }

  public AssociationGroceryListItemUnit findById(Long associationGroceryListId)
    throws GenericServiceException {
    Optional<AssociationGroceryListItemUnit> associationGroceryListItemUnit = associationGroceryListItemUnitRepository.findById(
      associationGroceryListId
    );
    if (associationGroceryListItemUnit.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Association Grocery List with ID: \"" +
        associationGroceryListId +
        "\""
      );
    }
    return associationGroceryListItemUnit.get();
  }

  public List<AssociationGroceryListItemUnit> findByGroceryList(
    GroceryList groceryList
  )
    throws GenericServiceException {
    List<AssociationGroceryListItemUnit> associationGroceryLists = associationGroceryListItemUnitRepository.findByGroceryList(
      groceryList
    );
    if (associationGroceryLists.isEmpty()) {
      throw new GenericServiceException(
        "There are not any Association Item And Units in given grocery list: " +
        groceryList
      );
    }
    return associationGroceryLists;
  }

  public AssociationGroceryListItemUnit findByItemUnitAndGroceryList(
    AssociationItemUnit associationItemUnit,
    GroceryList groceryList
  )
    throws GenericServiceException {
    AssociationGroceryListItemUnit associationGroceryListItemUnit = associationGroceryListItemUnitRepository.findByAssociationItemUnitAndGroceryList(
      associationItemUnit,
      groceryList
    );
    if (associationGroceryListItemUnit == null) {
      throw new GenericServiceException(
        "There are not any Association Item And Units in given grocery list: " +
        groceryList
      );
    }
    return associationGroceryListItemUnit;
  }

  public void setPurchased(
    AssociationGroceryListItemUnit associationGroceryListItemUnit,
    Boolean b
  ) {
    associationGroceryListItemUnit.setisPurchased(b);
    associationGroceryListItemUnitRepository.save(
      associationGroceryListItemUnit
    );
  }

  public boolean isPurchased(
    AssociationGroceryListItemUnit associationGroceryListItemUnit
  ) {
    return associationGroceryListItemUnit.getisPurchased();
  }

  /**
   * Get items from the specified grocery list sorted by category.
   *
   * @param groceryListId the unique identifier of the grocery list to retrieve items from
   * @return a list of associated items from the specified grocery list, sorted by category
   * @throws GenericServiceException if the grocery list is not found or other exceptions occur
   */
  public List<AssociationGroceryListItemUnit> getSortedItemsByCategoryForGroceryList(
    Long groceryListId
  )
    throws GenericServiceException {
    Optional<GroceryList> groceryListOpt = groceryListRepository.findById(
      groceryListId
    );
    if (!groceryListOpt.isPresent()) {
      throw new GenericServiceException(
        "Grocery list not found with ID: " + groceryListId
      );
    }

    GroceryList groceryList = groceryListOpt.get();
    List<AssociationGroceryListItemUnit> associatedItems = findByGroceryList(
      groceryList
    );

    // Assuming AssociationItemUnit has a method getItem() which returns the associated Item
    // and Item has a getCategory() method.
    return associatedItems
      .stream()
      .sorted(
        Comparator.comparing(
          assoc -> assoc.getAssociationItemUnit().getItem().getItemCategory()
        )
      )
      .collect(Collectors.toList());
  }
}
