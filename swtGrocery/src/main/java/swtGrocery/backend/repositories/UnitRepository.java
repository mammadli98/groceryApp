package swtGrocery.backend.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.Item;
import swtGrocery.backend.entities.Unit;

@Repository
public interface UnitRepository extends CrudRepository<Unit, Long> {
  List<Unit> findAll();
}
