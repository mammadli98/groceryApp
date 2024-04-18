package swtGrocery.backend.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.AssociationGroceryListItemUnit;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.GroceryList;

@Repository
public interface AssociationGroceryListItemUnitRepository
  extends CrudRepository<AssociationGroceryListItemUnit, Long> {
  List<AssociationGroceryListItemUnit> findAll();

  boolean existsByAssociationItemUnitAndGroceryList(
    AssociationItemUnit associationItemUnit,
    GroceryList groceryList
  );

  List<AssociationGroceryListItemUnit> findByGroceryList(
    GroceryList groceryList
  );

  List<AssociationGroceryListItemUnit> findByAssociationItemUnitIn(
    List<AssociationItemUnit> associationItemUnit
  );

  List<AssociationGroceryListItemUnit> findByAssociationItemUnit(
    AssociationItemUnit associationItemUnit
  );

  AssociationGroceryListItemUnit findByAssociationItemUnitAndGroceryList(
    AssociationItemUnit associationItemUnit,
    GroceryList groceryList
  );
}
