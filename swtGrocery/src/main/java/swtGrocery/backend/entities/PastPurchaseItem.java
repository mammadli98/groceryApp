package swtGrocery.backend.entities;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class PastPurchaseItem {

  @Id
  @GeneratedValue
  private Long id;

  private String itemName;
  private String itemUnit;
  private int itemQuantity;
  private LocalDate itemPurchaseDate;

  private String category;

  public void setCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return this.category;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public String getItemUnit() {
    return itemUnit;
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

  public LocalDate getItemPurchaseDate() {
    return itemPurchaseDate;
  }

  public void setItemPurchaseDate(LocalDate itemPurchaseDate) {
    this.itemPurchaseDate = itemPurchaseDate;
  }
}
