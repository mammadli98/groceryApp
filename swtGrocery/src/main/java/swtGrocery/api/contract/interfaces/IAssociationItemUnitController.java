package swtGrocery.api.contract.interfaces;

import java.util.List;
import swtGrocery.api.contract.AssociationItemUnitDTO;
import swtGrocery.backend.services.AssociationItemUnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

public interface IAssociationItemUnitController {
  /**
   * see {@link AssociationItemUnitService#associationItemUnits()}
   */
  List<AssociationItemUnitDTO> associationItemUnits();

  void create(String item, List<String> units, String itemCategory)
    throws GenericServiceException;
}
