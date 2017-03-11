var SlingSandbox = require("./sling-sandbox-plugin");
var Babel = require("babel-standalone");
var template = require("babel-template");
var t = require("babel-types");

require("babel-plugin-transform-react-jsx");

Babel.registerPlugin('SlingSandbox', SlingSandbox);



/*var input = 'while(true) { doSomething(); }; var profile = <div><h4>asdf</h4></div>';
var output = Babel.transform(input, { 
    presets: ['es2015'], 
    compact: true,
    plugins: ["SlingSandbox", ["transform-react-jsx", { "pragma" : "SlingEsx.createElement"}]] 
    }).code;

console.log(output);*/

module.exports = Babel;

/*export default function ({Plugin, types: t}) {
  return new Plugin('ast-transform', {
    visitor: {     
	   BlockStatement(path) {
        var threadInterrupted =  t.callExpression(
                    t.memberExpression(
                        t.identifier('java'), 
                        t.identifier('lang.Thread.interrupted'))
                );      
        path.body.unshift(
            t.ifStatement(threadInterrupted, t.identifier('throws "Timeout Exception";'))
        )           
	 }
    }
  });
}


function SlingSandbox() {
    return {
    visitor: {     
	   BlockStatement: function(path) {
        var threadInterrupted =  t.callExpression(
                    t.memberExpression(
                        t.identifier('java'), 
                        t.identifier('lang.Thread.interrupted'))
                );      
        path.body.unshift(
            t.ifStatement(threadInterrupted, t.identifier('throws "Timeout Exception";'))
        )           
	 }
    }
  }
}
Babel.registerPlugin('lolizer', SlingSandbox);




function SlingSandbox(t}) {
  return new Plugin('ast-transform', {
    visitor: {     
	   BlockStatement: function(path) {
        var threadInterrupted =  t.callExpression(
                    t.memberExpression(
                        t.identifier('java'), 
                        t.identifier('lang.Thread.interrupted'))
                );      
        path.body.unshift(
            t.ifStatement(threadInterrupted, t.identifier('throws "Timeout Exception";'))
        )           
	 }
    }
  });
}*/