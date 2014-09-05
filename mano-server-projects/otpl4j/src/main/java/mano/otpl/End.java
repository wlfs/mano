/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mano.otpl;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class End extends Span {
    protected End(Parser p, int type, String s) {
        super(p, type, s);
    }
    
    public static End create(Parser parser, int type) {
        End result=new End(parser,type,null);
        return result;
    }
}
