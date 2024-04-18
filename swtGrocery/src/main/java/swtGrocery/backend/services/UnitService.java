package swtGrocery.backend.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.Unit;
import swtGrocery.backend.repositories.ItemRepository;
import swtGrocery.backend.repositories.UnitRepository;
import swtGrocery.backend.services.exceptions.GenericServiceException;

@Service
public class UnitService {

  @Autowired
  private UnitRepository unitRepository;

  public List<Unit> units() {
    return unitRepository.findAll();
  }

  public Unit create(String unitName) throws GenericServiceException {
    if (unitName.isEmpty() || unitName.trim().isEmpty()) {
      throw new GenericServiceException(
        "Unit name is empty. Please add a name to proceed."
      );
    }
    Unit newUnit = new Unit();
    newUnit.setName(unitName);
    return unitRepository.save(newUnit);
  }

  public boolean checkIfUnitNotExist(String unitName)
    throws GenericServiceException {
    if (unitName.isEmpty() || unitName.trim().isEmpty()) {
      throw new GenericServiceException("No unit name was specified.");
    }
    return unitRepository
      .findAll()
      .stream()
      .noneMatch(unit -> unit.getUnitName().equals(unitName));
  }

  public Unit findById(Long unitId) throws GenericServiceException {
    Optional<Unit> units = unitRepository.findById(unitId);
    if (units.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Item with ID: \"" + unitId + "\""
      );
    }
    return units.get();
  }

  public Unit findByName(String unitName) throws GenericServiceException {
    if (unitName.isEmpty() || unitName.trim().isEmpty()) {
      throw new GenericServiceException("No unit name was specified.");
    }
    List<Unit> units = unitRepository
      .findAll()
      .stream()
      .filter(unit -> unit.getUnitName().equals(unitName))
      .collect(Collectors.toList());

    if (units.isEmpty()) {
      throw new GenericServiceException(
        "No Result found for Unit with name: \"" + unitName + "\""
      );
    }
    return units.get(0);
  }
}
