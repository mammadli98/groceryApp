package swtGrocery.backend.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.GroceryList;

@Repository
public interface GroceryListRepository
  extends CrudRepository<GroceryList, Long> {
  List<GroceryList> findAll();
}
