/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util.json;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class JsonException extends RuntimeException {

    public JsonException() {
        super();
    }

    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public JsonException(Throwable cause) {
        super(cause);
    }
}
