package swtGrocery.backend.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.repositories.ItemRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {

  @InjectMocks
  private ItemService itemService;

  @Mock
  private ItemRepository itemRepository;

  private Item apple() {
    Item apple = new Item();
    apple.setName("Apple");
    apple.setItemUnit("kg");
    apple.setItemQuantity(5);
    apple.setCategory("Fruit");
    return apple;
  }

  private Item bread() {
    Item bread = new Item();
    bread.setName("bRead");
    bread.setItemUnit("kg");
    bread.setCategory("Bread");
    bread.setItemQuantity(2);
    return bread;
  }

  @Test
  public void itemServiceReturnSearchedByNameItem()  //TODO: why is this repository test in Service Unit Testsuite?
    throws GenericServiceException {
    //Arrange
    Item item1 = new Item();
    item1.setName("blabla");
    item1.setItemUnit("blabla");

    Item item2 = new Item();
    item2.setName("albalb");
    item2.setItemUnit("albalb");

    List<Item> itemList = Arrays.asList(item1, item2);

    // Act
    when(itemRepository.findAll()).thenReturn(itemList);
    List<Item> allItems = itemService.searchByName("bla");

    //Assert
    Assertions.assertThat(allItems).isNotEmpty().hasSize(1);
  }

  @Test
  public void itemServiceReturnExceptionSearchedByNameItem() {
    //Arrange
    Item item1 = new Item();
    item1.setName("blabla");
    item1.setItemUnit("blabla");

    // Act and assert
    GenericServiceException exception = assertThrows(
      GenericServiceException.class,
      () -> itemService.searchByName("alb")
    );

    // Assert exception message
    assertThat(exception.getMessage())
      .isEqualTo("No result found with: \"alb\"");
  }

  @Test
  public void capitalizeFirstLetterTest() {
    //Arrange
    String itemName1 = apple().getItemName();
    String itemName2 = bread().getItemName();
    String itemName4 = "tomaTo \n Sauce";
    String[] testNames = { itemName1, itemName2, itemName4 };

    String[] assertNames = { "Apple", "Bread", "Tomato Sauce" };

    // Act
    for (int i = 0; i < testNames.length; i++) {
      testNames[i] = Utilities.capitalizeFirstLetter(testNames[i]);
    }
    // Assert
    assertArrayEquals(assertNames, testNames);
  }

  @Test
  public void createShouldFailIfItemNameIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.create("", "Bread");
      }
    );

    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.create("     ", "Bread");
      }
    );
  }

  @Test
  public void returnFalseIfItemDoesExistTest() {
    // Setup
    Item bread = bread();
    bread.setName("Bread");
    List<Item> itemList = List.of(bread);

    //STUB
    Mockito.doAnswer(invocation -> itemList).when(itemRepository).findAll();

    // Act
    boolean result = this.itemService.checkIfItemNotExist(bread.getItemName());

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  public void returnTrueIfItemDoesNotExistTest() {
    //Setup
    Item apple = apple();
    List<Item> itemList = Arrays.asList();

    //Act
    when(itemRepository.findAll()).thenReturn(itemList);
    boolean result =
      this.itemService.checkIfItemNotExist(
          Utilities.capitalizeFirstLetter(apple.getItemName())
        );

    //Assert
    assertThat(result).isTrue();
  }

  @Test
  public void deleteUnitTest() {
    // Setup
    Item bread = bread();
    List<Item> itemList = new ArrayList<>(Arrays.asList(bread));

    //Stub
    when(itemRepository.findAll()).thenReturn(itemList);
    doAnswer(
        invocation -> {
          itemList.remove(bread);
          return null;
        }
      )
      .when(itemRepository)
      .delete(any(Item.class));

    // Act
    boolean filled = this.itemRepository.findAll().isEmpty();
    this.itemService.delete(bread);
    boolean deleted = this.itemRepository.findAll().isEmpty();

    //Assert
    assertThat(filled == deleted).isFalse();
  }

  @Test
  public void updateShouldThrowGSExceptionIfNameEmpty() {
    Item apple = apple();
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.updateItemName(apple, "");
      }
    );
  }

  @Test
  public void updateShouldChangeNameCorrectlyTest()
    throws GenericServiceException {
    // Setup
    Item oldBread = bread();
    String newBreadName = "Whole wheat bread";
    Item newBread = bread();
    List<Item> itemList = new ArrayList<>();

    // Stub
    doAnswer(
        invocation -> {
          itemList.size();
          return null;
        }
      )
      .when(itemRepository)
      .save(any(Item.class));
    // Act
    this.itemService.updateItemName(newBread, newBreadName);

    // Assert
    Assertions.assertThat(oldBread.getItemName().equals("bRead")).isTrue();
    Assertions
      .assertThat(newBread.getItemName().equals("Whole wheat bread"))
      .isTrue();
  }

  @Test
  public void findByIDShouldFailIfItemNull() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.findById(null);
      }
    );
  }

  @Test
  public void findByNameShouldFailIfItemNull() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.findByName(null);
      }
    );
  }

  @Test
  public void findByNameFindsCorrectOneItem() throws GenericServiceException {
    // Setup
    List<Item> itemList = Arrays.asList(bread(), bread(), apple());
    // Stub
    doAnswer(
        invocation -> {
          return itemList;
        }
      )
      .when(itemRepository)
      .findAll();
    // Act
    Item exactlyOneMatch =
      this.itemService.findByName(itemList.get(2).getItemName());
    Item moreThanOneMatch =
      this.itemService.findByName(itemList.get(0).getItemName());

    //Assert
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.findByName("French Toast");
      }
    );
    assertThat(moreThanOneMatch == null).isFalse();
    assertThat(exactlyOneMatch == null).isFalse();
  }

  @Test
  public void findByCategoryShouldThrowExceptionIfSearchCategoryIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.findByCategory("");
      }
    );
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.itemService.findByCategory("     ");
      }
    );
  }

  @Test
  public void findByCategoryShouldReturnCorrectItems()
    throws GenericServiceException {
    // Setup
    List<Item> itemList = Arrays.asList(bread(), bread(), apple());
    // Stub
    doAnswer(
        invocation -> {
          return itemList;
        }
      )
      .when(itemRepository)
      .findAll();
    // Act
    List<Item> breads = this.itemService.findByCategory("Bread");
    List<Item> fruits = this.itemService.findByCategory("Fruit");
    List<Item> vegetables = this.itemService.findByCategory("Vegetable");

    // Assert
    assertThat(breads.size()).isEqualTo(2);
    assertThat(fruits.size()).isEqualTo(1);
    assertThat(vegetables.size()).isEqualTo(0);
  }
}
