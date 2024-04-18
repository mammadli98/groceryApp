package swtGrocery.backend.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import swtGrocery.backend.entities.GroceryList;
import swtGrocery.backend.repositories.GroceryListRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class GroceryListServiceUnitTest {

  @InjectMocks
  GroceryListService groceryListService;

  @Mock
  GroceryListRepository groceryListRepository;

  private GroceryList weekendList() {
    GroceryList groceryList = new GroceryList();
    groceryList.setName("Weekend List");
    return groceryList;
  }

  private GroceryList weekdayList() {
    GroceryList groceryList = new GroceryList();
    groceryList.setName("Weekday List");
    return groceryList;
  }

  @Test
  public void createShouldThrowExceptionIfNameIsEmptyTest() {
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.create("");
      }
    );
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.create("   ");
      }
    );
  }

  @Test
  public void createShouldThrowExceptionIfListExists() {
    // Setup
    GroceryList weekend = weekendList();
    LinkedList<GroceryList> repositoryReturn = new LinkedList<>();
    repositoryReturn.add(weekend);
    // STUB
    Mockito
      .doAnswer(invocation -> repositoryReturn)
      .when(groceryListRepository)
      .findAll();

    // Assert
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.create("Weekend List");
      }
    );
  }

  @Test
  public void createShouldCreateGroceryListTest()
    throws GenericServiceException {
    // SETUP
    GroceryList weekend = weekendList();
    // STUB
    Mockito
      .doAnswer(invocation -> invocation.getArgument(0))
      .when(groceryListRepository)
      .save(Mockito.any(GroceryList.class));
    // Act
    GroceryList returnedFromMethod =
      this.groceryListService.create(weekend.getName());
    // Assert
    Assertions.assertEquals(weekend.getName(), returnedFromMethod.getName());
  }

  @Test
  public void updateNameShouldThrowExceptionIfNameEmpty() {
    GroceryList weekend = weekendList();
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.updateName(weekend, "");
      }
    );
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.updateName(weekend, "   ");
      }
    );
  }

  @Test
  public void updateNameShouldThrowExceptionIfListIsNull() {
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.updateName(null, "new name");
      }
    );
  }

  @Test
  public void updateNameUnitTest() throws GenericServiceException {
    // Setup
    GroceryList weekend = weekendList();
    String nameBeforeUpdate = weekend.getName();
    // STUB
    Mockito
      .doAnswer(invocation -> weekend)
      .when(groceryListRepository)
      .save(Mockito.any(GroceryList.class));
    // ACT
    this.groceryListService.updateName(weekend, "New Weekend");
    // Assert
    assertThat(nameBeforeUpdate.equals(weekend.getName())).isFalse();
  }

  @Test
  public void deleteShouldThrowExceptionIfArgumentIsNull() {
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.delete(null);
      }
    );
  }

  @Test
  public void findByIDShouldThrowExceptionIfIDPassedNull() {
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.findById(null);
      }
    );
  }

  @Test
  public void findByIdShouldThrowExpcetionIfNoListIsFound() {
    // STUB
    Mockito
      .doAnswer(invocation -> Optional.empty())
      .when(this.groceryListRepository)
      .findById(Mockito.any(Long.class));
    // Assert
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.findById(5L);
      }
    );
  }

  @Test
  public void findByIDReturnsGroceryListUnitTest()
    throws GenericServiceException {
    // SETUP
    GroceryList weekend = weekendList();

    //STUB
    Mockito
      .doAnswer(invocation -> Optional.of(weekend))
      .when(this.groceryListRepository)
      .findById(Mockito.any(Long.class));
    // Act
    GroceryList foundGroceryList = this.groceryListService.findById(5L);

    // Assert
    assertThat(foundGroceryList == null).isFalse();
  }

  @Test
  public void findByNameShouldThrowExceptionIfPassedArgumentIsEmptyTest() {
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.findByName("");
      }
    );

    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.findByName("   ");
      }
    );
  }

  @Test
  public void findByNameShouldThrowExceptionIfNoListIsFoundTest() {
    // STUB
    Mockito
      .doAnswer(
        invocation -> {
          List<GroceryList> returnedLists = new LinkedList<>();
          return returnedLists;
        }
      )
      .when(this.groceryListRepository)
      .findAll();
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.groceryListService.findByName("weekend");
      }
    );
  }

  @Test
  public void findByNameShouldReturnListWhenFound()
    throws GenericServiceException {
    //SETUP
    GroceryList weekday = weekdayList();
    LinkedList<GroceryList> foundLists = new LinkedList<>();
    foundLists.add(weekday);
    //STUB
    Mockito
      .doAnswer(invocation -> foundLists)
      .when(this.groceryListRepository)
      .findAll();

    // ACT
    GroceryList listReturned =
      this.groceryListService.findByName(weekdayList().getName());

    // ASSERT
    assertThat(listReturned.getName().equals(weekdayList().getName())).isTrue();
  }
}
