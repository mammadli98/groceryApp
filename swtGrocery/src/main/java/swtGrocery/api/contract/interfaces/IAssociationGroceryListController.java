package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.AssociationItemUnitDTO;
import swtGrocery.api.contract.GroceryListDTO;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.services.AssociationGroceryListItemUnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IAssociationGroceryListController {
  /**
   * see {@link AssociationGroceryListItemUnitService#associationGroceryLists()}
   */
  List<AssociationGroceryListItemUnit> associationGroceryLists();

  void addItemAndUnitToGroceryList(
    AssociationItemUnitDTO associationItemUnitDTO,
    GroceryListDTO groceryListDTO,
    int quantity
  )
    throws GenericServiceException;
}
