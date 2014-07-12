/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.web;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class ViewResult implements ActionResult {
    
    @Override
    public void execute(RouteService service) {
        String tpl="E:\\repositories\\java\\mano\\mano.server\\server\\wwwroot\\views\\tpl\\member.tpl.html";
        
        ViewEngine engine=service.getContext().getApplication().getViewEngine();
        if(engine==null){
            return;//throws error
        }
        tpl=engine.compile("tmp_path",tpl);
        
        engine.render(service,tpl);
        
    }
    
}
