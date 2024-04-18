package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.ItemDTO;
import swtGrocery.backend.services.ItemService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IItemController {
  /**
   * see {@link ItemService#items()}
   */
  List<ItemDTO> items();

  ItemDTO create(String itemName, String itemCategory)
    throws GenericServiceException;
}
