package swtGrocery.backend.repositories;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.Unit;

@Repository
public interface AssociationItemUnitRepository
  extends CrudRepository<AssociationItemUnit, Long> {
  List<AssociationItemUnit> findAll();

  List<AssociationItemUnit> findByItem(Item item);

  List<AssociationItemUnit> findByUnit(Unit unit);

  AssociationItemUnit findByItemAndUnit(Item item, Unit unit);

  boolean existsByItemAndUnit(Item item, Unit unit);

  List<AssociationItemUnit> findByUnitIn(Collection<Unit> units);

  List<AssociationItemUnit> findByItemCategory(String itemCategory);
}
