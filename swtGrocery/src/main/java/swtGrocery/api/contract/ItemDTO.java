package swtGrocery.api.contract;

import java.io.Serializable;

public class ItemDTO implements Serializable {

  private Long id;

  private final String itemName;
  private final int itemQuantity;

  private final String itemCategory;

  public ItemDTO(
    Long id,
    String itemName,
    int itemQuantity,
    String itemCategory
  ) {
    super();
    this.id = id;
    this.itemName = itemName;
    this.itemQuantity = itemQuantity;
    this.itemCategory = itemCategory;
  }

  public Long getId() {
    return this.id;
  }

  public String getItemName() {
    return this.itemName;
  }

  public int getItemQuantity() {
    return this.itemQuantity;
  }

  public String getItemCategory() {
    return this.itemCategory;
  }
}
