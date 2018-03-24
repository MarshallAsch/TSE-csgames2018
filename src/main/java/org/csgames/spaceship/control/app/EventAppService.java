package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.SpaceshipSdk;

public class EventAppService {

  private SpaceshipSdk sdk;
  public EventAppService(SpaceshipSdk sdk) {

    this.sdk = sdk;
  }

  public void handleReceivedEvent(EventDto eventDto) {
    // FIXME: Implement core logic to handle received events

    int remainingFish = 0;
    int remainingWater = 0;


    switch (eventDto.type) {
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
      default:
        // none
    }
  }
}
