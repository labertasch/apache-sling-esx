/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import io.senol.esx.engine.EsxScriptEngineFactory;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.sling.api.resource.Resource;

/**
 *
 * @author stas
 */
public interface ScriptResolverService {
    /**
     * returns a ScriptModule
     * @param path
     * @param currentResource
     * @return
     */
    public String require(String module, Resource currentResource);

    /**
     * returns the path reference to the requested "path"
     * @param path
     * @param currentResource
     * @return
     */
    public Resource resolve(String module, Resource currentResource);
    
    public String readScript(Resource resource);
    
    public ScriptObjectMirror resolveScript(String module, Resource currentResource, EsxScriptEngineFactory factory);
    
    
    public ScriptObjectMirror resolveScript(Resource script, Resource currentResource, EsxScriptEngineFactory factory);
    
}
