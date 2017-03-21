# Apache Sling ESX Scripting Engine
Node JS (like) module loader for Apache Sling.

**IMPORTANT**
> You need to enable Nashorn support in sling.properties in order to use this
> scripting engine.

e.g. in sling.properties files:
```
jre-1.8=jdk.nashorn.api.scripting;version\="0.0.0.1_008_JavaSE"
```
## Description
This bundle is registering a new Apache Sling Script Engine on the extension "ESX".
The Apache Sling ESX Script Engine is currently looking for "render" function int he exported module (esx file)

At the moment, an ESX file is a regular java script file. The Script Engine has implemented the NODE JS module resolution (see https://nodejs.org/api/modules.html for detailed description of module resoultion).

Currently there is no priority handling of global modules, but it is planed to do so.

The search algorithm will search in following order, when the regular module resultion will not find any modules:
        - /apps/esx/node_modules
        - /apps/esx/esx_modules
        - /libs/esx/node_modules
        - /libs/esx/esx_modules

Additionally, ESX will try to resolve the folder *esx_modules* prior to *node_modules*.


### Special Loaders
Require Extensions are depricated (see https://nodejs.org/api/globals.html#globals_require_extensions), therefore we have not implemented/used the extension loaders api and .bin extension cannot be used.

We have borrowed the requirejs loader plugin syntax instead (see http://requirejs.org/docs/api.html#text). Additionally to the standard JS loader following two loaders are existing:

- text (e.g. ```require("text!./templates/header.html"))```)
  - will return a javascript native string containing the content of the file
- resource  (e.g. ```require("resource!./content/blogposts)```)
  following will be exposed:
  - properties (resource valuemap)
  - path (jcr path)  
  - simpleResource (has getChildren method with resolved simpleresoruce in an array)
  - array with list of children (simpleResource)

- json loader  (e.g. ```require("./dict/en.json```)
  - the json as a whole will be exported as a javascript Object

# Installation

- mvn clean install
if you want to install it on a running instance from sling (http://localhost:8000) 
- mvn clean install sling:install

## Installing Demo Application
Currently the demo application is bundles with the engine bundle. To install the engine with the demo application, follow this steps:
- switch to directory src/main/resources/libs/esx/demo
- run: npm install
- go back to package root directory
- run mvn clean install sling:install´

open http://localhost:8000/libs/esx/demo/content/demo.html

### Writing a module
You can actually follow the NODE JS description on https://nodejs.org/api/modules.html for more detailed explanation.

A module has access to following variables:
- __filename
- __dirname
- console (console.log is a log4j logger registered to the resolved module path and is not a 1:1 console.log implementation for now)
- properties (valuemap)
- simpleResource
- currentNode
 - currentNode.path
 - currentNode.resource
 - currentNode.properties
- sling (SlingScriptHelper)


# Example
## Caluclator Module
Path: /apps/demo/components/test/helper/calculator/index.js
```javascript
function calculate(a, b) {
  return a + b;
}
exports.math = calculate;
```

## Test components
Path: /apps/demo/components/test/test.esx
```javascript
var calculator = require("./helper/calculator");

exports.render = function () {
  return calculator.math(2,2);
}
```