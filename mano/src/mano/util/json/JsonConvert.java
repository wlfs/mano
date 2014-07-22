/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.json;

import mano.util.ProviderMapper;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class JsonConvert {

    private static JsonConverter converter;

    public static JsonConverter getConverter(String settingName) throws JsonException {
        try {
            return (JsonConverter) ProviderMapper.newInstance(settingName);
        } catch (Exception ex) {
            throw new JsonException(ex);
        }
    }

    private static JsonConverter getConverter() throws JsonException {
        if (converter == null) {
            converter = getConverter(JsonConvert.class.getName());
            if (converter == null) {
                throw new JsonException("不是一个有效的 JsonConverter 对象。");
            }
        }
        return converter;
    }

    public static String serialize(Object src) throws JsonException, IllegalArgumentException {
        return getConverter().serialize(src);
    }

    public static <T> T deserialize(Class<T> clazz, String json) throws JsonException, IllegalArgumentException {
        return getConverter().deserialize(clazz, json);
    }
}
