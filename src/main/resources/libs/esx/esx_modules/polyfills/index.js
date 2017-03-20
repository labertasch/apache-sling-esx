var eventLoop = require("./lib/event-loop.js");
var environment = require("./lib/environment.js");

module.exports = function (context) {    
    eventloop(context);
    environment(context);    
}
