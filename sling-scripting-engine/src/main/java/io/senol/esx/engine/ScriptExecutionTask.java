/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.engine;

import io.senol.esx.module.ScriptModuleCache;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stas
 */
public class ScriptExecutionTask implements Runnable{
    private final Logger    log = LoggerFactory.getLogger(getClass());
    
    private ScriptEngine    scriptEngine;
    private ScriptContext   esxGlobalContext;
    private EsxScriptEngine sengine;
    private String          babelPolyfill;
    
    
    public ScriptExecutionTask() {
        
    }
    
    /**
     * 
     */
    private final String BABEL_SOURCE_CODE = "/resources/libs/esx/modules/babel/babel.min.js";
    
    /**
     * 
     */    
    private final String BABEL_POLYFILL = "/resources/libs/esx/modules/node_modules/babel-polyfill/dist/polyfill.js";    
    
    @Reference
    ScriptModuleCache moduleCache;
    
    private Object babelOptions;
    private Object Babel;

    private EsxScriptEngineFactory factory;
    
    public void ScriptExecutionTask(EsxScriptEngineFactory factory) {
        this.factory = factory;
        
    }
    
    
    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
