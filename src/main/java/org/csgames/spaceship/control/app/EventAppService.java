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
        switch(eventDto.type) {

          case "RESOURCE_DISCOVERED":
            //Set a variable?


            sdk.getPlanetResourceService().registerResource(PlanetRegistry.CLASS_M, eventDto.source);
            break;
          }

         }

  }
