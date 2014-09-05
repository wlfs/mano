/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mano.Activator;
import mano.InvalidOperationException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class ProviderMapper {

    private static Activator loader = new Activator();
    private static Map<String, Entry<String, Class<?>>> mappings = new HashMap<>();

    public static void addPath(String path) throws FileNotFoundException, MalformedURLException {
        loader.loadAll(path);
    }

    public static void map(final String name, final String type, final String path) throws FileNotFoundException, MalformedURLException {
        if (path != null && !"".equals(path)) {
            addPath(path);
        }

        mappings.put(name, new Entry<String, Class<?>>() {
            Class<?> clazz;

            @Override
            public String getKey() {
                return name;
            }

            @Override
            public Class<?> getValue() {

                if (clazz == null) {
                    try {
                        clazz = loader.getClass(type);
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }

                return clazz;
            }

            @Override
            public Class<?> setValue(Class<?> value) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        });

    }

    public static Object newInstance(String name, Object... args) throws InstantiationException {
        if (name == null || !mappings.containsKey(name)) {
            throw new InvalidOperationException("name 为空或未找到映射。name：" + name);
        }
        Entry<String, Class<?>> entry = mappings.get(name);
        return loader.newInstance(entry.getValue(), args);
    }

    public static <T> T newInstance(Class<T> clazz, String name, Object... args) throws InstantiationException {
        return (T) newInstance(name, args);
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) throws InstantiationException {
        return (T) newInstance(clazz, clazz.getName(), args);
    }

}
