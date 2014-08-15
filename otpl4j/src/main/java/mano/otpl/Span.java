/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.otpl;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Span extends Node {
    protected Span(Parser p, int type, String s) {
        super(p, type, s);
    }
    
    public static Span create(Parser parser, int type, String source) {
        Span result=new Span(parser,type,source);
        return result;
    }
    
    @Override
    public final boolean isBlock() {
        return false;
    }
    
    @Override
    public void parse(){
        this.parser.parse(this);
    }
}
