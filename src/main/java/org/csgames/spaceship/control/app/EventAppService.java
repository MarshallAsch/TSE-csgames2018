package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.csgames.spaceship.sdk.service.AwayTeamLogService;
import org.csgames.spaceship.sdk.service.PlanetRegistry;

import java.util.HashMap;

public class EventAppService {

  private SpaceshipSdk sdk;


  public EventAppService(SpaceshipSdk sdk) {
    this.sdk = sdk;
  }

  public void handleReceivedEvent(EventDto eventDto) {

    AwayTeamLogService awayTeamLogService =  sdk.getAwayTeamLogService();

      switch (eventDto.type) {
        case "DATA_MEASURED":

          // save the data
          awayTeamLogService.reportMeasureData(eventDto.source, eventDto.payload);
          break;
        default:
          //none

      }
  }
}
