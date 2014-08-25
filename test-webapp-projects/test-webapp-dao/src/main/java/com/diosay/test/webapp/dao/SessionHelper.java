/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.test.webapp.dao;

import java.net.URL;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class SessionHelper {

    public static SessionFactory getSessionFactory(URL url) {
        Configuration cfg = new Configuration().configure(url);
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties());
        StandardServiceRegistry ssr = ssrb.build();
        java.sql.Date d;
        return cfg.buildSessionFactory(ssr);
    }
    
}
