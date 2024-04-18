package swtGrocery.backend.entities;

import javax.persistence.*;
import org.hibernate.annotations.ForeignKey;

@Entity
public class Unit {

  @Id
  @GeneratedValue
  private Long id;

  private String unitName;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUnitName() {
    return this.unitName;
  }

  public void setName(String name) {
    this.unitName = name;
  }
}
