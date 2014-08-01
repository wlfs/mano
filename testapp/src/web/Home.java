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

        this.set("title", "hello");
        this.set("title", "OPTL-IL TEST");
        this.set("obj", this);
        this.set("list", new String[]{"abx", "fttf"});

        this.view();
    }

    @UrlMapping("/submit")
    void submit() {
        this.text("post input[text] content:" + this.form("text"));
    }

    @UrlMapping("/form")
    void form() {
        this.view();
        this.getContext().getRequest().getCookie().iterator().forEach(mm->{
        
        });
    }

    Session getSession() throws Exception {
        Session result = (Session) this.context.getApplication().get("dao-session");
        if (result == null) {
            SessionFactory sessionFactory = (SessionFactory) this.context.getApplication().get("dao-session-fat");
            if (sessionFactory == null) {
                URL url;
                try {
                    url = new File(this.context.getServer().mapPath("config/hibernate.cfg.xml")).toURI().toURL();
                } catch (Exception ex) {
                    getLogger().fatal("", ex);
                    throw ex;
                }
                Configuration cfg = new Configuration().configure(url);
                //cfg.setProperty("mappingDirectoryLocations", this.context.getServer().mapPath("config/mappings"));
                StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties());
                StandardServiceRegistry ssr = ssrb.build();
                sessionFactory = cfg.buildSessionFactory(ssr);
                this.context.getApplication().set("dao-session-fat", sessionFactory);
            }

            result = sessionFactory.openSession();
            //this.context.getApplication().set("dao-session", result);
        }
        return result;
    }

    @UrlMapping
    void dao() throws Exception {
        Session session = getSession();

        dao.Employee entity = new dao.Employee();
        entity.setFirstName("张");
        entity.setLastName("三");

        testapp.model.Product p = new testapp.model.Product();
        p.setName("西瓜");
        Transaction trans = session.beginTransaction();
        session.save(entity);
        session.save(p);
        trans.commit();
        session.flush();
        this.json(new Object[]{entity, p});
        session.close();
    }

}
