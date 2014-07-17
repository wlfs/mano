/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
import mano.web.*;

/**
 *
 * @author jun <jun@diosay.com>
 */
@UrlMapping("/home")
public class Home extends Controller {
    
    @UrlMapping("/index/{id}")
    public void index(@PathParam("id") int id){
        
        System.out.println(id);
        
        this.view();
    }
}
