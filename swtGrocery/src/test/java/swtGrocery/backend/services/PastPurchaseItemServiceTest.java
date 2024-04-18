package swtGrocery.backend.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import swtGrocery.backend.entities.PastPurchaseItem;
import swtGrocery.backend.repositories.PastPurchaseItemRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class PastPurchaseItemServiceTest {

  @InjectMocks
  private PastPurchaseItemService pastPurchaseItemService;

  @Mock
  private PastPurchaseItemRepository pastPurchaseItemRepository;

  @Test
  public void createShouldFailIfNameIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("", "a", 1)
    );

    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("      ", "a", 1)
    );
  }

  @Test
  public void createShouldFailIfUnitIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("a", "", 1)
    );

    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("a", "      ", 1)
    );
  }

  @Test
  public void createShouldFailIfQuantityIsNegativeOrZero() {
    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("a", "a", -1)
    );

    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.create("a", "a", 0)
    );
  }

  @Test
  public void createPastPurchaseItemIfParametersAreCorrect()
    throws GenericServiceException {
    // Stub
    doAnswer(invocation -> invocation.getArgument(0))
      .when(this.pastPurchaseItemRepository)
      .save(any(PastPurchaseItem.class));

    // Act
    PastPurchaseItem pastPurchaseItem =
      this.pastPurchaseItemService.create("apples", "kg", 1);

    // Assert
    Assertions
      .assertThat(pastPurchaseItem.getItemName().equals("apples"))
      .isTrue();
    Assertions.assertThat(pastPurchaseItem.getItemUnit().equals("kg")).isTrue();
    Assertions.assertThat(pastPurchaseItem.getItemQuantity() == 1).isTrue();
  }

  @Test
  public void saveCurrentDateInPastPurchaseItemIfParametersAreCorrect()
    throws GenericServiceException {
    // Stub
    doAnswer(invocation -> invocation.getArgument(0))
      .when(this.pastPurchaseItemRepository)
      .save(any(PastPurchaseItem.class));

    // Act
    PastPurchaseItem pastPurchaseItem =
      this.pastPurchaseItemService.create("apples", "kg", 1);

    // Assert
    Assertions
      .assertThat(
        pastPurchaseItem.getItemPurchaseDate().equals(LocalDate.now())
      )
      .isTrue();
  }

  @Test
  public void findByIdShouldThrowExceptionIfItemsAreEmpty() {
    // Setup

    Long id = 1L;
    //STUB
    doAnswer(invocation -> Optional.empty())
      .when(this.pastPurchaseItemRepository)
      .findById(id);

    //Assert
    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.findById(id)
    );
  }

  @Test
  public void deleteShouldThrowExceptionWhenNullIsPassed() {
    assertThrows(
      GenericServiceException.class,
      () -> this.pastPurchaseItemService.delete(null)
    );
  }

  @Test
  public void pastPurchaseItemsShouldReturnAllItems()
    throws GenericServiceException {
    PastPurchaseItem pastPurchaseItem =
      this.pastPurchaseItemService.create("apples", "kg", 1);
    List<PastPurchaseItem> pastPurchaseItems = new ArrayList<>(
      Arrays.asList(pastPurchaseItem)
    );

    //Stub
    when(pastPurchaseItemRepository.findAll()).thenReturn(pastPurchaseItems);

    // Act and Assert
    assertThat(pastPurchaseItemService.pastPurchaseItems() == pastPurchaseItems)
      .isTrue();
  }

  @Test
  public void searchByNameShouldReturnMatchingItems()
    throws GenericServiceException {
    // Arrange
    PastPurchaseItem appleItem = new PastPurchaseItem();
    appleItem.setItemName("Apple");
    appleItem.setItemUnit("kg");
    appleItem.setItemQuantity(2);

    PastPurchaseItem bananaItem = new PastPurchaseItem();
    bananaItem.setItemName("Banana");
    bananaItem.setItemUnit("each");
    bananaItem.setItemQuantity(3);
    bananaItem.setItemPurchaseDate(LocalDate.now());

    when(pastPurchaseItemRepository.findAll())
      .thenReturn(Arrays.asList(appleItem, bananaItem));

    // Act
    List<PastPurchaseItem> result = pastPurchaseItemService.searchByName(
      "apple"
    );

    // Assert
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getItemName()).isEqualTo("Apple");
  }

  @Test
  public void searchByDateShouldReturnMatchingItems()
    throws GenericServiceException {
    // Arrange
    LocalDate startDate = LocalDate.of(2023, 1, 1);
    LocalDate endDate = LocalDate.of(2023, 12, 31);

    PastPurchaseItem appleItem = new PastPurchaseItem();
    appleItem.setItemName("Apple");
    appleItem.setItemUnit("kg");
    appleItem.setItemQuantity(2);
    appleItem.setItemPurchaseDate(LocalDate.of(2023, 5, 1));

    PastPurchaseItem bananaItem = new PastPurchaseItem();
    bananaItem.setItemName("Banana");
    bananaItem.setItemUnit("each");
    bananaItem.setItemQuantity(3);
    bananaItem.setItemPurchaseDate(LocalDate.of(2023, 7, 15));

    when(pastPurchaseItemRepository.findAll())
      .thenReturn(Arrays.asList(appleItem, bananaItem));

    // Act
    List<PastPurchaseItem> result = pastPurchaseItemService.searchByDate(
      startDate,
      endDate
    );

    // Assert
    assertThat(result.size()).isEqualTo(2);
  }

  @Test
  public void searchByNameAndDateShouldReturnMatchingItems()
    throws GenericServiceException {
    // Arrange
    LocalDate startDate = LocalDate.of(2023, 1, 1);
    LocalDate endDate = LocalDate.of(2023, 12, 31);

    PastPurchaseItem appleItem = new PastPurchaseItem();
    appleItem.setItemName("Apple");
    appleItem.setItemUnit("kg");
    appleItem.setItemQuantity(2);
    appleItem.setItemPurchaseDate(LocalDate.of(2023, 5, 1));

    PastPurchaseItem bananaItem = new PastPurchaseItem();
    bananaItem.setItemName("Banana");
    bananaItem.setItemUnit("each");
    bananaItem.setItemQuantity(3);
    bananaItem.setItemPurchaseDate(LocalDate.of(2023, 7, 15));

    when(pastPurchaseItemRepository.findAll())
      .thenReturn(Arrays.asList(appleItem, bananaItem));

    // Act
    List<PastPurchaseItem> result = pastPurchaseItemService.searchByNameAndDate(
      "banana",
      startDate,
      endDate
    );

    // Assert
    assertThat(result.size() == 1).isTrue();
    assertThat(result.get(0).getItemName()).isEqualTo("Banana");
  }
}
