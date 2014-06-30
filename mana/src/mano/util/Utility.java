/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.util;

import java.util.ArrayList;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Utility {
    public static String[] split(String s,String spliter,boolean removeEmptyItem){
        String[] arr=(s==null)?new String[0]:s.split(spliter);
        if(removeEmptyItem){
            ArrayList<String> temp=new ArrayList<>();
            for(int i=0;i<arr.length;i++){
                if(!"".equals(arr[i].trim())){
                    temp.add(arr[i].trim());
                }
            }
            arr=temp.toArray(new String[0]);
        }
        return arr;
    }
    
    public static String[] split(String s,String spliter){
        return split(s,spliter,false);
    }
    
}
