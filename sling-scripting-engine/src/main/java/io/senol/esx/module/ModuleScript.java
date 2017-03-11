/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import org.apache.sling.api.resource.Resource;

/**
 *
 * @author stas
 */
public class ModuleScript {
    public static int JS_FILE = 1;
    public static int JSON_FILE = 2;
    public static int RESOURCE_FILE = 3;
    public static int TEXT_FILE = 4;
    
    private int type;
    private Resource resource;
    
    public ModuleScript(int type, Resource resource) {
        this.type = type;
        this.resource = resource;
    }
    
    public boolean isJsFile() {
        return (type == JS_FILE);
    }
    
    public boolean isJsonFile() {
        return (type == JSON_FILE);
    }
    
    public boolean isResourceFile() {
        return (type == RESOURCE_FILE);
    }
    
    public boolean isTextFile() {
        return (type == TEXT_FILE);
    }
    public Resource getResource() {
        return resource;
    }
}
