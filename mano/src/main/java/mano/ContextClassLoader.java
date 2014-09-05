/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import mano.util.logging.Logger;

/**
 * 上下文类加载器
 * @author jun <jun@diosay.com>
 */
public class ContextClassLoader extends URLClassLoader {

    private Logger logger;

    public ContextClassLoader(Logger logger, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.logger = logger;
    }

    public ContextClassLoader(Logger logger, URL[] urls) {
        this(logger, urls, ContextClassLoader.class.getClassLoader() == null ? ClassLoader.getSystemClassLoader() : ContextClassLoader.class.getClassLoader());
    }

    public ContextClassLoader(Logger logger) {
        this(logger, new URL[0]);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void setLogger(Logger l) {
        this.logger = l;
    }

    public void register(String... paths) {
        if (paths == null || paths.length == 0) {
            return;
        }

        File file;
        final Set<URL> set = new HashSet<>();
        //final Set<String> urls = new HashSet<>();
        String name;
        for (String path : paths) {
            file = new File(path);
            if (!file.exists()) {
                try {
                    set.add(new URI(path).toURL());
                } catch (Throwable ex) {
                    logger.warn("[ContextClassLoader.register]Invalid path(URL ERROR 1) :" + path);
                }
                continue;
            }

            if (!file.isDirectory()) {
                name = file.getName().toLowerCase();
                if (!(name.endsWith(".jar") || name.endsWith(".class"))) {
                    logger.warn("[ContextClassLoader.register]Invalid path(Is not a valid JAR file or Class file):" + path);
                }
                try {
                    set.add(file.toURI().toURL());
                } catch (Throwable ex) {
                    logger.warn("[ContextClassLoader.register]Invalid path(URL ERROR 2)", ex);
                }
            } else {
                file.listFiles((File f) -> {
                    String fname = f.getName().toLowerCase();
                    if (f.isFile() && (fname.endsWith("jar") || fname.endsWith(".class"))) {
                        try {
                            set.add(f.toURI().toURL());
                        } catch (Throwable ex) {
                            logger.warn("[ContextClassLoader.register]Invalid path(URL ERROR 3)", ex);
                        }
                    }
                    return false;
                });
            }
        }

        if (!set.isEmpty()) {
            set.iterator().forEachRemaining(url -> {
                super.addURL(url);
            });
        }
    }

    public <T> T newInstance(Class<T> clazz, Object... args) throws ReflectiveOperationException {
        int length = args == null ? 0 : args.length;
        if (length > 0) {
            try {
                Class<?>[] types = new Class<?>[length];
                for (int i = 0; i < length; i++) {
                    types[i] = args[i].getClass();
                }
                Constructor<?> ctor = clazz.getConstructor(types);
                if (ctor != null) {
                    return (T) ctor.newInstance(args);
                }
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (MANO_MACRO.DEBUG) {
                    logger.debug(e.getTargetException());
                }
            } catch (Throwable e) {
                if (MANO_MACRO.DEBUG) {
                    logger.debug(e);
                }
            }
        }

        for (Constructor<?> ctor : clazz.getConstructors()) {
            if (ctor.getParameterTypes().length == length) {
                try {
                    return (T) ctor.newInstance(args);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new ReflectiveOperationException(e.getTargetException());
                } catch (Throwable ex) {
                    throw new ReflectiveOperationException(ex);
                }
            }
        }
        throw new ReflectiveOperationException();
    }

    public Object newInstance(String clazz, Object... args) throws ReflectiveOperationException {
        return this.newInstance(this.loadClass(clazz, true), args);
    }
    private HashMap<String, Class<?>> mappings = new HashMap<>();

    public void registerExport(String name, String clazz) throws ClassNotFoundException {
        mappings.put(name, this.loadClass(clazz, true));
    }

    public Object getExport(String name, Object... args) throws ReflectiveOperationException {
        if (mappings.containsKey(name)) {
            return newInstance(mappings.get(name), args);
        }
        return null;
    }

    public <T> T getExport(Class<T> clazz, String name, Object... args) throws ReflectiveOperationException {
        return (T) getExport(name, args);
    }
}
