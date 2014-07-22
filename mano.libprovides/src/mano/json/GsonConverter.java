/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.json;

import com.google.gson.Gson;
import mano.util.json.JsonConverter;
import mano.util.json.JsonException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class GsonConverter implements JsonConverter {

    private Gson gson = new Gson();

    public Gson getGson() {
        return gson;
    }

    @Override
    public String serialize(Object src) throws JsonException, IllegalArgumentException {
        try {
            String result=gson.toJson(src);
            return gson.toJson(src);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    @Override
    public <T> T deserialize(Class<T> clazz, String json) throws JsonException, IllegalArgumentException {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

}
