package com.takeyuweb.dataapiexample;

import com.fasterxml.jackson.databind.JsonNode;
import com.takeyuweb.util.ParseJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by uzuki05 on 14/01/24.
 */
public class ParseEntries extends ParseJson {
    private List<MTEntry> entries = new ArrayList<MTEntry>();
    public List<MTEntry> getEntries() {
        return entries;
    }

    @Override
    public void loadJson(String str) {
        JsonNode root = getJsonNode(str);
        if (root != null) {
            if (root.has("items")) {
                Iterator<JsonNode> ite =
                        root.path("items").elements();
                while (ite.hasNext()) {
                    JsonNode j = ite.next();
                    MTEntry MTEntry = new MTEntry();
                    MTEntry.id = j.path("id").asInt();
                    MTEntry.title = j.path("title").asText();
                    MTEntry.lat = j.path("lat").asDouble();
                    MTEntry.lng = j.path("lng").asDouble();
                    MTEntry.excerpt = j.path("excerpt").asText();
                    MTEntry.permalink = j.path("permalink").asText();
                    entries.add(MTEntry);
                }
            } else {
                MTEntry MTEntry = new MTEntry();
                MTEntry.id = root.path("id").asInt();
                MTEntry.title = root.path("title").asText();
                MTEntry.lat = root.path("lat").asDouble();
                MTEntry.lng = root.path("lng").asDouble();
                MTEntry.excerpt = root.path("excerpt").asText();
                MTEntry.permalink = root.path("permalink").asText();
                entries.add(MTEntry);
            }
        }
    }
}
