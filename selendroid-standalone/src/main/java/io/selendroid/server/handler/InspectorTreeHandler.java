/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.server.handler;

import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.JsResult;
import io.selendroid.server.Response;
import io.selendroid.server.UiResponse;
import io.selendroid.server.model.ActiveSession;
import io.selendroid.server.util.HttpClientUtil;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

public class InspectorTreeHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(InspectorTreeHandler.class.getName());
  private ActiveSession session = null;

  public InspectorTreeHandler(HttpRequest request, String mappedUri) {
    super(request, mappedUri);
  }

  @Override
  public Response handle() throws JSONException {
    String sessionId = getSessionId();
    log.info("inspector tree handler, sessionId: " + sessionId);


    if (sessionId == null || sessionId.isEmpty() == true) {
      if (getSelendroidDriver().getActiveSessions() != null
          && getSelendroidDriver().getActiveSessions().size() >= 1) {
        session = getSelendroidDriver().getActiveSessions().get(0);
        log.info("Selected sessionId: " + session.getSessionKey());
      } else {
        return new UiResponse(
            "",
            "Selendroid inspector can only be used if there is an active test session running. "
                + "To start a test session, add a break point into your test code and run the test in debug mode.");
      }
    } else {
      session = getSelendroidDriver().getActiveSession(sessionId);
    }

    try {
      HttpResponse r =
          HttpClientUtil.executeRequest("http://localhost:" + session.getSelendroidServerPort()
              + "/inspector/tree", HttpMethod.GET);
      return new JsResult(EntityUtils.toString(r.getEntity()));
    } catch (Exception e) {
      e.printStackTrace();
      throw new SelendroidException(e);
    }
  }
}
