package com.github.hpgrahsl.kstreams.table.join.model;

public class Owner {

  public Integer id;
  public String first_name;
  public String last_name;
  public String address;
  public String city;
  public String telephone;

  @Override
  public String toString() {
    return "Owner{" +
        "id=" + id +
        ", first_name='" + first_name + '\'' +
        ", last_name='" + last_name + '\'' +
        ", address='" + address + '\'' +
        ", city='" + city + '\'' +
        ", telephone='" + telephone + '\'' +
        '}';
  }

}
