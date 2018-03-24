package org.csgames.spaceship.control.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class Will allow the space ships data storage method to change while keeping the
 * interface the same.
 */
public interface DataStorage {

  // This will get the teams last known location. as “XX.XXXXXX,YY.YYYYYY”
  public String getLastLocation(String team);

  // This will set the teams location.
  public void updateTeamLocation(String team, String location);

  // Check to see if the resource has been encountered before.
  public boolean hasFoundResource (String resource);

  //this will mark the resource as found.
  public void foundResource(ResourceObject resource);

  // get a list of the team locations
  public Set<Map.Entry<String,String>> getAllTeamLocations();


  // this will store the telemetry data, and will return true if it needs to be logged
  public boolean recordTelemetry(String team, String key, String data);
}
