/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import javax.script.ScriptException;

/**
 *
 * @author stas
 */
public class SlingSandbox {
    boolean isAlive=true;
    
    public void checkInterrupted() throws ScriptException{
        if(!isAlive) {
            throw new ScriptException("Script Execution Timeout");
        }
    }    
    public void stop() {
        this.isAlive = false;
    }
    
}
