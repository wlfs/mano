/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import mano.http.HttpContext;
import mano.otpl.emit.EmitParser;
import mano.otpl.emit.Interpreter;
import mano.otpl.emit.OpCode;
import mano.util.LinkedMap;
import mano.util.Utility;
import mano.util.logging.LoggerOLD;
import mano.web.RequestService;
import mano.web.ViewEngine;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class OtplViewEngine extends ViewEngine {
    
    EmitParser parser=new EmitParser();
    Interpreter interpreter = new Interpreter();
    LoggerOLD logger = LoggerOLD.getDefault();//TODO:

    @Override
    public String compile(String tempdir, String tplName) {
        return null;
    }
    
    @Override
    public void render(RequestService service, String tmpName) {
        return;
    }
    
    @Override
    public void render(RequestService service) {
        String source = Utility.combinePath(this.getViewdir(), service.getPath()).toString();
        String target = Integer.toHexString(this.getViewdir().hashCode()) + "$" + Integer.toHexString(source.hashCode()) + ".otc";
        service.getContext().getApplication().getLogger().debug(source);
        File target_file = new File(Utility.combinePath(this.getTempdir(), target).toUri());
        OutProxy proxy = new OutProxy();
        proxy.context = service.getContext();
        try {
            if (target_file.exists()) {
                if (!target_file.delete()) {
                    service.getContext().getApplication().getLogger().debug("file deleting failed:" + target_file);
                }
            }
            service.getContext().getApplication().getLogger().debug("create file :" + target_file);
            if (true) {
                target_file.createNewFile();
                
                //parser=new EmitParser();
                parser.compile(source, target_file.toString());
            }
            for (Map.Entry<String, Object> entry : service.getEntries()) {
                proxy.args.put(entry.getKey(), entry.getValue());
            }
            interpreter.init(proxy);
            interpreter.setOut(proxy);
            interpreter.exec(target_file.toString());
        } catch (Exception ex) {
            logger.error(null, ex);
            try {
                service.getContext().getResponse().write(ex.getMessage());                
            } catch (Exception e) {
                logger.error(null, e);
            }
        }
    }
    
    public static class OutProxy extends OutputStream {
        
        HttpContext context;
        public Stack<Object> stack;
        public Map<String, Object> args;
        
        public OutProxy() {
            stack = new Stack<>();
            args = new HashMap<>();
        }
        
        @Override
        public void flush() throws IOException {
            context.getResponse().flush();
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            context.getResponse().write(b, off, len);
        }
        
        @Override
        public void write(int b) throws IOException {
            context.getResponse().write(new byte[]{(byte) b}, 0, 1);
        }
    }
}
