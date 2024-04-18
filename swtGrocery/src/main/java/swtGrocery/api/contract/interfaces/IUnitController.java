package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.ItemDTO;
import swtGrocery.api.contract.UnitDTO;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.services.ItemService;
import swtGrocery.backend.services.UnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IUnitController {
  /**
   * see {@link UnitService#units()}
   */
  List<UnitDTO> units();
  UnitDTO create(String unitName) throws GenericServiceException;
}
