/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import java.io.IOException;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 *
 * @author stas
 */
@FunctionalInterface
public interface RequireApi {
    public Object require(String module) throws ScriptException,IOException;
}
