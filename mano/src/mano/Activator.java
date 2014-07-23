/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import mano.util.logging.Logger;

/**
 *
 * @author jun <jun@diosay.com>
 */
public final class Activator {

    private volatile ClassLoader current;
    private Set<String> paths;
    private Map<String, ClassLoader> mappings;

    public synchronized ClassLoader loader() {
        return current;
    }

    public Activator() {
        this(ClassLoader.getSystemClassLoader());
        this.paths = new HashSet<>();
        this.mappings = new HashMap<>();
    }

    public Activator(ClassLoader parent) {
        this.paths = new HashSet<>();
        this.mappings = new HashMap<>();
        this.current = parent;
    }

    public Activator(Activator parent) {
        this(parent.current);
        this.paths.addAll(parent.paths);
        this.mappings.putAll(parent.mappings);
    }

    public synchronized Activator load(String filename) throws FileNotFoundException, MalformedURLException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException(filename);
        }

        if (!file.isFile() || !file.getName().toLowerCase().endsWith(".jar")) {
            throw new UnsupportedOperationException("不是一个有效的jar文件。filename:" + filename);
        }

        if (!paths.contains(file.getParent())) {
            paths.add(file.getParent());
        }
        current = new URLClassLoader(new URL[]{file.toURI().toURL()}, current);
        if (!this.mappings.containsKey(file.getAbsolutePath())) {
            this.mappings.put(file.getAbsolutePath(), current);
        }
        return this;
    }

    public synchronized Activator loadAll(String... files) {
        if (files == null) {
            return this;
        }
        File file;
        final Set<URL> set = new HashSet<>();
        final Set<String> urls = new HashSet<>();
        for (String path : files) {
            file = new File(path);
            if (!file.exists()) {
                Logger.warn("Activator.loadAll:File not found.path:%s", path);
            }

            if (!file.isDirectory()) {
                if (!file.getName().toLowerCase().endsWith(".jar")) {
                    Logger.warn("Activator.loadAll:File not a JAR file.path:%s", path);
                }
                if (!paths.contains(file.getParent())) {
                    paths.add(file.getParent());
                }
                try {
                    set.add(file.toURI().toURL());
                } catch (MalformedURLException ex) {
                    Logger.warn("Activator.loadAll:toURL fatal.", ex);
                }
            } else {
                file.listFiles((File f) -> {//java 8
                    if (f.isFile() && f.getName().toLowerCase().endsWith("jar")) {
                        try {
                            urls.add(f.getAbsolutePath());
                            set.add(f.toURI().toURL());
                            if (!paths.contains(f.getAbsolutePath())) {
                                paths.add(f.getAbsolutePath());
                            }
                        } catch (MalformedURLException ex) {
                            Logger.warn("Activator.loadAll:toURL fatal.", ex);
                        }
                    }
                    return false;
                });
            }
        }

        if (!set.isEmpty()) {
            current = new URLClassLoader(set.toArray(new URL[0]), current);
            for (String url : urls) {
                if (!this.mappings.containsKey(url)) {
                    this.mappings.put(url, current);
                }
            }
        }

        return this;
    }

    @Deprecated
    public Activator register(String path) throws FileNotFoundException {
        File file = new File(path);
        if (!file.exists()) {
            throw new FileNotFoundException(path);
        }
        if (!file.isDirectory()) {
            throw new UnsupportedOperationException("不是一个有效的路径。path:" + path);
        }

        if (!paths.contains(file.getAbsolutePath())) {
            paths.add(file.getAbsolutePath());
        }
        return this;
    }

    public Class<?> getClass(String fullname) throws ClassNotFoundException {
        String[] args = fullname.split(",");
        Class<?> clazz = null;

        if (args.length > 1) {
            //System.out.println("hh");
            Set<String> urls = new HashSet<>();
            Set<ClassLoader> loaders = new HashSet<>();
            String url;
            for (String s : paths) {

                if (s.endsWith("\\")) {
                    url = s + args[1] + ".jar";
                } else {
                    url = s + "\\" + args[1] + ".jar";
                }
                if (!this.mappings.containsKey(url)) {
                    urls.add(url);
                } else {
                    loaders.add(this.mappings.get(url));
                }
            }

            for (ClassLoader cl : loaders) {
                try {
                    clazz = cl.loadClass(args[0]);
                } catch (Exception ex) {
                    clazz = null;
                }
                if (clazz != null) {
                    return clazz;
                }
            }

            for (String u : urls) {
                try {
                    clazz = this.load(u).loader().loadClass(args[0]);
                } catch (Exception ex) {
                    clazz = null;
                }
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        return loader().loadClass(args[0]);

    }

    public <T> T newInstance(Class<T> clazz, Object... args) throws InstantiationException {
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
            } catch (Exception e) {
                //noting;
            }
        }

        for (Constructor<?> ctor : clazz.getConstructors()) {
            if (ctor.getParameterTypes().length == length) {
                try {
                    return (T) ctor.newInstance(args);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new InstantiationException(ex.getMessage());
                }
            }
        }
        throw new InstantiationException();
    }

    public Object newInstance(String classFullname, Object... args) throws InstantiationException, ClassNotFoundException {
        return this.newInstance(this.getClass(classFullname), args);
    }
}
