package swtGrocery.backend.entities;

import javax.persistence.*;

@Entity
public class AssociationGroceryListItemUnit {

  @Id
  @GeneratedValue
  private Long id;

  @OneToOne
  private AssociationItemUnit associationItemUnit;

  @OneToOne
  private GroceryList groceryList;

  private int quantity;
  private boolean isPurchased;

  public boolean getisPurchased() {
    return isPurchased;
  }

  public void setisPurchased(boolean purchased) {
    isPurchased = purchased;
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

  public void setAssociationItemUnit(AssociationItemUnit associationItemUnit) {
    this.associationItemUnit = associationItemUnit;
  }

  public GroceryList getGroceryList() {
    return groceryList;
  }

  public void setGroceryList(GroceryList groceryList) {
    this.groceryList = groceryList;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }
}
