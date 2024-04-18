package swtGrocery.api;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import swtGrocery.api.contract.*;
import swtGrocery.backend.entities.*;

/**
 * Maps entities to DTOs.
 */
@Component
public class ModelMapper {

  public ItemDTO itemToItemDTO(Item item) {
    return new ItemDTO(
      item.getId(),
      item.getItemName(),
      item.getItemQuantity(),
      item.getItemCategory()
    );
  }

  public List<ItemDTO> itemsToItemDTOs(List<Item> items) {
    return items
      .stream()
      .map(item -> itemToItemDTO(item))
      .collect(Collectors.toList());
  }

  public UnitDTO unitToUnitDTO(Unit unit) {
    return new UnitDTO(unit.getId(), unit.getUnitName());
  }

  public List<UnitDTO> unitsToUnitDTOs(List<Unit> units) {
    return units
      .stream()
      .map(unit -> unitToUnitDTO(unit))
      .collect(Collectors.toList());
  }

  public AssociationItemUnitDTO associationItemUnitToassociationItemUnitDTO(
    AssociationItemUnit associationItemUnit
  ) {
    return new AssociationItemUnitDTO(
      associationItemUnit.getId(),
      itemToItemDTO(associationItemUnit.getItem()),
      unitToUnitDTO(associationItemUnit.getUnit())
    );
  }

  public List<AssociationItemUnitDTO> associationItemUnitsToassociationItemUnitDTOs(
    List<AssociationItemUnit> associationItemUnits
  ) {
    return associationItemUnits
      .stream()
      .map(
        associationItemUnit ->
          associationItemUnitToassociationItemUnitDTO(associationItemUnit)
      )
      .collect(Collectors.toList());
  }

  public GroceryListDTO groceryListTogroceryListDTO(GroceryList groceryList) {
    return new GroceryListDTO(groceryList.getId(), groceryList.getName());
  }

  public List<GroceryListDTO> groceryListsTogroceryListDTOs(
    List<GroceryList> groceryLists
  ) {
    return groceryLists
      .stream()
      .map(groceryList -> groceryListTogroceryListDTO(groceryList))
      .collect(Collectors.toList());
  }

  public AssociationGroceryListDTO associationGroceryListToassociationGroceryListDTO(
    AssociationGroceryListItemUnit associationGroceryListItemUnit
  ) {
    return new AssociationGroceryListDTO(
      associationGroceryListItemUnit.getId(),
      associationGroceryListItemUnit.getAssociationItemUnit(),
      associationGroceryListItemUnit.getGroceryList(),
      associationGroceryListItemUnit.getQuantity(),
      associationGroceryListItemUnit.getisPurchased()
    );
  }

  public List<AssociationGroceryListDTO> associationGroceryListsToAssociationGroceryListDTOs(
    List<AssociationGroceryListItemUnit> associationGroceryLists
  ) {
    return associationGroceryLists
      .stream()
      .map(
        associationGroceryList ->
          associationGroceryListToassociationGroceryListDTO(
            associationGroceryList
          )
      )
      .collect(Collectors.toList());
  }

  public PastPurchaseItemDTO PastPurchaseItemToPastPurchaseItemDTO(
    PastPurchaseItem pastPurchaseItem
  ) {
    return new PastPurchaseItemDTO(
      pastPurchaseItem.getId(),
      pastPurchaseItem.getItemName(),
      pastPurchaseItem.getItemUnit(),
      pastPurchaseItem.getItemQuantity(),
      pastPurchaseItem.getItemPurchaseDate(),
      pastPurchaseItem.getCategory()
    );
  }

  public List<PastPurchaseItemDTO> PastPurchaseItemsToPastPurchaseItemDTOs(
    List<PastPurchaseItem> pastPurchaseItems
  ) {
    return pastPurchaseItems
      .stream()
      .map(this::PastPurchaseItemToPastPurchaseItemDTO)
      .collect(Collectors.toList());
  }
}
