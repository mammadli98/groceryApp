package swtGrocery.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.ItemDTO;
import swtGrocery.api.contract.UnitDTO;
import swtGrocery.api.contract.interfaces.IUnitController;
import swtGrocery.backend.services.ItemService;
import swtGrocery.backend.services.UnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class UnitController implements IUnitController {

  @Autowired
  UnitService unitService;

  @Autowired
  ModelMapper modelMapper;

  @Override
  public List<UnitDTO> units() {
    return modelMapper.unitsToUnitDTOs(unitService.units());
  }

  @Override
  public UnitDTO create(String unitName) throws GenericServiceException {
    return modelMapper.unitToUnitDTO(unitService.create(unitName));
  }

  public UnitDTO findByName(String searchedUnit)
    throws GenericServiceException {
    return modelMapper.unitToUnitDTO(unitService.findByName(searchedUnit));
  }
}
