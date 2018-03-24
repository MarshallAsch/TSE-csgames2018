package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.Coordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalStorage implements DataStorage {


  // This is the HashMap for the telemetry data
  private HashMap<String, String> telemetry = new HashMap<>();

  // this is the ArrayList that will store the locations of resources
  private ArrayList<ResourceObject> resources = new ArrayList<>();

  // the locations of all of the teams
  private HashMap<String, String> teamLocation = new HashMap<>();


  public LocalStorage()
  { }

  // This will get the teams last known location. as “XX.XXXXXX,YY.YYYYYY”
  public String getLastLocation(String team) {
    return teamLocation.getOrDefault(team, "");
  }

  // This will set the teams location.
  public void updateTeamLocation(String team, String location) {
    teamLocation.put(team, location);
  }

  // Check to see if the resource has been encountered before.
  public boolean hasFoundResource (String resource){

    //loops through the arrayList and compares the resources
    for (ResourceObject resource1 : resources) {
      if (resource1.getType().equals(resource)) {
        return true;
      }
    }

    return false;
  }

  //this will mark the resource as found.
  public void foundResource(ResourceObject resource) {
    resources.add(resource);
  }

  // get a list of the team locations
  public Set<Map.Entry<String,String>> getAllTeamLocations()
  {
    return teamLocation.entrySet();
  }


  // this will store the telemetry data, and will return true if it needs to be logged
  public boolean recordTelemetry(String team, String key, String data)
  {
    String mapKey = team + key;
    String oldVal = telemetry.put(mapKey, data);

    return oldVal == null || !oldVal.equals(data);
  }



  public Coordinates getFishLocation()
  {
    String location;
    double latitude;
    double longitude;

    for (ResourceObject resource : resources) {

      if (resource.getType().equals("Fish")) {
        location = resource.getLocation();

        latitude = Double.parseDouble(location.split(",")[0]);
        longitude = Double.parseDouble(location.split(",")[1]);

        return new Coordinates(latitude, longitude);
      }
    }

    return null;
  }

  public Coordinates getWaterLocation()
  {
    String location;
    double latitude;
    double longitude;

    for (ResourceObject resource : resources) {

      if (resource.getType().equals("Water")) {
        location = resource.getLocation();

        latitude = Double.parseDouble(location.split(",")[0]);
        longitude = Double.parseDouble(location.split(",")[1]);

        return new Coordinates(latitude, longitude);
      }
    }
    return null;
  }
}
