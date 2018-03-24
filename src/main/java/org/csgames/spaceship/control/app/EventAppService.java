package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.PlanetRegistry;
import org.csgames.spaceship.sdk.service.PlanetResourceService;

public class EventAppService {

  private SpaceshipSdk sdk;

  public EventAppService(SpaceshipSdk sdk) {
    // FIXME: Use the sdk to retrieve the available services we made for you

    this.sdk = sdk;


  }

  public void handleReceivedEvent(EventDto eventDto) {
    // FIXME: Implement core logic to handle received events
      int remainingFish = 0;
      int remainingWater = 0;


      switch (eventDto.type) {

        case "RESOURCE_DISCOVERED":
          //Set a variable?


          sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, eventDto.source);
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
  }
