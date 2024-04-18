package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.PastPurchaseItemDTO;
import swtGrocery.backend.services.PastPurchaseItemService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IPastPurchaseItemController {
  /**
   * see {@link PastPurchaseItemService#pastPurchaseItems()}
   */
  List<PastPurchaseItemDTO> pastPurchaseItems();

  PastPurchaseItemDTO create(
    String itemName,
    String itemUnit,
    int itemQuantity,
    String itemCategory
  )
    throws GenericServiceException;
}
