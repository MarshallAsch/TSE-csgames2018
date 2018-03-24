package org.csgames.spaceship.control.app;

import org.csgames.spaceship.sdk.SpaceshipSdk;
import org.junit.Test;

public class EventAppServiceTest {

  private SpaceshipSdk sdk;

  @Test
  public void test() {
    // FIXME: Test the event app service

    //F4 test data
    EventDto event = new EventDto("OUT_OF_SUPPLIES", "away-team-01", "");


    EventAppService testF4 = new EventAppService(sdk);

  }
}
