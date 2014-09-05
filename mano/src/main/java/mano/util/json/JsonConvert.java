/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.json;

import mano.ContextClassLoader;
import mano.util.ProviderMapper;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class JsonConvert {

    private JsonConverter converter;
    public JsonConvert(JsonConverter c){
        converter=c;
    }
    
    public static JsonConverter getConverter(ContextClassLoader loader,String exportName) throws JsonException {
        try {
            return (JsonConverter) loader.getExport(exportName);
        } catch (Exception ex) {
            throw new JsonException(ex);
        }
    }
    
    public static JsonConverter getConverter(ContextClassLoader loader) throws JsonException {
        
        return getConverter(loader,JsonConvert.class.getName());
    }

    public String serialize(Object src) throws JsonException, IllegalArgumentException {
        return converter.serialize(src);
    }

    public <T> T deserialize(Class<T> clazz, String json) throws JsonException, IllegalArgumentException {
        return converter.deserialize(clazz, json);
    }
}
