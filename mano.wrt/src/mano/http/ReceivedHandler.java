/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.io.Buffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
interface ReceivedHandler {

    ReceivedHandler onRead(Buffer buffer) throws Exception;
}
