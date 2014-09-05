/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.http;

import mano.util.Utility;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class HttpPostFile {

    protected String name;
    protected String original;
    //protected String filename;
    protected String contentType;
    protected long size;
    protected File file;
    protected HttpPostFile(){}
    HttpPostFile(File f, String field, String original, String type, long len) {
        this.file = f;
        this.name = field;
        this.original = original;
        this.contentType = type;
        this.size = len;
    }

    public String getName() {
        return this.name;
    }

    public String getFilename() {
        return this.original;
    }

    public String getType() {
        return this.contentType;
    }

    public long getLength() {
        return this.size;
    }

    public File getTempfile() {
        return this.file;
    }

    public void savaAs(String filename) throws IOException {
        Utility.copyFile(file, new File(filename));
    }

    public String getExtension() {
        if (this.original != null && this.original.lastIndexOf(".") > 0) {
            return this.original.substring(this.original.lastIndexOf("."));
        }
        return "";
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.file != null) {
            try {
                this.file.delete();
            } catch (Throwable e) {
            }
        }
        this.file = null;
        super.finalize();
    }
}
