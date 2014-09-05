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
public class ContentResult implements  ActionResult{
    private String content;
    public ContentResult(String s){
        this.content=s;
    }
    @Override
    public void execute(ActionContext service) {
        if(this.content!=null && !service.getContext().isCompleted()){
            service.getContext().getResponse().charset("utf-8");
            service.getContext().getResponse().write(this.content);
        }
    }
    
}
