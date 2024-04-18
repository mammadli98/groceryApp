package swtGrocery.backend.repositories;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import swtGrocery.backend.entities.Item;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ItemRepositoryUnitTest {

  @Autowired
  private ItemRepository itemRepository;

  @Test
  public void itemRepositoryReturnSavedItem() {
    //Arrange
    Item item = new Item();
    item.setName("blabla");
    item.setItemUnit("blabla");

    //Act
    Item savedItem = itemRepository.save(item);

    //Assert
    Assertions.assertThat(savedItem).isNotNull();
    Assertions.assertThat(savedItem.getId()).isGreaterThan(0);
  }

  @Test
  public void itemRepositoryReturnMoreThanOneItem() {
    //Arrange
    Item item1 = new Item();
    item1.setName("blabla1");
    item1.setItemUnit("blabla1");

    Item item2 = new Item();
    item2.setName("blabla2");
    item2.setItemUnit("blabla2");

    //Act
    itemRepository.save(item1);
    itemRepository.save(item2);

    List<Item> itemList = itemRepository.findAll();

    //Assert
    Assertions.assertThat(itemList).isNotNull();
    Assertions.assertThat(itemList.size()).isEqualTo(2);
  }

  @Test
  public void itemRepositoryFindByIdReturnItemNotNull() {
    //Arrange
    Item item = new Item();
    item.setName("blabla");
    item.setItemUnit("blabla");

    //Act
    itemRepository.save(item);
    Item itemList = itemRepository.findById(item.getId()).get();

    //Assert
    Assertions.assertThat(itemList).isNotNull();
  }

  @Test
  public void itemRepositoryUpdateItemReturnItemIsEqual() {
    //Arrange
    Item item = new Item();
    item.setName("blabla");
    item.setItemUnit("blabla");

    //Act
    itemRepository.save(item);

    Item itemSaved = itemRepository.findById(item.getId()).get();
    itemSaved.setName("newblabla");
    itemSaved.setItemUnit("newblabla");

    Item itemUpdated = itemRepository.save(itemSaved);

    //Assert
    Assertions.assertThat(itemUpdated.getItemName()).isEqualTo("newblabla");
    Assertions.assertThat(itemUpdated.getItemUnit()).isEqualTo("newblabla");
  }

  @Test
  public void itemRepositoryDeleteItemReturnItemIsEmpty() {
    //Arrange
    Item item = new Item();
    item.setName("blabla");
    item.setItemUnit("blabla");

    //Act
    itemRepository.save(item);

    itemRepository.deleteById(item.getId());

    Optional<Item> itemReturn = itemRepository.findById(item.getId());

    //Assert
    Assertions.assertThat(itemReturn).isEmpty();
  }
}
