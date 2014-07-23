package web;

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import mano.util.logging.Logger;
import mano.web.*;
import org.hibernate.*;
import org.hibernate.boot.registry.*;
import org.hibernate.cfg.*;
import org.hibernate.service.*;

/**
 * MVC - DCI
 *
 * @author jun <jun@diosay.com>
 */
@UrlMapping("/home")
public class Home extends Controller {

    public String field = "i am is a field";
    public String oprop = "i am is a auto property";

    public String getProp() {
        return "i am is a property(getProp())";
    }

    public int addTest(int a, int b) {
        return a + b;
    }

    @UrlMapping("/index/{id}")
    void index(@PathParam("id") int id) {

        if (this.context.getSession().get("user") != null) {
            System.out.println("已经登录,id:" + this.context.getSession().get("user"));
        } else {
            this.context.getSession().set("user", id);
            System.out.println("还未登录,id:" + id);
        }

        this.json("hello");
    }

    @UrlMapping("/submit")
    void submit() {
        this.text("post input[text] content:" + this.form("text"));
    }

    @UrlMapping("/form")
    void form() {
        this.view();

    }

    @UrlMapping
    void dao() throws Exception {
        URL url;
        try {
            // A SessionFactory is set up once for an application
            //sessionFactory = new Configuration()
            //    .configure() // configures settings from hibernate.cfg.xml
            //    .buildSessionFactory();

            url = new File(this.context.getServer().mapPath("config/hibernate.cfg.xml")).toURL();
        } catch (Exception ex) {
            Logger.fatal("", ex);
            throw ex;
        }
        //下载安装MYSQL驱动 http://dev.mysql.com/downloads/file.php?id=452397
        Configuration cfg = new Configuration().configure(url);
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties());
        StandardServiceRegistry ssr = ssrb.build();
        SessionFactory sessionFactory = cfg.buildSessionFactory(ssr);

        this.text(sessionFactory.toString());

    }

}
