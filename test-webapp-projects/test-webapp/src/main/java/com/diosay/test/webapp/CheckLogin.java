/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.test.webapp;

import mano.web.ActionContext;
import mano.web.ActionFilter;
import mano.web.HttpSession;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class CheckLogin implements ActionFilter {

    @Override
    public boolean onActionExecuting(ActionContext context) {
        if("home".equalsIgnoreCase(context.getController()) && "login".equalsIgnoreCase(context.getAction())){
            return true;
        }
        
        HttpSession session = context.getContext().getSession();
        if (session == null || session.get("sun") == null) {
            context.getContext().getResponse().redirect("/home/login");
            return false;
        }
        return true;
    }

    @Override
    public boolean onActionExecuted(ActionContext context) {
        return true;
    }

}
