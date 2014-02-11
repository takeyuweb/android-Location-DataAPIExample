package com.takeyuweb.dataapiexample;

import com.fasterxml.jackson.databind.JsonNode;
import com.takeyuweb.util.ParseJson;

/**
 * Created by uzuki05 on 14/02/09.
 */
public class ParseSession extends ParseJson {
    private MTSession session = null;
    public MTSession getSession(){ return session; }
    @Override
    public void loadJson(String str) {
        JsonNode root = getJsonNode(str);
        if (root != null) {
            MTSession session = new MTSession();
            session.sessionId = root.path("sessionId").asText();
            session.accessToken = root.path("accessToken").asText();
            session.expiresIn = root.path("expiresIn").asInt();
            session.remember = root.path("remember").asBoolean();
            this.session = session;
        }
    }
}
