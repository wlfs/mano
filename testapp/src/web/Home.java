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
 * MVC  - DCI
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
    void index(@PathParam("id") int id){
        
        System.out.println(id);
        
        this.view();
    }
}
