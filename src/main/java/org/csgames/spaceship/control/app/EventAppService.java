package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.SpaceshipSdk;

public class EventAppService {

  private SpaceshipSdk sdk;

  public EventAppService(SpaceshipSdk sdk) {
    // FIXME: Use the sdk to retrieve the available services we made for you

    this.sdk = sdk;

  }

  public void handleReceivedEvent(EventDto eventDto) {
    // FIXME: Implement core logic to handle received events

    switch(eventDto.type){

      case "OUT_OF_SUPPLIES":

        sdk.getSpaceshipService().sendFishTo(eventDto.source, 20);

        sdk.getSpaceshipService().sendWaterTo(eventDto.source, 15);

        break;


    }

  }
}
