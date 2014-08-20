package com.diosay.test.webapp.model;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author jun <jun@diosay.com>
 */
public class Product {
    private int id;
    public int getId(){
        return id;
    }
    public void setId(int val){
        id=val;
    }
    private String name;
    public String getName(){
        return name;
    }
    public void setName(String val){
        name=val;
    }
}
