package swtGrocery.api.contract;

import java.io.Serializable;

public class GroceryListDTO implements Serializable {

  private Long id;
  private final String name;

  public GroceryListDTO(Long id, String name) {
    super();
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }
}
