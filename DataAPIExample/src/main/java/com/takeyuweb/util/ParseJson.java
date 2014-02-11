package com.takeyuweb.util;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by uzuki05 on 14/01/23.
 */
public class ParseJson {
    protected String content;
    protected JsonNode getJsonNode(String str) {
        try {
            return new ObjectMapper().readTree(str);
        } catch (IOException e) {
            Log.d(getClass().getName(), e.getMessage());
        }
        return null;
    }

    public void loadJson(String str) {

    }

    public String getContent() {
        return this.content;
    }
}
