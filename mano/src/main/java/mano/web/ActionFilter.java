/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.web;

/**
 * 表示一个Action过滤程序。
 * <p>注意：该类型实例在应用程序生命周期中只有一个实例。
 * @author jun <jun@diosay.com>
 */
public interface ActionFilter {
    /**
     * 当执行Action之前调用。
     * @param context
     * @return 返回 false 则必须在该过滤器中处理该Action，因为它将不在继续处理。否则返回true。
     */
    boolean onActionExecuting(ActionContext context);
    
    /**
     * 当执行Action之后调用。
     * @param context
     * @return 返回 false 则必须在该过滤器中处理该Action，因为它将不在继续处理。否则返回true。
     */
    boolean onActionExecuted(ActionContext context);
}
