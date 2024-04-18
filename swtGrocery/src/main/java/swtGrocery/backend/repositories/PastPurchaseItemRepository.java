package swtGrocery.backend.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import swtGrocery.backend.entities.PastPurchaseItem;

@Repository
public interface PastPurchaseItemRepository
  extends CrudRepository<PastPurchaseItem, Long> {
  List<PastPurchaseItem> findAll();
}
