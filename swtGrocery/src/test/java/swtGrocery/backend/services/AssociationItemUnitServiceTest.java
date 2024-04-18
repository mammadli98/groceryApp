package swtGrocery.backend.services;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import swtGrocery.backend.Utilities;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.repositories.AssociationGroceryListItemUnitRepository;
import swtGrocery.backend.repositories.AssociationItemUnitRepository;
import swtGrocery.backend.repositories.ItemRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class AssociationItemUnitServiceTest {

  @InjectMocks
  AssociationItemUnitService associationItemUnitService;

  @Mock
  AssociationItemUnitRepository associationItemUnitRepository;

  @Mock
  AssociationGroceryListItemUnitRepository associationGroceryListItemUnitRepository;

  @Mock
  ItemRepository itemRepository;

  @Mock
  ItemService itemService;

  @Mock
  UnitService unitService;

  private AssociationItemUnit breadKG() {
    AssociationItemUnit breadKG = new AssociationItemUnit();
    Item bread = bread();
    Unit kg = new Unit();
    breadKG.setItem(bread);
    breadKG.setUnit(kg);
    return breadKG;
  }

  private AssociationItemUnit watermelonKG() {
    AssociationItemUnit watermelonKG = new AssociationItemUnit();
    Item watermelon = watermelon();
    Unit kg = new Unit();
    watermelonKG.setItem(watermelon);
    watermelonKG.setUnit(kg);
    return watermelonKG;
  }

  private Item watermelon() {
    Item watermelon = new Item();
    watermelon.setId(1L);
    watermelon.setName("watermelon");
    watermelon.setCategory("Fruit");
    return watermelon;
  }

  private AssociationItemUnit watermelonL() {
    AssociationItemUnit watermelonL = new AssociationItemUnit();
    Item watermelon = watermelon();
    Unit l = new Unit();
    watermelonL.setItem(watermelon);
    watermelonL.setUnit(l);
    return watermelonL;
  }

  private Item bread() {
    Item bread = new Item();
    bread.setId(2L);
    bread.setName("bread");
    bread.setCategory("Bread");
    return bread;
  }

  private Unit kg() {
    Unit kg = new Unit();
    kg.setName("kg");
    return kg;
  }

  private Unit l() {
    Unit l = new Unit();
    l.setName("l");
    return l;
  }

  @Test
  public void createShouldThrowExceptionIfItemNameIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.create("", List.of("kg"), "Bread");
      }
    );
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.create(" ", List.of("kg"), "Fruit");
      }
    );
  }

  @Test
  public void createShouldThrowExceptionIfUnitNamesIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.create("bread", List.of(), "Vegetable");
      }
    );
  }

  @Test
  public void createShouldCreateAssociationUnitTest()
    throws GenericServiceException {
    // SETUP
    List<String> unitNames = List.of("kg");
    // STUB
    Mockito
      .doAnswer(invocation -> true)
      .when(itemService)
      .checkIfItemNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> true)
      .when(unitService)
      .checkIfUnitNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> bread())
      .when(itemService)
      .create(any(String.class), any(String.class));
    Mockito
      .doAnswer(invocation -> kg())
      .when(unitService)
      .create(any(String.class));
    Mockito
      .doAnswer(invocation -> breadKG())
      .when(associationItemUnitRepository)
      .save(any(AssociationItemUnit.class));
    // ACT
    this.associationItemUnitService.create("bread", unitNames, "Bread");
    // ASSERT
    Mockito
      .verify(itemService, Mockito.times(1))
      .checkIfItemNotExist(any(String.class));
    Mockito
      .verify(unitService, Mockito.times(1))
      .checkIfUnitNotExist(any(String.class));
    Mockito
      .verify(associationItemUnitRepository, Mockito.times(1))
      .save(any(AssociationItemUnit.class));
  }

  @Test
  public void createShouldFindByNameIfItemExists()
    throws GenericServiceException {
    // SETUP
    List<String> unitNames = List.of("kg");
    // STUB
    Mockito
      .doAnswer(invocation -> false)
      .when(itemService)
      .checkIfItemNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> true)
      .when(unitService)
      .checkIfUnitNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> bread())
      .when(itemService)
      .findByName(any(String.class));
    Mockito
      .doAnswer(invocation -> kg())
      .when(unitService)
      .create(any(String.class));
    Mockito
      .doAnswer(invocation -> breadKG())
      .when(associationItemUnitRepository)
      .save(any(AssociationItemUnit.class));
    // ACT
    this.associationItemUnitService.create("bread", unitNames, "Bread");
    // ASSERT
    Mockito.verify(itemService, Mockito.times(1)).findByName(any(String.class));
  }

  @Test
  public void createShouldFindByNameIfUnitExist()
    throws GenericServiceException {
    // SETUP
    List<String> unitNames = List.of("kg");
    // STUB
    Mockito
      .doAnswer(invocation -> true)
      .when(itemService)
      .checkIfItemNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> false)
      .when(unitService)
      .checkIfUnitNotExist(any(String.class));
    Mockito
      .doAnswer(invocation -> bread())
      .when(itemService)
      .create(any(String.class), any(String.class));
    Mockito
      .doAnswer(invocation -> kg())
      .when(unitService)
      .findByName(any(String.class));

    Mockito
      .doAnswer(invocation -> breadKG())
      .when(associationItemUnitRepository)
      .save(any(AssociationItemUnit.class));
    // ACT
    this.associationItemUnitService.create("bread", unitNames, "Bread");
    // ASSERT
    Mockito.verify(unitService, Mockito.times(1)).findByName(any(String.class));
  }

  @Test
  public void getItemUnitsShouldReturnList() throws GenericServiceException {
    // SETUP
    Item bread = bread();
    // STUB
    Mockito
      .doAnswer(invocation -> List.of(breadKG()))
      .when(associationItemUnitRepository)
      .findByItem(any(Item.class));
    // ACT
    List<Unit> units = this.associationItemUnitService.getItemUnits(bread);
    // ASSERT
    assertEquals(1, units.size());
  }

  @Test
  public void updateItemUnitsShouldThrowGenericExceptionIfUnitListIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.updateItemUnits(bread(), List.of());
      }
    );
  }

  @Test
  public void updateItemUnitsShouldRemoveAssociationItemUnitIfUnitIsNotInList()
    throws GenericServiceException {
    //TODO: WhiteBoxtest with Mockito ArgumentCaptor

    // SETUP
    Item bread = bread();
    Unit kg = kg();
    Unit l = l();

    List<Unit> unitAfter = List.of(kg, l);

    // STUB
    List<AssociationItemUnit> associationItemUnits = Mockito
      .doAnswer(invocation -> List.of(breadKG(), watermelonL()))
      .when(associationItemUnitRepository)
      .findByItem(Mockito.any(Item.class));
    Mockito
      .doAnswer(invocation -> List.of(new AssociationGroceryListItemUnit()))
      .when(associationGroceryListItemUnitRepository)
      .findByAssociationItemUnit(Mockito.any(AssociationItemUnit.class));
    Mockito
      .doNothing()
      .when(associationItemUnitRepository)
      .delete(Mockito.any(AssociationItemUnit.class));
    Mockito
      .doNothing()
      .when(associationGroceryListItemUnitRepository)
      .deleteAll(Mockito.any(List.class));
    Mockito
      .doAnswer(invocation -> new AssociationItemUnit())
      .when(associationItemUnitRepository)
      .findByItemAndUnit(Mockito.any(Item.class), Mockito.any(Unit.class));
    Utilities.capitalizeFirstLetter(Mockito.any(String.class));

    // ACT
    associationItemUnitService.updateItemUnits(bread, unitAfter);
    //ASSERT

  }

  @Test
  public void findByIdShouldThrowGenericExceptionIfIdIsNull() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findById(null);
      }
    );
  }

  @Test
  public void findByItemAndUnitShouldThrowGenericExceptionIfAssocationItemUnitIsNull() {
    Mockito
      .doAnswer(invocation -> null)
      .when(associationItemUnitRepository)
      .findByItemAndUnit(Mockito.any(Item.class), Mockito.any(Unit.class));
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByItemAndUnit(bread(), kg());
      }
    );
  }

  @Test
  public void findByItemAndUnitShouldThrowGenericExceptionIfInputIsNull()
    throws GenericServiceException {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByItemAndUnit(null, null);
      }
    );

    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByItemAndUnit(bread(), null);
      }
    );
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByItemAndUnit(null, kg());
      }
    );
  }

  @Test
  public void findByCategoryShouldThrowGenericExceptionIfCategoryIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByCategory("");
      }
    );
    assertThrows(
      GenericServiceException.class,
      () -> {
        associationItemUnitService.findByCategory("     ");
      }
    );
  }
  /*@Test
    public void findByCategoryShouldReturnCorrectList() throws GenericServiceException {
        // SETUP
        Item bread = bread();
        Item watermelon = watermelon();
        // STUB
        Mockito
                .doAnswer(invocation -> List.of(bread, watermelon))
                .when(itemService)
                .findByCategory(Mockito.any(String.class));
        // ACT
        List<AssociationItemUnit> associationItemUnits = associationItemUnitService.findByCategory("Bread");
        // ASSERT
        assertEquals(1, associationItemUnits.size());
    }*/
}
