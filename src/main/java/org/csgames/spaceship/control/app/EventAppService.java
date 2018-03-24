package org.csgames.spaceship.control.app;

import com.sun.org.apache.xpath.internal.SourceTree;
import lombok.NonNull;
import org.csgames.spaceship.sdk.*;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import org.csgames.spaceship.sdk.service.TeamStatus;

import java.util.HashMap;
import java.util.*;



public class EventAppService {

  private SpaceshipSdk sdk;

  // This is the hashmap for the telemetry data
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
      case "CHECK_SHIP_TEMPERATURE":

        handleTemperature(eventDto);
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
        runAway(eventDto);
        break;

        default:
          // none
      }
    }


  // this will generate the key for the hashmap for storing telemetry
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

    Boolean foundResource = hasFoundResourse(event.source);

    if(!foundResource) {
      resources.add(new ResourceObject(event.source, event.payload));
      sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, event.source);
    }
  }



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

  private void runAway(EventDto event){
    teamsInRange(event.payload);
  }

  private void teamsInRange(String dangerLocation) {

    String location;
    String team;
    int distance;

    Coordinates danger = new Coordinates(Double.parseDouble(dangerLocation.split(",")[0]), Double.parseDouble(dangerLocation.split(",")[1]));
    Coordinates teamCoordinates;


    Direction direction;


    for (Map.Entry<String, String> entry: teamLocation.entrySet()) {

      location = entry.getValue();
      team = entry.getKey();

      teamCoordinates = new Coordinates(Double.parseDouble(location.split(",")[0]), Double.parseDouble(location.split(",")[1]));

      distance = sdk.getLocationService().distanceBetween(teamCoordinates, danger);

      if (distance < 1000) {

        direction = sdk.getLocationService().directionTo(teamCoordinates, danger);
        Direction opposite = getOppositeDirection(direction);

        sdk.getCommunicationService().moveTo(team, opposite, 1000);
      }

    }
  }


  private boolean hasFoundResourse(String resource){

    //loops through the arrayList and compares the resources
    for(int i = 0; i < resources.size(); i ++){
      if(resources.get(i).getType().equals(resource)){
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

  private void handleTemperature(EventDto event){/*Deals with the ship's temperature control*/
    SpaceshipBlueprint blueprint = sdk.getSpaceshipService().readBlueprint();
    SpaceshipRoom room;
    double temperature = 0.0;

    for(int i = 0; i < blueprint.rooms.size(); i++){/*loop through all the rooms*/
      room = blueprint.rooms.get(i);/*Store the current room*/
      /*Attempt to get the temperature of the current room*/
      if(room.type.equals("habitable")) {/*if a penguin can live in this room*/

        try {
          temperature = sdk.getSpaceshipService().readRoomTemperature(room.roomNumber);
          if(sdk.getSpaceshipService().roomTemperatureSensorUnit(room.roomNumber).equals("P")){/*in penguin degrees*/
            System.out.println("temperature before: " + temperature + " room number: " + room.roomNumber);
            temperature = (((temperature * 9) / (double) 5) + 32);/*convert to celsius*/
            System.out.println("temperature after: " + temperature + " room number: " + room.roomNumber);
          }
        } catch (TemperatureSensorNotWorkingException e) {
          temperature = sdk.getSpaceshipService().readMeanHabitableTemperature();
        }

        if (temperature > 0) {/*It's too hot, so open door, vents, and turn on AC*/
          sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
          sdk.getSpaceshipService().openDoor(room.roomNumber);
          sdk.getSpaceshipService().openVent(room.roomNumber);

        } else if (temperature > -10) {/*It's too hot, so open doors and vents*/
          //System.out.println("> -10 " + temperature + "room number: " + room.roomNumber+"\n");
          sdk.getSpaceshipService().openDoor(room.roomNumber);
          sdk.getSpaceshipService().openVent(room.roomNumber);

        } else if (temperature < -10) {/*it's too cold so close everything*/
         // System.out.println("< -10 " + temperature + "room number: " + room.roomNumber+"\n");
          sdk.getSpaceshipService().closeAirConditioning(room.roomNumber);
          sdk.getSpaceshipService().closeDoor(room.roomNumber);
          sdk.getSpaceshipService().closeVent(room.roomNumber);

        }else if (temperature < -20){/*it's too cold so close everything except air conditioning*/

          sdk.getSpaceshipService().closeDoor(room.roomNumber);
          sdk.getSpaceshipService().closeVent(room.roomNumber);
          sdk.getSpaceshipService().closeAirConditioning(room.roomNumber);

        }
      } else {/*Cannot live in this room (freezer room)*/
        try {
          temperature = sdk.getSpaceshipService().readRoomTemperature(room.roomNumber);
          if(sdk.getSpaceshipService().roomTemperatureSensorUnit(room.roomNumber).equals("P")){/*in penguin degrees*/
            temperature = (temperature * 9 / 5 + 32);/*convert to celsius*/
          }
          if(temperature > -15){/*Room's too hot, open AC*/
            sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
          } else if (temperature < -22){
            sdk.getSpaceshipService().closeAirConditioning(room.roomNumber);
          }
        } catch (TemperatureSensorNotWorkingException e) {
          sdk.getSpaceshipService().openAirConditioning(room.roomNumber);
        }
      }
    }

  }

}


