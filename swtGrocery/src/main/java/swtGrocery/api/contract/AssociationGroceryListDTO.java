package swtGrocery.api.contract;

import java.io.Serializable;
import swtGrocery.backend.entities.AssociationItemUnit;
import swtGrocery.backend.entities.GroceryList;

public class AssociationGroceryListDTO implements Serializable {

  private Long id;
  private final AssociationItemUnit associationItemUnit;
  private final GroceryList groceryList;
  private final int quantity;
  private boolean isPurchased;

  public AssociationGroceryListDTO(
    Long id,
    AssociationItemUnit associationItemUnit,
    GroceryList groceryList,
    int quantity,
    boolean isPurchased
  ) {
    super();
    this.id = id;
    this.associationItemUnit = associationItemUnit;
    this.groceryList = groceryList;
    this.quantity = quantity;
    this.isPurchased = isPurchased;
  }

  public boolean isPurchased() {
    return isPurchased;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AssociationItemUnit getAssociationItemUnit() {
    return associationItemUnit;
  }

  public GroceryList getGroceryList() {
    return groceryList;
  }

  public int getQuantity() {
    return quantity;
  }

  public String toString() {
    return (
      groceryList.getName() + " " + associationItemUnit.getItem().getItemName()
    );
  }
}
