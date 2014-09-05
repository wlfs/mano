/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.maven.manoserver.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
//http://blog.sina.com.cn/s/blog_3e3e61fb0100k08z.html
//http://www.kankanews.com/ICkengine/archives/78579.shtml
//http://java-xp.iteye.com/blog/1625574
//http://blog.csdn.net/yuxinleipp/article/details/7633600

/**
 *
 * @author jun <jun@diosay.com>
 * @phase install
 * @goal run
 */
public class RunMojo extends AbstractMojo {

    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String serverDirectory;
    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String resourceDirectory;

    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String libDirectory;
    private final String template = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><application path=\"%s\"><dependency path=\"%s\"/></application>";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("Mano Server Run");
        this.getLog().info("------------------------------------------------------------------------");

        String content = String.format(template, resourceDirectory, libDirectory);
        File cfgPath = new File(serverDirectory + "/conf/apps");
        if (!cfgPath.exists() || !cfgPath.isDirectory()) {
            cfgPath.mkdirs();
        }
        File cfg = new File(serverDirectory + "/conf/apps/" + Integer.toHexString(UUID.randomUUID().hashCode()) + ".xml");
        try {
            cfg.createNewFile();
            try (FileOutputStream out = new FileOutputStream(cfg)) {
                out.write(content.getBytes("utf-8"));
            }
        } catch (IOException ex) {
            this.getLog().debug(ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            getLog().debug("deletting " + cfg);
            cfg.delete();
        }));

        Process process = null;
        try {
            //this.getPluginContext()."cmd.exe /C start " + 
            process = Runtime.getRuntime().exec(serverDirectory + "/bin/run.bat");
            //http://blog.163.com/zhao_jinggui/blog/static/169620429201161163711467/
            final InputStream err = process.getErrorStream();
            final InputStream in = process.getInputStream();

            new Thread(() -> {
                while (true) {
                    try {
                        System.err.print((char) err.read());
                    } catch (IOException ex) {
                        this.getLog().debug(ex);
                        break;
                    }
                }
            }).start();

            new Thread(() -> {
                while (true) {
                    try {
                        System.out.print((char) in.read());
                    } catch (IOException ex) {
                        this.getLog().debug(ex);
                        break;
                    }
                }
            }).start();

            process.waitFor();
            err.close();
            in.close();
            process.destroy();

        } catch (IOException ex) {
            if (process != null) {
                process.destroyForcibly();
            }
            ex.printStackTrace(System.out);
        } catch (InterruptedException ex) {
            if (process != null) {
                process.destroyForcibly();
            }
            getLog().debug("deletting " + cfg);
            cfg.delete();
        }
        getLog().debug("deletting " + cfg);
        cfg.delete();
        System.out.println("manoserver exit.");
    }
    
    @Override
    protected void finalize() throws Throwable{
        System.out.println("manoserver exit.");
        super.finalize();
        
    }

}
