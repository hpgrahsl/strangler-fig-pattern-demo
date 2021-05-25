package com.github.hpgrahsl.kstreams.table.join.model;

public class Pet {

  public int id;
  public String name;
  public String birth_date; //TODO: check date handling in DBZ MYSQL or convert in app layer
  public int type_id;
  public int owner_id;

  @Override
  public String toString() {
    return "Pet{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", birth_date='" + birth_date + '\'' +
        ", type_id=" + type_id +
        ", owner_id=" + owner_id +
        '}';
  }

}
