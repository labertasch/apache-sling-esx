# Apache Sling ESX Scripting Engine

# IMPORTANT
You need to enable Nashorn support in sling.properties in order to use this 
scripting engine.

Node JS module loader for sling: https://nodejs.org/api/modules.html

additionally to node_modules it will also search in esx_modules with higher priority.

folllowing global module directories will be searched for:
        - /apps/esx/node_modules
        - /apps/esx/esx_modules
        - /libs/esx/node_modules
        - /libs/esx/esx_modules

## Example with NODEJS Handlebars module
'''
var Handlebars = require("handlebars");

var source = require("text!./template.html");


var template = Handlebars.compile(source);

exports.render = function() {
	return template(currentNode);
}
'''