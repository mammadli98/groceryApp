package swtGrocery.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.AssociationGroceryListDTO;
import swtGrocery.api.contract.AssociationItemUnitDTO;
import swtGrocery.api.contract.GroceryListDTO;
import swtGrocery.api.contract.interfaces.IAssociationGroceryListController;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.services.*;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class AssociationGroceryListController
  implements IAssociationGroceryListController {

  @Autowired
  private AssociationGroceryListItemUnitService associationGroceryListItemUnitService;

  @Autowired
  private AssociationItemUnitService associationItemUnitService;

  @Autowired
  private GroceryListService groceryListService;

  @Autowired
  ModelMapper modelMapper;

  @Override
  public List<AssociationGroceryListItemUnit> associationGroceryLists() {
    return associationGroceryListItemUnitService.associationGroceryLists();
  }

  @Override
  public void addItemAndUnitToGroceryList(
    AssociationItemUnitDTO associationItemUnitDTO,
    GroceryListDTO groceryListDTO,
    int quantity
  )
    throws GenericServiceException {
    associationGroceryListItemUnitService.addItemAndUnitToGroceryList(
      associationItemUnitService.findById(associationItemUnitDTO.getId()),
      groceryListService.findById(groceryListDTO.getId()),
      quantity
    );
  }

  public void deleteItemAndUnitFromGroceryList(
    AssociationGroceryListDTO associationGroceryListDTO
  )
    throws GenericServiceException {
    associationGroceryListItemUnitService.deleteItemAndUnitFromGroceryList(
      associationGroceryListItemUnitService.findById(
        associationGroceryListDTO.getId()
      )
    );
  }

  public void updateItemAndUnitQuantity(
    AssociationGroceryListDTO associationGroceryListDTO,
    int quantity
  )
    throws GenericServiceException {
    associationGroceryListItemUnitService.updateItemAndUnitQuantity(
      associationGroceryListItemUnitService.findById(
        associationGroceryListDTO.getId()
      ),
      quantity
    );
  }

  public AssociationGroceryListDTO findByItemUnitAndGroceryList(
    AssociationItemUnitDTO associationItemUnitDTO,
    GroceryListDTO groceryListDTO
  )
    throws GenericServiceException {
    return modelMapper.associationGroceryListToassociationGroceryListDTO(
      associationGroceryListItemUnitService.findByItemUnitAndGroceryList(
        associationItemUnitService.findById(associationItemUnitDTO.getId()),
        groceryListService.findById(groceryListDTO.getId())
      )
    );
  }

  public List<AssociationGroceryListDTO> findByGroceryList(
    GroceryListDTO groceryListDTO
  )
    throws GenericServiceException {
    return modelMapper.associationGroceryListsToAssociationGroceryListDTOs(
      associationGroceryListItemUnitService.findByGroceryList(
        groceryListService.findById(groceryListDTO.getId())
      )
    );
  }

  public void setPurchased(
    AssociationGroceryListDTO associationGroceryListDTO,
    Boolean b
  )
    throws GenericServiceException {
    associationGroceryListItemUnitService.setPurchased(
      associationGroceryListItemUnitService.findById(
        associationGroceryListDTO.getId()
      ),
      b
    );
  }

  public boolean isPurchased(
    AssociationGroceryListDTO associationGroceryListDTO
  )
    throws GenericServiceException {
    return associationGroceryListItemUnitService.isPurchased(
      associationGroceryListItemUnitService.findById(
        associationGroceryListDTO.getId()
      )
    );
  }

  public List<AssociationGroceryListItemUnit> getSortedItemsByCategoryForGroceryList(
    Long groceryListId
  )
    throws GenericServiceException {
    return associationGroceryListItemUnitService.getSortedItemsByCategoryForGroceryList(
      groceryListId
    );
  }
}
