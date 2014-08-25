/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package mano.service;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class NewClass {
    
    class Test{
        
        
        
    }
    
    static class MessageArgs{
        public int count(){
            return 0;
        }
        
        public Object get(int index){
            return null;
        }
        
        public void proc(){
            
        }
        
    }
    
    
    static class Task{
        public boolean isDone(){
            return true;
        }
        public void ready(){}
        public void process(){}
        public void on(){}
    }
    
    public static void mainS(String...bargs){
        
        Task task=new Task();
        task.ready();
        //service.publish(catgory,type,params)
        
        java.util.concurrent.FutureTask<Object> bb;
        
        
    }
    
    
}
