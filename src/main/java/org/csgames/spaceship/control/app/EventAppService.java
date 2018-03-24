package org.csgames.spaceship.control.app;

import lombok.NonNull;
import org.csgames.spaceship.sdk.Coordinates;
import org.csgames.spaceship.sdk.Direction;
import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import org.csgames.spaceship.sdk.service.TeamStatus;
import java.util.HashMap;
import java.util.*;


public class EventAppService {

  private SpaceshipSdk sdk;

  // This is the HashMap for the telemetry data
  private HashMap<String, String> telemetry = new HashMap<>();

  // this is the ArrayList that will store the locations of resources
  private ArrayList<ResourceObject> resources = new ArrayList<>();

  // the locations of all of the teams
  private HashMap<String, String> teamLocation = new HashMap<>();
  
  public EventAppService(SpaceshipSdk sdk) {
    this.sdk = sdk;
  }

  public void handleReceivedEvent(EventDto eventDto) {

    // FIXME: Implement core logic to handle received events

    switch (eventDto.type) {

      case "RESOURCE_DISCOVERED":
        handleResourcesDiscovered(eventDto);
        break;
      case "DATA_MEASURED":
        // handle event telemetry, record if needed
        handleTelemetry(eventDto);
        break;
      case "SUPPLY_CONSUMED":
        // send more supplies if needed
        handleSuppliesConsumed(eventDto);
        break;
      case "OUT_OF_SUPPLIES":
        // send supplies to starving penguins.
        handleOutOfSupplies(eventDto);
        break;
      case "LOCATION_CHANGED":
        handleTrackLocation(eventDto);
        break;
      case "PREDATORS_DETECTED":
        handleDanger(eventDto);
        break;
      default:
        // none
      }
    }


  // this will generate the key for the HashMap for storing telemetry
  private @NonNull String genTelemetryKey(EventDto event) {

    String type = event.type;
    String team = event.source;
    String[] data =  event.payload.split("=");

    String key = data.length == 0 ? "" : data[0];


    String hashKey = type + team + key;

    return hashKey;

  }

  // This will send 20 fish and 15 units water
  private void handleOutOfSupplies (EventDto event) {

    sdk.getSpaceshipService().sendFishTo(event.source, 20);
    sdk.getSpaceshipService().sendWaterTo(event.source, 15);
  }

  // This will send more supplies to the penguins when they are needed
  private void handleSuppliesConsumed (EventDto event)
  {

    String[] parts = event.payload.split(",");

    // make sure that the parts has values
    if (parts.length != 2) {
      parts = new String[]{"0", "0"};
    }

    int remainingFish = Integer.parseInt(parts[0]);
    int remainingWater = Integer.parseInt(parts[1]);

    // send food if needed
    if (remainingFish < 5)
      sdk.getSpaceshipService().sendFishTo(event.source, 15);

    // send water if needed
    if (remainingWater < 2)
      sdk.getSpaceshipService().sendWaterTo(event.source, 10);

  }

  // will lof event types when one is received if it updates or if it is new
  private void handleTelemetry(EventDto event) {

    String dataKey = genTelemetryKey(event);

    // save the data
    String dataName = event.payload.split("=")[0];

    if (telemetry.containsKey(dataKey) && telemetry.get(dataKey).equals(dataName)) {
      // the data is already there do nothing
    }
    else if (telemetry.containsKey(dataKey) && !telemetry.get(dataKey).equals(dataName)) {
      // the data is already there the value needs to be updated
      sdk.getAwayTeamLogService().reportMeasureData(event.source, dataName);
      telemetry.replace(dataKey, event.payload);
    }
    else {
      sdk.getAwayTeamLogService().reportMeasureData(event.source, dataName);
      telemetry.put(dataKey, event.payload);
    }
  }


  // when a new resource is discovered, add it to the log and save its location
  private void handleResourcesDiscovered (EventDto event) {

    Boolean foundResource = hasFoundResource(event.source);

    if(!foundResource) {
      resources.add(new ResourceObject(event.source, event.payload));
      sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, event.source);
    }
  }

  // Handel teh event when a team moves
  private void handleTrackLocation(EventDto event) {

    String team = event.source;
    String location  = event.payload;

    String lastLocation = getLastLocation(team);

    if (lastLocation.equals("")) {
      // new location
      teamLocation.put(team, location);
    }
    else if (lastLocation.equals(location)) {
      // same location
      sdk.getAwayTeamLogService().reportStatus(team, TeamStatus.STATIONARY);
    }
    else {
      // moved
      sdk.getAwayTeamLogService().reportStatus(team, TeamStatus.MOVING);
      teamLocation.replace(team, location);
    }
  }


  private String getLastLocation(String team) {

    return teamLocation.getOrDefault(team, "");
  }

  private void handleDanger(EventDto event){
    teamsInRange(event.payload);
  }

  private void teamsInRange(String dangerLocation) {

    String location;
    String team;
    int distance;
    Direction direction;

    double latitude;
    double longitude;

    Coordinates teamCoordinates;
    Coordinates danger;

    latitude = Double.parseDouble(dangerLocation.split(",")[0]);
    longitude = Double.parseDouble(dangerLocation.split(",")[1]);

    danger = new Coordinates(latitude, longitude);


    // check to see which teams are too close to the danger
    for (Map.Entry<String, String> entry: teamLocation.entrySet()) {

      location = entry.getValue();
      team = entry.getKey();

      // get the coordinates
      latitude = Double.parseDouble(location.split(",")[0]);
      longitude = Double.parseDouble(location.split(",")[1]);

      teamCoordinates = new Coordinates(latitude, longitude);
      distance = sdk.getLocationService().distanceBetween(teamCoordinates, danger);


      // check to see if the team needs to moved
      if (distance < 1000) {

        direction = sdk.getLocationService().directionTo(teamCoordinates, danger);
        Direction opposite = getOppositeDirection(direction);

        sdk.getCommunicationService().moveTo(team, opposite, 1000);
      }
    }
  }


  // Check to see if the resource has been encountered before.
  private boolean hasFoundResource (String resource){

    //loops through the arrayList and compares the resources
    for (ResourceObject resource1 : resources) {
      if (resource1.getType().equals(resource)) {
        return true;
      }
    }

    return false;
  }

  // helper function that will tell you what direction the team should move in.
  private Direction getOppositeDirection (Direction direction){

    switch(direction){
      case NORTH:
        return Direction.SOUTH;
      case SOUTH:
        return Direction.NORTH;
      case WEST:
        return Direction.EAST;
      case EAST:
        return Direction.SOUTH;
      case NORTH_EAST:
        return Direction.SOUTH_WEST;
      case NORTH_WEST:
        return Direction.SOUTH_EAST;
      case SOUTH_EAST:
        return Direction.NORTH_WEST;
      case SOUTH_WEST:
        return Direction.NORTH_EAST;
      default:
        return Direction.NONE;
    }
  }
}


