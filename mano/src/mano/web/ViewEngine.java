/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.web;

import java.io.File;
import mano.InvalidOperationException;

/**
 *
 * @author jun <jun@diosay.com>
 */
public abstract class ViewEngine {

    private String tmpdir;
    private String viewdir;

    public void setTempdir(String path) throws InvalidOperationException {

        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            throw new InvalidOperationException("指定路径不是一个有效目录：" + path);
        } else if (!file.exists() && !file.mkdirs()) {
            throw new InvalidOperationException("指定路径不存在，尝试创建但失败：" + path);
        }
        try {
            if (!file.canWrite()) {
                file.setWritable(true);
            }
            if (!file.canRead()) {
                file.setReadable(true);
            }
        } catch (Exception ex) {
            throw new InvalidOperationException("指定路径不可读或写，尝试设置但失败：" + path, ex);
        }
        tmpdir = file.getAbsolutePath();
    }

    public String getTempdir() throws InvalidOperationException {
        if (tmpdir == null) {
            throw new InvalidOperationException("未设置临时目录。");
        }
        return tmpdir;
    }

    public void setViewdir(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            throw new InvalidOperationException("指定路径不是一个有效目录：" + path);
        }
        try {

            if (!file.canRead()) {
                file.setReadable(true);
            }
        } catch (Exception ex) {
            throw new InvalidOperationException("指定路径不可读或写，尝试设置但失败：" + path, ex);
        }
        viewdir = file.getAbsolutePath();
    }

    public String getViewdir() {
        if (viewdir == null) {
            throw new InvalidOperationException("未设置视图目录。");
        }
        return viewdir;
    }

    /**
     * 编译一个模板文件，如果编译成功则返回编译的文件名，否则返回 null.
     *
     * @param tempdir 临时目录
     * @param tplFilename 模板文件名
     * @return
     */
    public abstract String compile(String tempdir, String tplName);

    /**
     *
     * @param service 当前上下文
     * @param tempFilename
     * @return
     */
    public abstract void render(RequestService service, String tmpName);
    
    /**
     *
     * @param service 当前上下文
     * @param tempFilename
     * @return
     */
    public abstract void render(RequestService service);

}
