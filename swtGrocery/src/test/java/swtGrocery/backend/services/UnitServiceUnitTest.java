package swtGrocery.backend.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.LinkedList;
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
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.repositories.UnitRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ExtendWith(MockitoExtension.class)
public class UnitServiceUnitTest {

  @InjectMocks
  private UnitService unitService;

  @Mock
  private UnitRepository unitRepository;

  private Unit kg() {
    Unit unit = new Unit();
    unit.setName("kg");
    return unit;
  }

  private Unit l() {
    Unit unit = new Unit();
    unit.setName("l");
    return unit;
  }

  @Test
  public void createShouldFailIfNameIsNull() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.create("");
      }
    );

    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.create("     ");
      }
    );
  }

  @Test
  public void createUnitUnitTest() throws GenericServiceException {
    // Setup
    Unit kg = kg();
    // Stub
    doAnswer(
        invocation -> {
          Unit savedUnit = invocation.getArgument(0);
          return savedUnit;
        }
      )
      .when(this.unitRepository)
      .save(any(Unit.class));

    // Act
    Unit newUnit = this.unitService.create(kg.getUnitName());

    // Assert
    Assertions.assertThat(newUnit.getUnitName().equals("kg")).isTrue();
  }

  @Test
  public void checkIfUnitNotExistFailsIfInputEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.checkIfUnitNotExist("");
      }
    );

    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.checkIfUnitNotExist("   ");
      }
    );
  }

  @Test
  public void returnTrueIfUnitDoesNotExistsTest()
    throws GenericServiceException {
    String unitName = "kg";
    Unit liter = l();
    this.unitService.checkIfUnitNotExist(unitName);
    List<Unit> unitList = new ArrayList<>();

    //STUB
    doAnswer(invocation -> unitList).when(this.unitRepository).findAll();

    //Act
    unitList.add(liter);

    assertThat(this.unitService.checkIfUnitNotExist(unitName)).isTrue();
  }

  @Test
  public void returnFalseIfUnitDoesExistTest() throws GenericServiceException {
    String unitName = "l";
    Unit liter = l();
    this.unitService.checkIfUnitNotExist(unitName);
    List<Unit> unitList = new ArrayList<>();

    //STUB
    doAnswer(invocation -> unitList).when(this.unitRepository).findAll();

    //Act
    unitList.add(liter);

    assertThat(this.unitService.checkIfUnitNotExist(unitName)).isFalse();
  }

  @Test
  public void findByIdShouldFailIfUnitsEmpty() throws GenericServiceException {
    // Setup

    Long id = 1L;
    //STUB
    doAnswer(invocation -> Optional.empty())
      .when(this.unitRepository)
      .findById(id);

    //Assert
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.findById(id);
      }
    );
  }

  @Test
  public void findByNameShouldFailIfNameIsEmpty() {
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.findByName("");
      }
    );

    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.findByName("   ");
      }
    );
  }

  @Test
  public void findByNameShouldFailIfReturnUnitsEmpty() {
    //STUB
    doAnswer(
        invocation -> {
          List unitList = new LinkedList();
          return unitList;
        }
      )
      .when(this.unitRepository)
      .findAll();

    //Assert
    assertThrows(
      GenericServiceException.class,
      () -> {
        this.unitService.findByName("kg");
      }
    );
  }
}
