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
public class AuthorizationFilter implements ActionFilter {

    @Filter(AuthorizationFilter.class)
    @Filter(AuthorizationFilter.class)
    void test() {

    }

    @Override
    public boolean onActionExecuting(ActionContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean onActionExecuted(ActionContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
