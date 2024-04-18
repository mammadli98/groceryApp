package swtGrocery.api.contract;

import java.io.Serializable;

public class UnitDTO implements Serializable {

  private Long id;
  private final String unitName;

  public UnitDTO(Long id, String unitName) {
    super();
    this.id = id;
    this.unitName = unitName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUnitName() {
    return unitName;
  }
}
