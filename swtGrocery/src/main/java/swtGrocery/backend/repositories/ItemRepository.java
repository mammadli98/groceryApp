package swtGrocery.backend.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.Item;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {
  List<Item> findAll();
}
