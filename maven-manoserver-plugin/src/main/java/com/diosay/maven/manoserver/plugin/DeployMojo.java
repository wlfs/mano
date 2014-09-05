/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diosay.maven.manoserver.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author jun <jun@diosay.com>
 * @phase install
 * @goal deploy
 */
public class DeployMojo extends AbstractMojo {

    /**
     * @parameter expression=”${aSystemProperty}”
     * default-value=”${anExpression}”
     */
    private String buildDirectory;

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        this.getLog().info("------------------------------------------------------------------------");
        this.getLog().info("Mano Web Application Deploy");
        this.getLog().info("------------------------------------------------------------------------");

        File src = new File(resourceDirectory);
        if (!src.exists() || !src.isDirectory()) {
            this.getLog().error(new IOException("源目录不存在或不是目录：" + resourceDirectory));
        } else {
            this.getLog().info("copying " + src + " to " + buildDirectory);
            try {
                this.getLog().info("deleting " + buildDirectory);
                Utility.deleteFolder(buildDirectory);
                Utility.copyFolder(src, new File(buildDirectory));
            } catch (IOException ex) {
                this.getLog().error(ex);
            }

            this.getLog().info("copying " + libDirectory + " to " + buildDirectory + "/WEB-INF/lib");
            try {
                this.getLog().info("deleting " + buildDirectory + "/WEB-INF/lib");
                Utility.deleteFolder(buildDirectory + "/WEB-INF/lib");
                Utility.copyFolder(new File(libDirectory), new File(buildDirectory + "/WEB-INF/lib"));
            } catch (IOException ex) {
                this.getLog().error(ex);
            }
        }
        this.getLog().info("done");
    }

    static class Utility {

        public static void copyFile(String src, String target) throws IOException {
            copyFile(new File(src), new File(target));
        }

        public static void copyFile(File src, File target) throws IOException {
            if (!src.exists() || !src.isFile()) {
                throw new FileNotFoundException("源文件不存或不是文件：" + src);
            }

            if (target.exists() && target.isFile()) {
                target.delete();
            }

            File parent = target.getParentFile();
            if (!parent.exists() || (parent.exists() && !parent.isDirectory())) {
                parent.mkdirs();
            }

            target.createNewFile();

            try (FileInputStream input = new FileInputStream(src)) {
                try (FileOutputStream out = new FileOutputStream(target)) {
                    out.getChannel().transferFrom(input.getChannel(), 0, input.getChannel().size());
                }
            }
        }

        public static void copyFolder(String src, String target) throws IOException {
            copyFolder(new File(src), new File(target));
        }

        public static void copyFolder(File src, File target) throws IOException {
            if (!src.exists() || !src.isDirectory()) {
                throw new FileNotFoundException("源目录不存在或不是目录：" + src);
            }
            if (!target.exists() || !target.isDirectory()) {
                if (!target.mkdirs()) {
                    throw new IOException("创建目标目录失败：" + target);
                }
            }
            for (File child : src.listFiles()) {
                if (child.isDirectory()) {
                    copyFolder(src.toString() + "/" + child.getName(), target.toString() + "/" + child.getName());
                } else if (child.isFile()) {
                    copyFile(src.toString() + "/" + child.getName(), target.toString() + "/" + child.getName());
                }
            }
        }

        public static void deleteFile(String filename) {
            new File(filename).delete();
        }

        public static void deleteFolder(String filename) {
            deleteFolder(new File(filename));
        }

        public static void deleteFolder(File file) {
            if (file.exists() && file.isDirectory()) {
                for (File child : file.listFiles()) {
                    if (child.isFile()) {
                        child.delete();
                    } else {
                        deleteFolder(child);
                    }
                }
                file.delete();
            }

        }
    }

}
