package com.diosay.test.webapp.controllers;

import com.diosay.test.webapp.CheckLogin;
import mano.web.Controller;
import mano.web.Filter;
import mano.web.UrlMapping;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class My extends Controller {

    @Filter(CheckLogin.class)
    @UrlMapping
    void index() {
        set("sun", session("sun"));
        view();
    }
}
