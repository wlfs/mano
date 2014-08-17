
package com.diosay.test.webapp.controllers;

import mano.web.Controller;
import mano.web.UrlMapping;

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
        String un=this.form("username");
        set("postun",un);
        session("sun",un);
        set("sun", session("sun"));
        view();
    }
}
