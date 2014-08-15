/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.io.Buffer;
import mano.net.ByteArrayBuffer;

/**
 *
 * @author jun <jun@diosay.com>
 */
interface ReceivedHandler {

    ReceivedHandler onRead(ByteArrayBuffer buffer) throws Exception;
}
