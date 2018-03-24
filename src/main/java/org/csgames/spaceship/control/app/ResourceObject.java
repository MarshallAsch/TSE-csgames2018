package org.csgames.spaceship.control.app;

public class ResourceObject {

  private String type;
  private String location;

  public ResourceObject(String type, String location) {
    this.location = location;
    this.type = type;
  }

  public String getType () {
    return type;
  }

  public String getLocation () {
    return location;
  }

}
