package swtGrocery.backend.entities;

import javax.persistence.*;

@Entity
public class Item {

  @Id
  @GeneratedValue
  private Long id;

  private String itemName;
  private String itemUnit;
  private int itemQuantity;

  private String category;

  public Item() {
    super();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getItemName() {
    return this.itemName;
  }

  public void setName(String name) {
    this.itemName = name;
  }

  public String getItemUnit() {
    return this.itemUnit;
  }

  public void setItemUnit(String itemUnit) {
    this.itemUnit = itemUnit;
  }

  public int getItemQuantity() {
    return itemQuantity;
  }

  public void setItemQuantity(int itemQuantity) {
    this.itemQuantity = itemQuantity;
  }

  public String getItemCategory() {
    return this.category;
  }

  public void setCategory(String category) {
    this.category = category;
  }
}
