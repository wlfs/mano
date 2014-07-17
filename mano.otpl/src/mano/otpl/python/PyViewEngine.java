/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.otpl.python;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.http.HttpContext;
import mano.otpl.python.PyParaser;
import mano.otpl.python.PyParaser;
import mano.web.RequestService;
import mano.web.ViewEngine;
import org.python.util.PythonInterpreter;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class PyViewEngine extends ViewEngine {
    
    PyParaser paraser = new PyParaser();
    Queue<OutputProxy> queued;

    public PyViewEngine() {
        queued = new java.util.concurrent.LinkedBlockingQueue<>();
    }

    @Override
    public String compile(String tempdir, String tplName) {
        try {
            paraser.open("E:\\repositories\\java\\mano\\mano.server\\server\\wwwroot\\views\\tpl\\member.tpl.html");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PyParaser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        try {
            paraser.parse();
            File f = new File("E:\\repositories\\java\\mano\\mano.server\\server\\tmp\\" + Integer.toHexString(tplName.hashCode()) + ".py");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            //File f = java.io.File.createTempFile(Integer.toHexString(tplName.hashCode()), ".py");
            tplName = f.getAbsolutePath();
            System.out.println(tplName);
            paraser.compile(tplName);
        } catch (Exception ex) {
            ex.printStackTrace();
            //Logger.getLogger(Paraser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tplName;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void render(RequestService service, String tmpName) {
        //java.io.FileInputStream fs = null;

        /*fs = new java.io.FileInputStream(tmpName);
         java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(fs));
            
         String s;
         while ((s = reader.readLine()) != null) {
         service.getContext().getResponse().write(s);
         }*/
        try {
            
            OutputProxy proxy = queued.poll();
            if (proxy == null) {
                proxy = new OutputProxy();
            }
            proxy.init(service.getContext());
            for (Entry<String, Object> entry : service.getEntries()) {
                proxy.interpreter.set(entry.getKey(), entry.getValue());
            }
            proxy.interpreter.execfile(tmpName);
            proxy.reset();
            queued.offer(proxy);
        } catch (Exception ex) {
            ex.printStackTrace();
            //Logger.getLogger(Paraser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void render(RequestService service) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    class OutputProxy extends Writer {
        
        HttpContext context;
        StringBuilder sb;
        PythonInterpreter interpreter;
        
        public void init(HttpContext hc) {
            context = hc;
            sb = new StringBuilder();
            interpreter = new PythonInterpreter();
            interpreter.setOut(this);
            interpreter.setErr(this);
        }
        
        public void reset() {
            context = null;
            sb.setLength(0);
            interpreter.cleanup();
        }
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            sb.setLength(0);
            sb.append(cbuf, off, len);
            context.getResponse().write(sb.toString());
            sb.setLength(0);
        }
        
        @Override
        public void flush() throws IOException {
            context.getResponse().flush();
        }
        
        @Override
        public void close() throws IOException {
            context.getResponse().end();
        }
        
    }
    
}
