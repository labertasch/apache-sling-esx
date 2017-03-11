/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.senol.esx.module;

import io.senol.esx.engine.EsxScriptEngineFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stas
 */
public class Module extends SimpleBindings implements Require {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ScriptEngine engine;
    private final EsxScriptEngineFactory factory;
    private final Resource resource;
    private final Resource moduleResource;

    private final List<Module> children = new ArrayList();

    private boolean isLoaded = false;

    private final Module main;

    private Object exports;

    private String mainScript = null;

    private final String id;

    private final ConsoleLog console;

    private final ModuleScript moduleScript;
    private final SimpleBindings moduleContext;

    private final SlingSandbox sandbox;

    /**
     *
     * @param factory
     * @param resource
     * @param moduleScript
     * @param id
     * @param parent
     * @param mainScript
     * @param sandbox
     * @throws ScriptException
     */
    public Module(EsxScriptEngineFactory factory, Resource resource, ModuleScript moduleScript,
            String id, Module parent, String mainScript, SlingSandbox sandbox) throws ScriptException {

        this.resource = resource;
        this.engine = factory.getGlobalEngine();

        this.factory = factory;
        this.moduleResource = moduleScript.getResource();
        this.moduleScript = moduleScript;

        this.main = (parent == null) ? this : (Module) parent.get("main");
        this.mainScript = mainScript;
        this.id = id;

        JSObject jsObject = (JSObject) engine.eval("Object");
        this.exports = (JSObject) jsObject.newObject();
        this.moduleContext = new SimpleBindings();
        this.console = new ConsoleLog(moduleResource.getPath());
        this.sandbox = sandbox;

        moduleContext.put("id", id);
        moduleContext.put("loaded", this.isLoaded);
        moduleContext.put("main", this.main);
        moduleContext.put("filename", moduleResource.getPath());
        moduleContext.put("resource", resource);
        moduleContext.put("exports", this.exports);
        moduleContext.put("children", children);
        moduleContext.put("moduleResource", moduleResource);
        moduleContext.put("console", console);
        moduleContext.put("log", console.getLogger());
        moduleContext.put("SlingSandbox", this.sandbox);

    }

    /**
     *
     * @return
     */
    public Object getExports() {
        return this.exports;
    }

    /**
     *
     * @return
     */
    public Resource getModuleResource() {
        return this.moduleResource;
    }

    /**
     *
     * @return
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     *
     * @param source
     * @return
     * @throws ScriptException
     */
    private ScriptObjectMirror decoreateScript(String source)
            throws ScriptException {

        source = "//@sourceURL=" + this.moduleResource.getPath() + "\n"
                + "(function (exports, require, module, __filename,"
                + " __dirname, currentNode, console, log, properties,SlingSandbox) { var window = {}; window.document = {}; var document = window.document;\n"
                //
                //                + factory.getBabelPolyfill()
                //+ "exports = new Object(); exports.prototype = module.exports;"
                + source
                + "})";

        source = "load( { name : \"" + this.moduleResource.getPath() + "\","
                + " script: \""
                + StringEscapeUtils.escapeEcmaScript(source)
                + "\" } )";

        ScriptObjectMirror function = null;
        try {
            function = (ScriptObjectMirror) engine.eval(
                    source
            );
            if (function == null) {
                log.error("Function is null !");
            }
        } catch (ScriptException ex) {
            // todo: better handling in future
            throw ex;
        }
        return function;
    }

    /**
     *
     * @param code
     * @return
     * @throws ScriptException
     */
    private String transformCode(String code) throws ScriptException {
        try {
            Invocable inv = (Invocable) engine;
            Object Babel = factory.getBabel();
            Object data = factory.getBabelOptions();
            JSObject rs = (JSObject) inv.invokeMethod(Babel, "transform", code, data);
            code = rs.getMember("code").toString();
            return code;
        } catch (NoSuchMethodException ex) {
            throw new ScriptException(ex);
        }
    }

    /**
     *
     *
     * @return @throws ScriptException
     */
    public Object runMainScript() throws ScriptException {

        ScriptObjectMirror function = factory.getModuleCache().get(this.moduleScript.getResource().getPath());

        if (function == null) {
            function = decoreateScript(
                    transformCode(mainScript)
            //mainScript
            );
            factory.getModuleCache().put(this.moduleScript.getResource().getPath(), function);
        }

        SimpleBindings currentNode = new SimpleBindings();
        if (this.resource != null) {
            currentNode.put("resource", this.resource);
            currentNode.put("properties", this.resource.adaptTo(ValueMap.class));
        } else {
            log.debug("module id " + this.id + " resource is null");
        }

        function.call(moduleContext, this.exports, this, moduleContext, this.getModuleResource().getPath(),
                this.getModuleResource().getParent().getPath(), currentNode, moduleContext.get("console"), moduleContext.get("log"), null, this.sandbox);
        moduleContext.put("exports", this.moduleContext.get("exports"));

        // todo: make smarter - check if render exists, otherwise checkif exports is a function or throw propper warning
        return engine.eval("exports.render.bind(this)() ", moduleContext).toString();
    }

    /**
     *
     * @return @throws ScriptException
     */
    public Object runScript() throws ScriptException {
        log.debug("runScript  id: " + id);
        ScriptObjectMirror function = factory.getModuleCache().get(this.moduleScript.getResource().getPath());

        if (function == null) {
            if (this.moduleScript.isJsFile()) {
                function = decoreateScript(
                        //readScript(moduleResource)//  
                        transformCode(readScript(moduleResource))
                );
            }

            if (moduleScript.isJsonFile()) {
                String jsonfile = readScript(moduleResource);
                function = decoreateScript(
                        "module.exports = " + jsonfile
                );
            }

            if (moduleScript.isResourceFile()) {
                Iterator<Resource> resChildren = moduleResource.listChildren();
                List<Resource> children = new ArrayList<>();
                resChildren.forEachRemaining(children::add);

                moduleContext.put("children", children.toArray());

                ValueMap map = this.moduleResource.adaptTo(ValueMap.class);

                JSONObject values = new JSONObject(map);

                String jsonprop = values.toString();

                String source = "exports.properties =  " + jsonprop + ";"
                        + "exports.path = '" + moduleResource.getPath() + "';"
                        + "exports.children = this.children;";

                function = decoreateScript(source);
            }

            factory.getModuleCache().put(this.moduleScript.getResource().getPath(), function);
        } else {
            log.debug("module " + id + " received from cache");
        }

        if (moduleScript.isTextFile()) {
            log.debug("is textfile loaidng file");
            String source = StringEscapeUtils.escapeEcmaScript(readScript(moduleResource));
            log.debug("sourcE: ");
            source = "module.exports = \"" + source + "\";";
            log.debug(source);
            function = decoreateScript(source);
            factory.getModuleCache().put(this.moduleScript.getResource().getPath(), function);
        }

        if (function != null) {
            SimpleBindings currentNode = new SimpleBindings();
            if (this.resource != null) {
                currentNode.put("resource", this.resource);
                currentNode.put("properties", this.resource.adaptTo(ValueMap.class));
            } else {
                log.debug("module id " + this.id + " resource is null");
            }

            function.call(moduleContext, this.exports, this, moduleContext, this.getModuleResource().getPath(),
                    this.getModuleResource().getParent().getPath(), currentNode, moduleContext.get("console"), moduleContext.get("log"), null, this.sandbox);

        } else {
            log.warn("function not called because it is null");
        }

        exports = moduleContext.get("exports");

        return exports;
    }

    /**
     *
     * @param script
     * @return
     */
    public String readScript(Resource script) {
        InputStream is = script.getChild("jcr:content").adaptTo(InputStream.class);
        BufferedReader esxScript = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String temp;
        try {
            while ((temp = esxScript.readLine()) != null) {
                buffer.append(temp).append("\r\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            log.error("Error reading file", e);
        }
        return null;
    }

    /**
     *
     * @param path
     * @return
     */
    private boolean isLocalModule(String path) {
        return (path.startsWith("./") == true
                || path.startsWith("/") == true
                || path.startsWith("../") == true);
    }

    /**
     *
     * @param path
     * @param basePath
     * @return
     */
    private String normalizePath(String path, String basePath) {
        path = StringUtils.removeStart(path, basePath);
        return ResourceUtil.normalize(basePath + "/" + path);
    }

    /**
     * not implemented yet
     *
     * @param path
     * @return
     */
    private boolean isGlobalModule(String path) {
        return false;
    }

    /**
     *
     * @param file
     * @param type
     * @return
     * @throws ScriptException
     */
    private ModuleScript createModuleScript(Resource file, int type) throws ScriptException {
        log.debug("module created. " + file.getPath());
        Node currentNode = file.adaptTo(Node.class);
        if (currentNode != null) {
            log.debug("currentNode !) null = " + (currentNode != null));
            try {
                boolean isFile = currentNode.isNodeType(NodeType.NT_FILE);
                log.debug("isFile: " + isFile);
                if (isFile) {
                    return new ModuleScript(type, file);
                }
                log.debug("not a file " + currentNode.getMixinNodeTypes().toString());
            } catch (RepositoryException ex) {
                throw new ScriptException("cannot load file " + file.getPath());
            }
        }
        return null;

    }

    /**
     *
     * @param module
     * @param path
     * @param currentResource
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadAsFile(String module, String path,
            Resource currentResource) throws ScriptException {
        int type = ModuleScript.JS_FILE;

        log.debug("p = " + path);
        Resource file = currentResource.getResourceResolver().getResource(path);

        // require.extensions is deprecated, however to implement this might 
        // be a good way to handle .resource loading or to implemend loader 
        // like in requirejs e.g. similar to https://github.com/requirejs/text
        // "text!some/module.html" 
        // or require("resource!/content/homepage/jcr:content")
        if (module.endsWith("!resource") && file == null) {
            file = currentResource.getResourceResolver().getResource(
                    path.substring(0, path.length() - "!resource".length())
            );

            if (file != null) {
                return new ModuleScript(ModuleScript.RESOURCE_FILE, file);
            } else {
                throw new ScriptException("resource " + module + " not found!");
            }
        } else {
            log.debug("not a resource module " + module);
        }
        if (module.endsWith("!text") && file == null) {
            file = currentResource.getResourceResolver().getResource(
                    path.substring(0, path.length() - "!text".length())
            );

            if (file != null) {
                log.debug("reutnr text file module thing in module scriot");
                log.debug(file.getPath());
                return new ModuleScript(ModuleScript.TEXT_FILE, file);
            } else {
                throw new ScriptException("text file " + module + " not found!");
            }
        } else {
            log.debug("not a text module " + module);
        }
        try {
            if (file == null || !file.adaptTo(Node.class).isNodeType(NodeType.NT_FILE)) {
                file = currentResource.getResourceResolver().getResource(path + ".js");
                if (file == null) {
                    file = currentResource.getResourceResolver().getResource(path + ".json");
                    if (file == null) {
                        return null;
                    }
                    type = ModuleScript.JSON_FILE;
                } else {
                    type = ModuleScript.JS_FILE;
                }
            }
        } catch (RepositoryException ex) {
            log.error(module + "", ex);
        }
        return createModuleScript(file, type);
    }

    /**
     *
     * @param module
     * @param path
     * @param currentResource
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadAsDirectory(String module, String path, Resource currentResource) throws ScriptException {

        ResourceResolver resolver = currentResource.getResourceResolver();
        Resource packageJson = resolver.getResource(path + "/package.json");

        if (packageJson != null) {
            Node jsonFile = packageJson.adaptTo(Node.class);

            try {
                boolean isFile = (jsonFile.isNodeType(NodeType.NT_FILE) || jsonFile.isNodeType(NodeType.NT_RESOURCE));

                if (isFile) {

                    InputStream is = packageJson.getChild("jcr:content").adaptTo(InputStream.class);
                    try {
                        String jsonData = IOUtils.toString(is);

                        JSONObject json = new JSONObject(jsonData);

                        if (json.has("main")) {
                            String packageModule = json.getString("main");

                            String mainpath = normalizePath(packageModule,
                                    path);

                            return loadAsFile(packageModule, mainpath, currentResource);
                        }

                    } catch (JSONException ex) {
                        throw new ScriptException(ex);
                    } catch (IOException ex) {
                        throw new ScriptException(ex);
                    }
                }

            } catch (RepositoryException ex) {
                throw new ScriptException(ex);
            }

        }

        Resource indexjs = resolver.getResource(path + "/index.js");

        if (indexjs != null) {
            return createModuleScript(indexjs, ModuleScript.JS_FILE);
        }

        Resource indexjson = resolver.getResource(path + "/index.json");

        if (indexjson != null) {
            return createModuleScript(indexjson, ModuleScript.JSON_FILE);
        }

        Resource indexnode = resolver.getResource(path + "/index.node");
        if (indexnode != null) {
            throw new ScriptException("Node module .node (binary) loading is currently not supported");
        }

        return null;
    }

    /**
     *
     * @param module
     * @param currentResource
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadAsModule(String module, Resource currentResource) throws ScriptException {
        return loadAsModule(module, currentResource, true);
    }

    /**
     *
     * @param paths
     * @return
     */
    private String[] loadModulePaths(String paths) {
        String[] parts = paths.split("/");
        List<String> dirs = new ArrayList<String>();

        for (int i = (parts.length - 1); i > 0;) {
            log.debug(parts[i]);
            if (parts[i] == "node_modules" || parts[i] == "esx_modules") {
                continue;
            }
            String part = StringUtils.join(parts, "/", 0, i);
            String dir = part + "/node_modules";
            log.debug("load dir: " + dir);
            dirs.add(part + "/esx_modules");
            dirs.add(dir);
            i = i - 1;
        }

        // if the regular module resoultion is not finding anything, try and check the
        // global paths. needs to be optimized.
        dirs.add("/apps/esx/node_modules");
        dirs.add("/apps/esx/esx_modules");
        dirs.add("/libs/esx/node_modules");
        dirs.add("/libs/esx/esx_modules");

        return dirs.stream().toArray(String[]::new);
    }

    /**
     *
     * @param module
     * @param currentResource
     * @param isFileResource
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadAsModule(String module, Resource currentResource, boolean isFileResource) throws ScriptException {
        ModuleScript script = null;

        String[] dirs = loadModulePaths(currentResource.getPath());
        log.debug("loading modules from dir path");
        for (String dir : dirs) {
            log.debug("trying to resolve. " + dir);
            Resource searchPath = currentResource.getResourceResolver().resolve(dir);
            log.debug("searchpath  = " + searchPath.getPath());
            if (searchPath != null && !ResourceUtil.isNonExistingResource(searchPath)) {
                log.debug("searchpath loadasmodule: " + searchPath.getPath());
                script = loadLocalModule(module, searchPath, false);
                if (script != null) {
                    return script;
                }
            } else {
                log.debug("dir is null = " + dir);
            }
        }
        log.debug("finsihed loading dirs, script is loaded?!");
        return script;
    }

    /**
     *
     * @param module
     * @param currentResource
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadLocalModule(String module, Resource currentResource) throws ScriptException {
        return loadLocalModule(module, currentResource, false);
    }

    /**
     *
     * @param resource
     * @return
     */
    private boolean resourceIsfile(Resource resource) {
        try {
            return resource.adaptTo(Node.class).isNodeType(NodeType.NT_FILE);
        } catch (RepositoryException ex) {
            log.error("resourceIsfile", ex);
        }
        return false;
    }

    /**
     *
     * @param module
     * @param currentResource
     * @param isFile
     * @return
     * @throws ScriptException
     */
    public ModuleScript loadLocalModule(String module, Resource currentResource, boolean isFile) throws ScriptException {
        String basePath = (resourceIsfile(currentResource))
                ? currentResource.getParent().getPath() : currentResource.getPath();
        String path = normalizePath(module, basePath);

        ModuleScript script = loadAsFile(module, path, currentResource);

        if (script != null) {
            return script;
        }

        return loadAsDirectory(module, path, currentResource); // load as directory                              
    }

    /**
     * p´
     *
     * @param module
     * @param currentResource
     * @return
     * @throws javax.script.ScriptException
     */
    public ModuleScript resolve(String module, Resource currentResource) throws ScriptException {
        // if x is core module / library return directly the one
        log.debug("resolving module: " + module);
        ModuleScript script;

        if (isGlobalModule(module)) {
            // ignore for now
        }

        if (isLocalModule(module)) {
            script = loadLocalModule(module, currentResource);
            if (script != null) {
                return script;
            }
        }

        // load as module (first split path, then load)
        script = loadAsModule(module, currentResource);
        if (script != null) {
            return script;
        }

        throw new ScriptException("module not found " + module);
    }

    /**
     *
     * @param id
     * @return
     * @throws ScriptException
     * @throws IOException
     */
    @Override
    public Object require(String id) throws ScriptException, IOException {
        log.debug("require id: " + id);
        ModuleScript subModuleScript = resolve(id, this.getModuleResource());
        Module submodule = new Module(factory, this.resource, subModuleScript, id, this, null, this.sandbox);
        Object result = submodule.runScript();
        submodule.isLoaded = true;
        children.add(submodule);
        return result;
    }
}