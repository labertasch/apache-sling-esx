/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import java.io.IOException;
import javax.script.ScriptException;

/**
 *
 * @author stas
 */
@FunctionalInterface
public interface Require {
    public Object require(String id) throws ScriptException,IOException;
}
