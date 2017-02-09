/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx;

/**
 *
 * @author Senol Tas
 */
public interface ScriptEngineService {
    
    /**
     * 
     * @param source
     * @return 
     */
    public Object eval(String source);
    
        
    
}
