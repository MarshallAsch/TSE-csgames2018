package org.csgames.spaceship.control.app;

import lombok.NonNull;
import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.AwayTeamLogService;
import org.csgames.spaceship.sdk.service.PlanetRegistry;

import java.util.HashMap;

public class EventAppService {

  private SpaceshipSdk sdk;

  public EventAppService(SpaceshipSdk sdk) {
    this.sdk = sdk;
  }

  private HashMap<String, String> telemetry = new HashMap<>();

  public void handleReceivedEvent(EventDto eventDto) {

    int remainingFish = 0;
    int remainingWater = 0;


    switch (eventDto.type) {

      case "DATA_MEASURED":

        String dataKey = genTelemetryKey(eventDto);


        // save the data
        String dataName = eventDto.payload.split("=")[0];

        if (telemetry.containsKey(dataKey) && telemetry.get(dataKey).equals(dataName)) {
          // the data is already there do nothing
        }
        else if (telemetry.containsKey(dataKey) && !telemetry.get(dataKey).equals(dataName)) {
          // the data is already there the value needs to be updated
          sdk.getAwayTeamLogService().reportMeasureData(eventDto.source, dataName);
          telemetry.replace(dataKey, eventDto.payload);
        }
        else {
          sdk.getAwayTeamLogService().reportMeasureData(eventDto.source, dataName);
          telemetry.put(dataKey, eventDto.payload);
        }


        break;
      case "SUPPLY_CONSUMED":

        String[] parts = eventDto.payload.split(",");

        // make sure that the parts has values
        if (parts.length != 2) {
          parts = new String[]{"0", "0"};
        }

        remainingFish = Integer.parseInt(parts[0]);
        remainingWater = Integer.parseInt(parts[1]);

        // send food if needed
        if (remainingFish < 5)
          sdk.getSpaceshipService().sendFishTo(eventDto.source, 15);

        // send water if needed
        if (remainingWater < 2)
          sdk.getSpaceshipService().sendWaterTo(eventDto.source, 10);

        break;

      case "OUT_OF_SUPPLIES":

        // send supplies to starving penguins.
        sdk.getSpaceshipService().sendFishTo(eventDto.source, 20);
        sdk.getSpaceshipService().sendWaterTo(eventDto.source, 15);

        break;
      default:
        // none
    }
  }


  // this will generate the key for the hashmap for storing telemtry
  private @NonNull String genTelemetryKey(EventDto event) {

    String type = event.type;
    String team = event.source;
    String[] data =  event.payload.split("=");

    String key = data.length == 0 ? "" : data[0];


    String hashKey = type + team + key;

    return hashKey;

  }
}
