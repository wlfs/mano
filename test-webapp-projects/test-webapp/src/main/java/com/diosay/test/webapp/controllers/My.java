package com.diosay.test.webapp.controllers;

import mano.web.Controller;
import mano.web.UrlMapping;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class My extends Controller {

    @UrlMapping
    void index() {
        set("sun", session("sun"));
        view();
    }
}
