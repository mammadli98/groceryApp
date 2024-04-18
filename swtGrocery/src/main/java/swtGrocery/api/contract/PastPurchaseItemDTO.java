package swtGrocery.api.contract;

import java.io.Serializable;
import java.time.LocalDate;

public class PastPurchaseItemDTO implements Serializable {

  private Long id;
  private final String itemName;
  private final String itemUnit;
  private final int itemQuantity;
  private final LocalDate itemPurchaseDate;

  private final String itemCategory;

  public PastPurchaseItemDTO(
    Long id,
    String itemName,
    String itemUnit,
    int itemQuantity,
    LocalDate itemPurchaseDate,
    String itemCategory
  ) {
    super();
    this.id = id;
    this.itemName = itemName;
    this.itemUnit = itemUnit;
    this.itemQuantity = itemQuantity;
    this.itemPurchaseDate = itemPurchaseDate;
    this.itemCategory = itemCategory;
  }

  public Long getId() {
    return id;
  }

  public String getItemName() {
    return itemName;
  }

  public String getItemUnit() {
    return itemUnit;
  }

  public int getItemQuantity() {
    return itemQuantity;
  }

  public LocalDate getItemPurchaseDate() {
    return itemPurchaseDate;
  }

  public String getCategory() {
    return itemCategory;
  }
}
