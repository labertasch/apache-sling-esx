/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import javax.script.CompiledScript;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.event.EventHandler;

/**
 *
 * @author stas
 */
public interface ScriptModuleCache extends EventHandler{

    /**
     * put compiled script into script module cache
     * @param path
     * @param script
     */
    public void put(String module, ScriptObjectMirror script);

    /**
     * get compiled script or null if the script is not in the cache
     * @param path
     * @return
     */
    public ScriptObjectMirror get(String module);

    /**
     * 
     * @param resource
     * @return 
     */
    public ScriptObjectMirror get(Resource resource);

    /**
     * removing module script from cache if existing otherwise
     * doesn't do anything (return always true)
     *
     * return false if for some reason cannot be flushed from cache
     *
     * @param path
     */
    public boolean flush(String module);
}
