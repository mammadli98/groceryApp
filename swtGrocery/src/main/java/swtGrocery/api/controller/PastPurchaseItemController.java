package swtGrocery.api.controller;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import swtGrocery.api.ModelMapper;
import swtGrocery.api.contract.PastPurchaseItemDTO;
import swtGrocery.api.contract.interfaces.IPastPurchaseItemController;
import swtGrocery.backend.services.PastPurchaseItemService;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Component
public class PastPurchaseItemController implements IPastPurchaseItemController {

  @Autowired
  PastPurchaseItemService pastPurchaseItemService;

  @Autowired
  ModelMapper modelMapper;

  @Override
  public List<PastPurchaseItemDTO> pastPurchaseItems() {
    return modelMapper.PastPurchaseItemsToPastPurchaseItemDTOs(
      pastPurchaseItemService.pastPurchaseItems()
    );
  }

  @Override
  public PastPurchaseItemDTO create(
    String itemName,
    String itemUnit,
    int itemQuantity,
    String itemCategory
  )
    throws GenericServiceException {
    return modelMapper.PastPurchaseItemToPastPurchaseItemDTO(
      pastPurchaseItemService.create(
        itemName,
        itemUnit,
        itemQuantity,
        itemCategory
      )
    );
  }

  protected void delete(PastPurchaseItemDTO pastPurchaseItemDTO)
    throws GenericServiceException {
    pastPurchaseItemService.delete(
      pastPurchaseItemService.findById(pastPurchaseItemDTO.getId())
    );
  }

  /**
   * Gets a Path pathToPastPurchaseList from the view controller and passes it to the
   * PastPurchaseItemService for processing.
   *
   * @param pathToPastPurchaseList Path of the file to be imported
   */
  public void importPastPurchaseList(Path pathToPastPurchaseList) {
    pastPurchaseItemService.importPastPurchaseList(pathToPastPurchaseList);
  }

  public List<PastPurchaseItemDTO> searchByName(String searchedItem)
    throws GenericServiceException {
    return modelMapper.PastPurchaseItemsToPastPurchaseItemDTOs(
      pastPurchaseItemService.searchByName(searchedItem)
    );
  }

  public List<PastPurchaseItemDTO> searchByDate(
    LocalDate startDate,
    LocalDate endDate
  )
    throws GenericServiceException {
    return modelMapper.PastPurchaseItemsToPastPurchaseItemDTOs(
      pastPurchaseItemService.searchByDate(startDate, endDate)
    );
  }

  public List<PastPurchaseItemDTO> searchByNameAndDate(
    String searchedItem,
    LocalDate startDate,
    LocalDate endDate
  )
    throws GenericServiceException {
    return modelMapper.PastPurchaseItemsToPastPurchaseItemDTOs(
      pastPurchaseItemService.searchByNameAndDate(
        searchedItem,
        startDate,
        endDate
      )
    );
  }

  public Map<Object, Object> runAprioriAlgorithm() {
    return pastPurchaseItemService.runAprioriAlgorithm();
  }

  public Set<String> suggestItem(
    Map<Object, Object> aprioriLibrary,
    List<String> items
  ) {
    return pastPurchaseItemService.suggestItem(aprioriLibrary, items);
  }

  /**
   * @return list of strings with items based on previous purchases
   * @throws GenericServiceException
   */
  public List<String> suggestions() throws GenericServiceException {
    return pastPurchaseItemService.generateRecommendations();
  }
}
