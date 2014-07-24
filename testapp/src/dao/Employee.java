/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

package dao;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class Employee {
    private int id;
    public int getId(){
        return id;
    }
    public void setId(int val){
        id=val;
    }
    
    private String firstName;
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String val){
        firstName=val;
    }
    
    private String lastName;
    public String getLastName(){
        return lastName;
    }
    public void setLastName(String val){
        lastName=val;
    }
}
