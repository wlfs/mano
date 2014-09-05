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
    
    private ViewEngine engine;
    private String template;
    
    public ViewResult() {
    }
    
    public final ViewResult init(ViewEngine ve) {
        engine = ve;
        return this;
    }
    
    @Override
    public void execute(ActionContext service) {
        if (engine == null) {
            service.getContext().getResponse().write("this view engine not found.");
            return;//throws error
        }
        
        String path = service.getPath();
        if (path == null || "".equals(path)) {
            path = service.getController() + "/" + service.getAction() + ".html";
        }
        service.setPath(path);
        engine.render(service);
    }
    
}
