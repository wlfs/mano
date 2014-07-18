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
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.http.HttpContext;
import mano.otpl.emit.EmitParser;
import mano.otpl.emit.Interpreter;
import mano.otpl.emit.OpCode;
import mano.util.LinkedMap;
import mano.util.Utility;
import mano.web.RequestService;
import mano.web.ViewEngine;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class OtplViewEngine extends ViewEngine {

    EmitParser parser = new EmitParser();
    Interpreter interpreter = new Interpreter();

    @Override
    public String compile(String tempdir, String tplName) {
        try {
            parser.open(tplName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OtplViewEngine.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            parser.parse();
            File f = new File(tempdir + Integer.toHexString(tplName.hashCode()) + ".il");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            //File f = java.io.File.createTempFile(Integer.toHexString(tplName.hashCode()), ".py");
            tplName = f.getAbsolutePath();
            System.out.println(tplName);
            //parser.compile(tplName);
            FileOutputStream fs = new FileOutputStream(f);
            //OutputStreamWriter write = new OutputStreamWriter(fs, "UTF-8");
            parser.compile(fs);
            fs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //Logger.getLogger(Paraser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tplName;
    }

    @Override
    public void render(RequestService service, String tmpName) {
        //interpreter.init();
        for (Map.Entry<String, Object> entry : service.getEntries()) {
            interpreter.set(entry.getKey(), entry.getValue());
        }
        OutProxy proxy = new OutProxy();
        proxy.context = service.getContext();

        interpreter.setOut(proxy);
        try {
            interpreter.exec(tmpName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        proxy.context.getResponse().end();
    }

    @Override
    public void render(RequestService service) {
        String source = Utility.combinePath(this.getViewdir(), service.getRequestPath()).toString();
        String target = Integer.toHexString(this.getViewdir().hashCode()) + "$" + Integer.toHexString(source.hashCode());

        File target_file = new File(Utility.combinePath(this.getTempdir(), target).toUri());
        OutProxy proxy = new OutProxy();
        proxy.context = service.getContext();
        try {
            if (target_file.exists()) { //test
                target_file.delete();
            }

            if (true) {//!target_file.exists()

                //File source_file = new File(Utility.combinePath(this.getTempdir(), target).toUri());
                target_file.createNewFile();
                parser.compile(source, target_file.toString());
            }

            //InputStream input = new FileInputStream(target_file);
            for (Map.Entry<String, Object> entry : service.getEntries()) {
                proxy.args.put(entry.getKey(), entry.getValue());
            }
            interpreter.init(proxy);
            interpreter.setOut(proxy);
            interpreter.exec(target_file.toString());
        } catch (Exception ex) {
            try {
                service.getContext().getResponse().write(ex.getMessage());
                //PrintStream ps=new PrintStream(proxy,true);
                //ex.printStackTrace(ps);
                //ps.flush();
                //ps.close();
            } catch (Exception e) {
                //
                e.printStackTrace();
            }
            Logger.getLogger(OtplViewEngine.class.getName()).log(Level.SEVERE, null, ex);
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
