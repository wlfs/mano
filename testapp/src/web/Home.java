package web;

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import mano.web.*;

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
    
}
