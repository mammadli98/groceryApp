package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.GroceryListDTO;
import swtGrocery.api.contract.UnitDTO;
import swtGrocery.backend.services.GroceryListService;
import swtGrocery.backend.services.UnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IGroceryListController {
  /**
   * see {@link GroceryListService#groceryLists()} ()}
   */
  List<GroceryListDTO> groceryLists();
  GroceryListDTO create(String unitName) throws GenericServiceException;
}
