package org.csgames.spaceship.control.app;

import lombok.NonNull;
import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import java.util.HashMap;
import java.util.*;



public class EventAppService {

  private SpaceshipSdk sdk;

  // This is the hashmap for the telemetry data
  private HashMap<String, String> telemetry = new HashMap<>();

  // this is the ArrayList that will store the locations of resources
  private ArrayList<ResourceObject> resources = new ArrayList<>();

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

    Boolean foundResource = false;

    //loops through the arrayList and compares the resources
    for(int i = 0; i < resources.size(); i ++){
      if(resources.get(i).getType().equals(event.source)){
        foundResource = true;
        break;
      }
    }

    if(!foundResource) {
      resources.add(new ResourceObject(event.source, event.payload));
      sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, event.source);
    }
  }

}


