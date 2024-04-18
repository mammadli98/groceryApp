package swtGrocery.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.GroceryListDTO;
import swtGrocery.api.contract.interfaces.IGroceryListController;
import swtGrocery.backend.services.GroceryListService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class GroceryListController implements IGroceryListController {

  @Autowired
  GroceryListService groceryListService;

  @Autowired
  ModelMapper modelMapper;

  @Override
  public List<GroceryListDTO> groceryLists() {
    return modelMapper.groceryListsTogroceryListDTOs(
      groceryListService.groceryLists()
    );
  }

  @Override
  public GroceryListDTO create(String name) throws GenericServiceException {
    return modelMapper.groceryListTogroceryListDTO(
      groceryListService.create(name)
    );
  }

  public void delete(GroceryListDTO groceryListDTO)
    throws GenericServiceException {
    groceryListService.delete(
      groceryListService.findById(groceryListDTO.getId())
    );
  }

  public void updateName(GroceryListDTO groceryListDTO, String name)
    throws GenericServiceException {
    groceryListService.updateName(
      groceryListService.findById(groceryListDTO.getId()),
      name
    );
  }

  public GroceryListDTO findByName(String name) throws GenericServiceException {
    return modelMapper.groceryListTogroceryListDTO(
      groceryListService.findByName(name)
    );
  }
}
