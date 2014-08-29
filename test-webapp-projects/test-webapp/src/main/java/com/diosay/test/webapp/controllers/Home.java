package com.diosay.test.webapp.controllers;

import com.diosay.test.webapp.dao.SessionHelper;
import com.diosay.test.webapp.model.Product;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.http.HttpPostFile;
import mano.web.Controller;
import mano.web.PathParam;
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
    public void index() {
        this.getLogger().info("=====:index:" + this.session("uid"));

        view();
    }

    @UrlMapping
    public void login() {
        this.getLogger().info("=====:login:" + this.query("tid"));
        view();
    }

    @UrlMapping
    public void doLogin() throws IOException {

        HttpPostFile f = this.file("fn");
        if (f != null) {
            set("postfile", f.getFilename());
            f.savaAs(this.getContext().getServer().mapPath("upfile/abc") + f.getExtension());
        }
        set("posttext", this.form("username"));
        this.session("sun", this.form("username"));
        view();
        
        
        /*
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
         this.text(entity.getId()+"");*/
    }

    @UrlMapping("/foo/{id}")
    void foo(@PathParam("id") int id) {
        this.text("foo:" + id);
    }

    @UrlMapping("/bar/{id}")
    void bar(@PathParam("id") long id) {
        this.text("bar:" + id);
    }

}
