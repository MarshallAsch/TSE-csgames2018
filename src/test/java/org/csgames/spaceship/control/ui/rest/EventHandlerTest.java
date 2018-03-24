package org.csgames.spaceship.control.ui.rest;

import com.google.gson.Gson;

import org.csgames.spaceship.control.app.EventAppService;
import org.csgames.spaceship.control.app.EventDto;
import org.junit.Before;
import org.junit.Test;

import spark.Request;
import spark.Response;

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EventHandlerTest {

  private static final String AN_EVENT_TYPE = "EVENT_TYPE";
  private static final String AN_EVENT_SOURCE = "team-01";
  private static final String AN_EVENT_PAYLOAD = "EVENT_PAYLOAD";

  private final EventDto anEvent = new EventDto(AN_EVENT_TYPE, AN_EVENT_SOURCE, AN_EVENT_PAYLOAD);

  private Gson gson;
  private EventAppService eventAppService;

  private Request request;
  private Response response;

  private EventHandler eventHandler;

  @Before
  public void setUp() throws Exception {
    gson = new Gson();
    eventAppService = mock(EventAppService.class);

    eventHandler = new EventHandler(gson, eventAppService);
  }

  @Before
  public void prepareReceivedEvent() throws Exception {
    request = mock(Request.class);
    response = mock(Response.class);

    String requestBody = gson.toJson(anEvent);
    willReturn(requestBody).given(request).body();
  }

  @Test
  public void handleReceivedEvent() throws Exception {
    eventHandler.handle(request, response);

    verify(eventAppService).handleReceivedEvent(anEvent);

  }
}
