package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.Coordinates;

public class ToSend {
  public String team;
  public Coordinates coordinates;

  public ToSend(String team, Coordinates location) {
    this.team = team;
    this.coordinates = location;
  }
}
