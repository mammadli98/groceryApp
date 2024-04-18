package swtGrocery.api.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.AssociationItemUnitDTO;
import swtGrocery.api.contract.ItemDTO;
import swtGrocery.api.contract.UnitDTO;
import swtGrocery.api.contract.interfaces.IAssociationItemUnitController;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.services.AssociationItemUnitService;
import swtGrocery.backend.services.ItemService;
import swtGrocery.backend.services.UnitService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class AssociationItemUnitController
  implements IAssociationItemUnitController {

  @Autowired
  AssociationItemUnitService associationItemUnitService;

  @Autowired
  ModelMapper modelMapper;

  @Autowired
  ItemService itemService;

  @Autowired
  UnitService unitService;

  @Override
  public List<AssociationItemUnitDTO> associationItemUnits() {
    return modelMapper.associationItemUnitsToassociationItemUnitDTOs(
      associationItemUnitService.associationItemUnits()
    );
  }

  @Override
  public void create(String item, List<String> unit, String itemCategory)
    throws GenericServiceException {
    associationItemUnitService.create(item, unit, itemCategory);
  }

  public void deleteItem(ItemDTO itemDto) throws GenericServiceException {
    associationItemUnitService.deleteItem(
      itemService.findById(itemDto.getId())
    );
  }

  public void updateUnits(ItemDTO itemDTO, List<UnitDTO> unitsAfterDTO)
    throws GenericServiceException {
    List<Unit> unitsAfter = unitsAfterDTO
      .stream()
      .map(
        unitDTO -> {
          try {
            return unitService.findById(unitDTO.getId());
          } catch (GenericServiceException e) {
            throw new RuntimeException(e);
          }
        }
      )
      .collect(Collectors.toList());

    associationItemUnitService.updateItemUnits(
      itemService.findById(itemDTO.getId()),
      unitsAfter
    );
  }

  public void deleteItemUnit(ItemDTO itemDto, UnitDTO unitDto)
    throws GenericServiceException {
    associationItemUnitService.deleteItemUnit(
      itemService.findById(itemDto.getId()),
      unitService.findById(unitDto.getId())
    );
  }

  public List<UnitDTO> getItemUnits(ItemDTO itemDto)
    throws GenericServiceException {
    return modelMapper.unitsToUnitDTOs(
      associationItemUnitService.getItemUnits(
        itemService.findById(itemDto.getId())
      )
    );
  }

  public AssociationItemUnitDTO findByItemAndUnit(
    ItemDTO itemDTO,
    UnitDTO unitDTO
  )
    throws GenericServiceException {
    return modelMapper.associationItemUnitToassociationItemUnitDTO(
      associationItemUnitService.findByItemAndUnit(
        itemService.findById(itemDTO.getId()),
        unitService.findById(unitDTO.getId())
      )
    );
  }

  public String getItemCategory(AssociationItemUnit associationItemUnit)
    throws GenericServiceException {
    return associationItemUnitService.getItemCategory(associationItemUnit);
  }

  public void updateItemCategory(
    AssociationItemUnit associationItemUnit,
    String itemCategory
  )
    throws GenericServiceException {
    associationItemUnitService.updateItemCategory(
      associationItemUnit,
      itemCategory
    );
  }
}
