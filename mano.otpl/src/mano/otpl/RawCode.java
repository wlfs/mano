/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mano.otpl;

import mano.otpl.Span;

/**
 *
 * @author jun <jun@diosay.com>
 */
public class RawCode extends Span {
    public RawCode(Parser p, String name, String s) {
        super(p, 0, s);
    }
}
