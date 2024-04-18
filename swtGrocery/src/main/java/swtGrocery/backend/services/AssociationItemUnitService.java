package swtGrocery.backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.repositories.AssociationGroceryListItemUnitRepository;
import swtGrocery.backend.repositories.AssociationItemUnitRepository;
import swtGrocery.backend.repositories.ItemRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Service
public class AssociationItemUnitService {

  @Autowired
  private AssociationItemUnitRepository associationItemUnitRepository;

  @Autowired
  private AssociationGroceryListItemUnitRepository associationGroceryListItemUnitRepository;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  public ItemService itemService;

  @Autowired
  public UnitService unitService;

  public List<AssociationItemUnit> associationItemUnits() {
    return associationItemUnitRepository.findAll();
  }

  public void create(
    String itemName,
    List<String> unitNames,
    String itemCategory
  )  //Tested
    throws GenericServiceException {
    if (unitNames.isEmpty()) {
      throw new GenericServiceException(
        "\"No unit was specified. No Item was created.\""
      );
    }

    if (itemName.isEmpty() || itemName.trim().isEmpty()) {
      throw new GenericServiceException("No Item name specified.");
    }
    Item item = itemService.checkIfItemNotExist(itemName)
      ? itemService.create(
        Utilities.capitalizeFirstLetter(itemName),
        itemCategory
      )
      : itemService.findByName(itemName);

    for (String unitName : unitNames) {
      Unit unit = unitService.checkIfUnitNotExist(unitName)
        ? unitService.create(unitName)
        : unitService.findByName(unitName);

      if (checkAssociationItemUnitNotExist(item, unit)) {
        AssociationItemUnit associationItemUnit = new AssociationItemUnit();
        associationItemUnit.setItem(item);
        associationItemUnit.setUnit(unit);
        associationItemUnitRepository.save(associationItemUnit);
      }
    }
  }

  private boolean checkAssociationItemUnitNotExist(Item item, Unit unit)
    throws GenericServiceException {
    return !associationItemUnitRepository.existsByItemAndUnit(item, unit);
  }

  public List<Unit> getItemUnits(Item item) throws GenericServiceException {
    List<AssociationItemUnit> associationItemUnits = associationItemUnitRepository.findByItem(
      item
    );

    // Extract a list of Unit objects
    return associationItemUnits
      .stream()
      .map(AssociationItemUnit::getUnit) // Extract Unit from AssociationItemUnit
      .collect(Collectors.toList());
  }

  public void deleteItem(Item item) throws GenericServiceException {
    List<AssociationItemUnit> deletedItems = associationItemUnitRepository.findByItem(
      itemService.findById(item.getId())
    );

    // Delete all associated items in one go from Grocery Lists
    associationGroceryListItemUnitRepository.deleteAll(
      associationGroceryListItemUnitRepository.findByAssociationItemUnitIn(
        deletedItems
      )
    );

    // Delete all associated items in one go
    associationItemUnitRepository.deleteAll(deletedItems);

    // Delete the main item
    itemRepository.delete(itemService.findById(item.getId()));
  }

  public void deleteItemUnit(Item item, Unit unit) {
    List<AssociationGroceryListItemUnit> associationGroceryLists = associationGroceryListItemUnitRepository.findByAssociationItemUnit(
      associationItemUnitRepository.findByItemAndUnit(item, unit)
    );
    associationGroceryListItemUnitRepository.deleteAll(associationGroceryLists);
    associationItemUnitRepository.delete(
      associationItemUnitRepository.findByItemAndUnit(item, unit)
    );
  }

  public void updateItemUnits(Item item, List<Unit> unitsAfter)
    throws GenericServiceException {
    if (unitsAfter.isEmpty()) {
      throw new GenericServiceException(
        "No unit was specified. Each item needs at least one specified unit. " +
        "Not item was updated."
      );
    }
    List<Unit> unitsBefore = associationItemUnitRepository
      .findByItem(item)
      .stream()
      .map(AssociationItemUnit::getUnit)
      .collect(Collectors.toList());
    List<String> newUnits = new ArrayList<>();

    unitsBefore.removeIf(
      unitBefore ->
        unitsAfter
          .stream()
          .anyMatch(
            unitAfter ->
              unitAfter.getUnitName().equalsIgnoreCase(unitBefore.getUnitName())
          )
    );

    for (Unit unit : unitsBefore) {
      deleteItemUnit(item, unit);
    }

    for (Unit unit : unitsAfter) {
      newUnits.add(unit.getUnitName());
      create(
        Utilities.capitalizeFirstLetter(item.getItemName()),
        newUnits,
        item.getItemCategory()
      );
    }
  }

  public AssociationItemUnit findById(Long associationItemUnitId)
    throws GenericServiceException {
    Optional<AssociationItemUnit> associationItemUnit = associationItemUnitRepository.findById(
      associationItemUnitId
    );
    if (associationItemUnit.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Association Grocery List with ID: \"" +
        associationItemUnitId +
        "\""
      );
    }
    return associationItemUnit.get();
  }

  public AssociationItemUnit findByItemAndUnit(Item item, Unit unit)
    throws GenericServiceException {
    if (item == null || unit == null) {
      throw new GenericServiceException("Item or Unit is null");
    }
    AssociationItemUnit associationItemUnit = associationItemUnitRepository.findByItemAndUnit(
      item,
      unit
    );
    if (associationItemUnit == null) {
      throw new GenericServiceException(
        "There is not exist Association with given item: " +
        item +
        " and unit: " +
        unit
      );
    }
    return associationItemUnit;
  }

  public List<AssociationItemUnit> findByCategory(String itemCategory)
    throws GenericServiceException { //TODO:
    // TODO: function WIP!!! Test not passed.
    if (itemCategory.isEmpty() || itemCategory.trim().isEmpty()) {
      throw new GenericServiceException("Item category is null");
    }
    List<Item> categoryItems = itemService.findByCategory(itemCategory);
    List<AssociationItemUnit> associationItemUnits = new ArrayList<>();
    for (Item item : categoryItems) {
      associationItemUnits.add(
        findByItemAndUnit(
          item,
          associationItemUnitRepository.findByItem(item).get(0).getUnit()
        )
      );
    }
    return associationItemUnits;
  }

  public void updateItemCategory(
    AssociationItemUnit associationItemUnit,
    String itemCategory
  )
    throws GenericServiceException {
    if (itemCategory.isEmpty() || itemCategory.trim().isEmpty()) {
      throw new GenericServiceException("Item category is null");
    }
    Item item = associationItemUnit.getItem();
    itemService.updateItemCategory(item, itemCategory);
  }

  public String getItemCategory(AssociationItemUnit associationItemUnit)
    throws GenericServiceException {
    Item item = associationItemUnit.getItem();
    return item.getItemCategory();
  }
}
