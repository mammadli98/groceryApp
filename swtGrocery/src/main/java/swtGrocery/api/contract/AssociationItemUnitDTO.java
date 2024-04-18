package swtGrocery.api.contract;

import java.io.Serializable;

public class AssociationItemUnitDTO implements Serializable {

  private Long id;
  private final ItemDTO item;
  private final UnitDTO unit;

  public AssociationItemUnitDTO(Long id, ItemDTO item, UnitDTO unit) {
    super();
    this.id = id;
    this.item = item;
    this.unit = unit;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ItemDTO getItem() {
    return item;
  }

  public UnitDTO getUnit() {
    return unit;
  }
}
