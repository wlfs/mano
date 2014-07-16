
import java.util.Set;

/*
 * Copyright (C) 2014 The MANO Authors. 
 * All rights reserved. Use is subject to license terms. 
 * 
 * See more http://mano.diosay.com/
 * 
 */

/**
 *
 * @author jun <jun@diosay.com>
 */
public class NewClass {
    
    class Node{
        public String name;
        public String compile(){
            return null;//lexers
        }
    }
    
    class Block extends Node{
        public Set<Node> nodes;
        
    }
    
    class Span extends Node{
        
    }
    
    class Plain extends Span{
        
    }
    
    class If extends Block{
        //cond=@num>3
        //body
        //source file
        //line
        //start end
        //{*}
        //coments
        //{/*}
        //{//coments}{{abc}}
    }
    class Elif extends Block{
        Node body;
    }
    class Else extends Span{
        //Conditional elif=new Conditional(test,body,elif/else);
        //Conditional else=new Conditional(test,body,next);
        //Conditional if=new Conditional(@num>3,body,elif/else);
        //member(num,host):object
        //const(3)
        //>/>=/</<=/!=/==
        
        
    }
    class EndIf extends Block{
        
    }
    
}
