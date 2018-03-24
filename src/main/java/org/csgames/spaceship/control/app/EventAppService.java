package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.Direction;
import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import org.csgames.spaceship.sdk.service.PlanetResourceService;

import java.util.*;

public class EventAppService {

  private SpaceshipSdk sdk;

  private ArrayList<String> resources = new ArrayList<>();

  public EventAppService(SpaceshipSdk sdk) {
    // FIXME: Use the sdk to retrieve the available services we made for you

    this.sdk = sdk;


  }

  public void handleReceivedEvent(EventDto eventDto) {
    // FIXME: Implement core logic to handle received events
      Boolean foundResource = false;
      int remainingFish = 0;
      int remainingWater = 0;
      String predatorLocation;
      String Latitude, Longitude, Coordinates;
      int latitudeDistance, longitudeDistance, coordinateDistance;


      switch (eventDto.type) {

        case "PREDATORS_DETECTED":

         runAway(eventDto);

          break;

        case "RESOURCE_DISCOVERED":
          //Set a variable

          //loops through the arraylist and compares the resources
          for(int i = 0; i < resources.size(); i ++){
            if(resources.get(i).equals(eventDto.source)){
              foundResource = true;
            }
          }

          if(!foundResource) {
            resources.add(eventDto.source);
            sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, eventDto.source);
          }
          break;

        case "SUPPLY_CONSUMED":

          String[] parts = eventDto.payload.split(",");

          if (parts == null || parts.length == 0) {
            parts = new String[]{"0", "0"};
          }

          remainingFish = Integer.parseInt(parts[0]);
          remainingWater = Integer.parseInt(parts[1]);

          if (remainingFish < 5)
            sdk.getSpaceshipService().sendFishTo(eventDto.source, 15);

          if (remainingWater < 2)
            sdk.getSpaceshipService().sendWaterTo(eventDto.source, 10);

          break;

        case "OUT_OF_SUPPLIES":

          sdk.getSpaceshipService().sendFishTo(eventDto.source, 20);

          sdk.getSpaceshipService().sendWaterTo(eventDto.source, 15);

          break;
        default:
          // none
      }
    }

    private void runAway(EventDto event){

      String predatorLocation;
      String Latitude, Longitude, Coordinates, coordinateDirection, newDirection;
      int latitudeDistance, longitudeDistance, coordinateDistance;

      Coordinates = event.payload;
        /*
            Latitude = eventDto.payload.split(",")[0];
            Longitude = eventDto.payload.split("\n")[0];

          latitudeDistance = sdk.getLocationService().distanceBetween(team coordinates, Latitude);
          longitudeDistance = sdk.getLocationService().distanceBetween(team cooordinates, Longitude);
        */
      coordinateDistance = sdk.getLocationService().distanceBetween(/*team coordinates*/, Coordinates);
          /*
            if(latitudeDistance < 1000 && longitudeDistance < 1000){
              latitudeDistance = sdk.getLocationService().directionTo(team coordinates, Latitude);
              longitudeDistance = sdk.getLocationService().directionTo(team cooordinates, Longitude);
            }*/

      if(coordinateDistance < 1000){

        coordinateDirection = sdk.getLocationService().directionTo(/*team coordinates*/, Coordinates);

        oppositeDirection(coordinateDirection, Coordinates);


      }

    }


    private void oppositeDirection(String direction, String Coordinates){
      String reverseDirection;

      switch(direction){

        case "NORTH":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.SOUTH, 1000);

          break;

        case "SOUTH":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.NORTH, 1000);

          break;

        case "WEST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.EAST, 1000);

          break;

        case "EAST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.WEST, 1000);

          break;

        case "NORTH_EAST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.SOUTH_WEST, 1000);

          break;

        case "NORTH_WEST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.SOUTH_EAST, 1000);

          break;

        case "SOUTH_EAST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.NORTH_WEST, 1000);

          break;

        case "SOUTH_WEST":

          sdk.getCommunicationService().moveTo(Coordinates, Direction.NORTH_EAST, 1000);

          break;

      }
    }

}
