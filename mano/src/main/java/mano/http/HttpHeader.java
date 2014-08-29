/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.util.NameValueCollection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpHeader {

    private final String _name;
    private String _text;
    private String _value;
    private Map<String, String> _attrs;
    //static final Pattern pattern = Pattern.compile(";\\s*(\\w+)\\s*=\\s*\"*([\\w-]+)\"*(\\s*|;*)");
    static final Pattern pattern = Pattern.compile("([^\\s=]+)=[\"\\s]?([^\"]+)");
    public HttpHeader(String name){
        _name=name;
    }
    public HttpHeader(String name,String text){
        this(name);
        this.text(text);
    }
    
    public String name() {
        return _name;
    }

    public String value() {
        return _value;
    }

    public HttpHeader value(String val) {
        _value = val;
        return this;
    }

    public String text() {
        return _text;
    }

    public void text(String text) {
        _text = text;

        int index = text.indexOf(';');
        if (index > 0) {
            _value = text.substring(0, index);
            Matcher m = pattern.matcher(text.substring(index));
            while (m.find()) {
                if (_attrs == null) {
                    _attrs = new NameValueCollection<>();
                }
                _attrs.put(m.group(1), m.group(2));
                //System.out.println("item:"+m.group(1)+" = "+m.group(2));
            }
        } else {
            _value = text;
        }
    }

    public String attr(String name) {
        if (_attrs == null) {
            return null;
        }
        if (_attrs.containsKey(name)) {
            return _attrs.get(name);
        }
        return null;
    }

    public void attr(String name, String value) {
        if (_attrs == null) {
            _attrs = new NameValueCollection<>();
        }
        _attrs.put(name, value);
    }

    @Override
    public String toString() {
        String result = _name + ": " + _value;
        if (_attrs != null) {

            result = _attrs.entrySet().stream()
                    .map((entry) -> "; " + entry.getKey() + "=\"" + entry.getValue() + "\"")
                    .reduce(result, String::concat);
        }

        return result;
    }

    public static HttpHeader prase(String line) {
        HttpHeader header;
        int index = line.indexOf(':');
        if (index < 1) {
            return null;
        }
        header = new HttpHeader(line.substring(0, index).trim());
        header.text(line.substring(index + 1).trim());
        return header;
    }

}
