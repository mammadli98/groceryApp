package swtGrocery.backend.entities;

import javax.persistence.*;

@Entity
public class AssociationItemUnit {

  @Id
  @GeneratedValue
  private Long id;

  @OneToOne
  private Item item;

  @OneToOne
  private Unit unit;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }
}
