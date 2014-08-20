
package com.diosay.test.webapp.controllers;

import com.diosay.test.webapp.dao.SessionHelper;
import com.diosay.test.webapp.model.Product;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.web.Controller;
import mano.web.UrlMapping;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author jun <jun@diosay.com>
 */
@UrlMapping
public class Home extends Controller {
    
    @UrlMapping
    public void index(){
        this.getLogger().info("=====:index");
        view();
    }
    
    @UrlMapping
    public void login(){
        this.getLogger().info("=====:login");
        view();
    }
    
    @UrlMapping
    public void doLogin(){
        URL url=null;
        try {
            url = new File(this.getContext().getServer().mapPath("WEB-INF/hibernate.cfg.xml")).toURI().toURL();
        } catch (MalformedURLException ex) {
            this.getLogger().error(null, ex);
            return;
        }
        Session session =SessionHelper.getSessionFactory(url).openSession();
        Product entity = new Product();
        Transaction trans = session.beginTransaction();
        session.save(entity);
        trans.commit();
        session.flush();
        session.close();
        this.text(entity.getId()+"");
    }
}
