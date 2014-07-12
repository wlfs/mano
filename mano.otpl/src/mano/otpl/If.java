/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mano.otpl;

import mano.otpl.Node;
import mano.otpl.Block;
import java.util.LinkedHashSet;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class If extends Block {

    public If(Parser p, String name, String s) {
        super(p, 0, s);
    }

    @Override
    public boolean canEnd(Node node) {
        if (node == null || node.getName() == null) {
            return false;
        } else if (node.getName().equals("endif") || node.getName().equals("elif") || node.getName().equals("else")) {
            return true;
        }

        return false;
    }
}
