package swtGrocery.api.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.ItemDTO;
import swtGrocery.api.contract.interfaces.IItemController;
import swtGrocery.backend.services.ItemService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class ItemController implements IItemController {

  @Autowired
  ItemService itemService;

  @Autowired
  ModelMapper modelMapper;

  @Override
  public List<ItemDTO> items() {
    return modelMapper.itemsToItemDTOs(itemService.items());
  }

  @Override
  public ItemDTO create(String itemName, String itemCategory)
    throws GenericServiceException {
    return modelMapper.itemToItemDTO(
      itemService.create(itemName, itemCategory)
    );
  }

  public void updateItemName(ItemDTO itemDto, String itemName)
    throws GenericServiceException {
    itemService.updateItemName(itemService.findById(itemDto.getId()), itemName);
  }

  /**
   * Interface to item serivce to update item category. Updates item category and persists it in the database.
   */
  public void updateItemCategory(ItemDTO itemDTO, String itemCategory)
    throws GenericServiceException {
    itemService.updateItemCategory(
      itemService.findById(itemDTO.getId()),
      itemCategory
    );
  }

  public List<ItemDTO> searchByName(String searchedItem)
    throws GenericServiceException {
    return modelMapper.itemsToItemDTOs(itemService.searchByName(searchedItem));
  }

  /**
   * Interface to item serivce to find item by name. Searches item by name and retruns the item if exists.
   */
  public ItemDTO findByName(String searchedItem)
    throws GenericServiceException {
    return modelMapper.itemToItemDTO(itemService.findByName(searchedItem));
  }

  /**
   * Interface to item serivce to find item by category. Searches item by category and returns a list of items
   * carrying this category.
   */
  public List<ItemDTO> findByCategory(String searchedCategory)
    throws GenericServiceException {
    return modelMapper.itemsToItemDTOs(
      itemService.findByCategory(searchedCategory)
    );
  }

  public void delete(ItemDTO itemDto) throws GenericServiceException {
    itemService.delete(itemService.findById(itemDto.getId()));
  }
}
