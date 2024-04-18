package swtGrocery.backend.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
import swtGrocery.api.controller.AssociationGroceryListController;
import swtGrocery.backend.entities.*;
import swtGrocery.backend.repositories.AssociationGroceryListItemUnitRepository;
import swtGrocery.backend.repositories.GroceryListRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class AssociationGroceryItemUnitListServiceUnitTest {

  @InjectMocks
  private AssociationGroceryListItemUnitService associationGroceryListItemUnitService;

  @InjectMocks
  private AssociationGroceryListController associationGroceryListController;

  @Mock
  private GroceryListRepository groceryListRepository;

  @InjectMocks
  private AssociationGroceryListItemUnit associationGroceryListItemUnit;

  @Mock
  private AssociationGroceryListItemUnitRepository associationGroceryListItemUnitRepository;

  private GroceryList groceryList() {
    GroceryList groceryList = new GroceryList();
    groceryList.setId(1L);
    groceryList.setName("Test");
    return groceryList;
  }

  private Item bread() {
    Item bread = new Item();
    bread.setName("Bread");
    return bread;
  }

  private Unit kg() {
    Unit kg = new Unit();
    kg.setName("kg");
    return kg;
  }

  private AssociationItemUnit associationItemUnit() {
    AssociationItemUnit associationItemUnit = new AssociationItemUnit();
    associationItemUnit.setItem(bread());
    associationItemUnit.setUnit(kg());
    associationItemUnit.setId(1L);
    return associationItemUnit;
  }

  private AssociationGroceryListItemUnit associationGroceryListItemUnit(
    int quant
  ) {
    AssociationGroceryListItemUnit associationGroceryListItemUnit = new AssociationGroceryListItemUnit();
    associationGroceryListItemUnit.setGroceryList(groceryList());
    associationGroceryListItemUnit.setQuantity(quant);
    associationGroceryListItemUnit.setAssociationItemUnit(
      associationItemUnit()
    );
    return associationGroceryListItemUnit;
  }

  @Test
  public void addItemAndUnitToGroceryListShouldSaveAssociationGroceryListItemUnit()
    throws GenericServiceException {
    // Arrange
    AssociationItemUnit testAssociation = associationItemUnit();
    GroceryList testList = groceryList();
    int quant = 5;
    AssociationGroceryListItemUnit testGroceryListItemUnit = new AssociationGroceryListItemUnit();

    // STUB
    Mockito
      .doAnswer(invocation -> false)
      .when(associationGroceryListItemUnitRepository)
      .existsByAssociationItemUnitAndGroceryList(
        Mockito.any(AssociationItemUnit.class),
        Mockito.any(GroceryList.class)
      );
    Mockito
      .doAnswer(invocation -> testGroceryListItemUnit)
      .when(associationGroceryListItemUnitRepository)
      .save(Mockito.any(AssociationGroceryListItemUnit.class));

    // ACT
    this.associationGroceryListItemUnitService.addItemAndUnitToGroceryList(
        testAssociation,
        testList,
        quant
      );

    // ASSERT
    Mockito
      .verify(associationGroceryListItemUnitRepository, Mockito.times(1))
      .save(Mockito.any(AssociationGroceryListItemUnit.class));
  }

  @Test
  public void addItemShouldThrowGenericExceptionIfAssociationItemUnitGroceryListExists() {
    //STUB
    Mockito
      .doAnswer(invocation -> true)
      .when(associationGroceryListItemUnitRepository)
      .existsByAssociationItemUnitAndGroceryList(
        Mockito.any(AssociationItemUnit.class),
        Mockito.any(GroceryList.class)
      );
    //ASSERT
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        associationGroceryListItemUnitService.addItemAndUnitToGroceryList(
          associationItemUnit(),
          groceryList(),
          1
        );
      }
    );
  }

  @Test
  public void testDeleteItemAndUnitFromGroceryList() {
    Mockito
      .doNothing()
      .when(associationGroceryListItemUnitRepository)
      .delete(Mockito.any());

    this.associationGroceryListItemUnitService.deleteItemAndUnitFromGroceryList(
        Mockito.any()
      );

    Mockito
      .verify(associationGroceryListItemUnitRepository, Mockito.times(1))
      .delete(Mockito.any());
  }

  @Test
  public void updateItemAndUnitQuantityShouldChangeQuantity()
    throws GenericServiceException {
    // SETUP
    AssociationGroceryListItemUnit testAssociation = new AssociationGroceryListItemUnit();

    //ACT
    associationGroceryListItemUnitService.updateItemAndUnitQuantity(
      testAssociation,
      5
    );

    //ASSERT
    Assertions.assertEquals(5, testAssociation.getQuantity());
  }

  @Test
  public void updateItemAndUnitQuantityShouldInvokeRepositorySave()
    throws GenericServiceException {
    // SETUP
    AssociationGroceryListItemUnit testAssociation = new AssociationGroceryListItemUnit();
    Mockito
      .doAnswer(invocation -> testAssociation)
      .when(associationGroceryListItemUnitRepository)
      .save(Mockito.any());

    //ACT
    associationGroceryListItemUnitService.updateItemAndUnitQuantity(
      testAssociation,
      5
    );

    //ASSERT
    Mockito
      .verify(associationGroceryListItemUnitRepository, Mockito.times(1))
      .save(Mockito.any());
  }

  @Test
  public void findByIDShouldThrowGenericExceptionIfAssociationGroceryListEmpty() {
    //STUB
    Mockito
      .doAnswer(invocation -> Optional.empty())
      .when(associationGroceryListItemUnitRepository)
      .findById(Mockito.any(Long.class));

    //Assert
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.associationGroceryListItemUnitService.findById(5L);
      }
    );
  }

  @Test
  public void findByIdShouldInvokeAssociationGroceryListGetIfListNotEmpty()
    throws GenericServiceException {
    AssociationGroceryListItemUnit testAssociation = associationGroceryListItemUnit(
      1
    );

    //STUB
    Mockito
      .doAnswer(invocation -> Optional.of(testAssociation))
      .when(associationGroceryListItemUnitRepository)
      .findById(Mockito.any(Long.class));

    //ACT
    AssociationGroceryListItemUnit actualAssociation =
      this.associationGroceryListItemUnitService.findById(5L);
    //ASSERT
    Assertions.assertEquals(testAssociation, actualAssociation);
  }

  @Test
  public void findByGroceryListShouldThrowGenericExceptionIfNoAssociationFound() {
    //STUB
    Mockito
      .doAnswer(invocation -> List.of())
      .when(associationGroceryListItemUnitRepository)
      .findByGroceryList(Mockito.any(GroceryList.class));

    //ASSERT
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.associationGroceryListItemUnitService.findByGroceryList(
            groceryList()
          );
      }
    );
  }

  @Test
  public void findByGroceryListShouldReturnFoundLists()
    throws GenericServiceException {
    List<AssociationGroceryListItemUnit> testAssociationList = List.of(
      associationGroceryListItemUnit(5)
    );
    //STUB
    Mockito
      .doAnswer(invocation -> List.of(testAssociationList.get(0)))
      .when(associationGroceryListItemUnitRepository)
      .findByGroceryList(Mockito.any(GroceryList.class));

    //ASSERT
    Assertions.assertEquals(
      testAssociationList,
      this.associationGroceryListItemUnitService.findByGroceryList(
          groceryList()
        )
    );
  }

  @Test
  public void findByItemUnitAndGroceryListShouldThrowGenericExceptionIfNoGroceryListItemUnitAssociationExists() {
    //STUB
    Mockito
      .doAnswer(invocation -> null)
      .when(associationGroceryListItemUnitRepository)
      .findByAssociationItemUnitAndGroceryList(
        Mockito.any(AssociationItemUnit.class),
        Mockito.any(GroceryList.class)
      );

    //ASSERT
    Assertions.assertThrows(
      GenericServiceException.class,
      () -> {
        this.associationGroceryListItemUnitService.findByItemUnitAndGroceryList(
            associationItemUnit(),
            groceryList()
          );
      }
    );
  }

  @Test
  public void findByItemUnitAndGroceryListShouldReturnAssociationGrocerListItemUnit()
    throws GenericServiceException {
    AssociationGroceryListItemUnit testAssociation = associationGroceryListItemUnit(
      5
    );
    GroceryList testList = groceryList();
    AssociationItemUnit testItemUnit = associationItemUnit();

    //STUB
    Mockito
      .doAnswer(invocation -> testAssociation)
      .when(associationGroceryListItemUnitRepository)
      .findByAssociationItemUnitAndGroceryList(
        Mockito.any(AssociationItemUnit.class),
        Mockito.any(GroceryList.class)
      );

    //ACT
    AssociationGroceryListItemUnit actualGroceryListItemUnit =
      this.associationGroceryListItemUnitService.findByItemUnitAndGroceryList(
          testItemUnit,
          testList
        );

    //ASSERT
    Assertions.assertEquals(testAssociation, actualGroceryListItemUnit);
  }

  @Test
  public void setPurchasedShouldChangeBoolean() {
    //SETUP
    AssociationGroceryListItemUnit testAssociation1 = associationGroceryListItemUnit(
      5
    );
    testAssociation1.setisPurchased(false);

    //STUB
    Mockito
      .doAnswer(invocation -> testAssociation1)
      .when(associationGroceryListItemUnitRepository)
      .save(Mockito.any(AssociationGroceryListItemUnit.class));

    //ACT
    this.associationGroceryListItemUnitService.setPurchased(
        testAssociation1,
        true
      );

    //ASSERT
    assertThat(testAssociation1.getisPurchased() == true).isTrue();
  }
}
