/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mano.http.HttpContext;
import mano.otpl.emit.EmitParser;
import mano.otpl.emit.Interpreter;
import mano.web.RouteService;
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
    public void render(RouteService service, String tmpName) {
        interpreter.init();
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

    class OutProxy extends OutputStream {

        HttpContext context;

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
