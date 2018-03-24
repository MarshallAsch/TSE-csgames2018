package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.*;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import org.csgames.spaceship.sdk.service.TeamStatus;
import java.util.*;


public class EventAppService {

  private SpaceshipSdk sdk;
  private DataStorage dataStorage = new LocalStorage();

  public EventAppService(SpaceshipSdk sdk) {
    this.sdk = sdk;
  }

  public void handleReceivedEvent(EventDto eventDto) {
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
        case "CHECK_SHIP_TEMPERATURE":
          handleTemperature(eventDto);
          break;
      default:
        // none
      }
    }


  // This will send 20 fish and 15 units water
  private void handleOutOfSupplies (EventDto event) {

    try {
      sdk.getSpaceshipService().sendFishTo(event.source, 20);
    } catch (SpaceshipOutOfSuppliesException e) {

      Coordinates fish = dataStorage.getFishLocation();
      if (fish != null)
      sdk.getCommunicationService().catchFish(event.source, fish);
    }

    try {
      sdk.getSpaceshipService().sendWaterTo(event.source, 15);
    } catch (SpaceshipOutOfSuppliesException e) {
      Coordinates water = dataStorage.getWaterLocation();
      if (water != null)
        sdk.getCommunicationService().refillWater (event.source, water);
    }
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
    if (remainingFish < 5) {

      try{
        sdk.getSpaceshipService().sendFishTo(event.source, 15);
      } catch (SpaceshipOutOfSuppliesException e) {
        Coordinates fish = dataStorage.getFishLocation();
        if (fish != null)

          sdk.getCommunicationService().catchFish(event.source, fish);
      }
    }

    // send water if needed
    if (remainingWater < 2) {
      try {
        sdk.getSpaceshipService().sendWaterTo(event.source, 10);
      } catch (SpaceshipOutOfSuppliesException e) {

        Coordinates water = dataStorage.getWaterLocation();
        if (water != null)
          sdk.getCommunicationService().refillWater (event.source, water);
      }

    }

  }

  // will lof event types when one is received if it updates or if it is new
  private void handleTelemetry(EventDto event) {

    // save the data
    String dataName = event.payload.split("=")[0];
    String dataValue = event.payload.split("=")[1];

    if (dataStorage.recordTelemetry(event.source, dataName, dataValue)) {
      sdk.getAwayTeamLogService().reportMeasureData(event.source, dataName);
    }
  }


  // when a new resource is discovered, add it to the log and save its location
  private void handleResourcesDiscovered (EventDto event) {

    Boolean foundResource = dataStorage.hasFoundResource(event.source);

    if(!foundResource) {
      dataStorage.foundResource(new ResourceObject(event.source, event.payload));
      sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, event.source);
    }
  }

  // handle the event when a team moves
  private void handleTrackLocation(EventDto event) {

    String team = event.source;
    String location  = event.payload;

    String lastLocation = dataStorage.getLastLocation(team);

    if (lastLocation.equals("")) {
      // new location
    }
    else if (lastLocation.equals(location)) {
      // same location
      sdk.getAwayTeamLogService().reportStatus(team, TeamStatus.STATIONARY);
    }
    else {
      // moved
      sdk.getAwayTeamLogService().reportStatus(team, TeamStatus.MOVING);
    }

    // update the location of the team
    dataStorage.updateTeamLocation(team, location);
  }

  private void handleDanger(EventDto event){
    teamsInRange(event.payload);
  }

  // This will send the instruction to all of the teams that are within range of the danger
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
    for (Map.Entry<String, String> entry: dataStorage.getAllTeamLocations()) {

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

  private void handleTemperature(EventDto event){/*Deals with the ship's temperature control*/
    SpaceshipBlueprint blueprint = sdk.getSpaceshipService().readBlueprint();

    double tempature;

    boolean failed;

    for(SpaceshipRoom room : blueprint.rooms)
    {
      failed = false;
      try {
        tempature = sdk.getSpaceshipService().readRoomTemperature(room.roomNumber);

        // convert penguin temp to celsius
        if (sdk.getSpaceshipService().roomTemperatureSensorUnit(room.roomNumber).equals("P")) {
          tempature =  - tempature * 9 / (double) 5 + 32;
        }
      } catch (TemperatureSensorNotWorkingException e) {
        tempature = sdk.getSpaceshipService().readMeanHabitableTemperature();
        failed = true;
      }

      if (room.type.equals("habitable"))
      {
        // temp is now in C
        if (tempature > -10) {
          sdk.getSpaceshipService().openDoor(room.roomNumber);
          sdk.getSpaceshipService().openVent(room.roomNumber);
        }

        if (tempature < -20) {
          sdk.getSpaceshipService().closeDoor(room.roomNumber);
          sdk.getSpaceshipService().closeVent(room.roomNumber);
        }

        if (tempature > 0) {
          sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
        }

        if (tempature < -10) {
          sdk.getSpaceshipService().closeAirConditioning(room.roomNumber);
        }
      }
      else {

        if (tempature > -15) {
          sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
        }

        if (tempature < -22) {
          sdk.getSpaceshipService().closeAirConditioning(room.roomNumber);
        }

        if (failed) {
          sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
        }
      }
    }
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


