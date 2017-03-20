/**
 * URL Module
 * only Sling specific functions are implemented from:
 * org.apache.sling.api.request Interface RequestPathInfo
 *
 * needs to be extended to support full: https://nodejs.org/api/url.html
 * This Function should return an annotated URL element including
 * sling specific informatin like suffix, resourcePath, selectors, selector string
 * @param link (location.href for instance or resoruce path)
 * @returns {URL}
 */

var _ = require("underscore");
function decompose(link) {
    var suffix;
    var selectorString;
    var selectors = [];
    var extension;
    var resourcePath;

    if(_.isNaN(link) || _.isEmpty(link)){
        throw Error("link must not be empty");
    }

    var url =  URL.parse(link);
    var pathToParse = url.pathname;
    var firstDot = url.pathname.indexOf(".");
    var pathOnly = (firstDot < 0);

    resourcePath = (pathOnly) ?  pathToParse : pathToParse.substring(0, firstDot);
    if(_.endsWith(resourcePath, "/") && resourcePath.length > 1) {
        resourcePath = resourcePath.substring(0, resourcePath.length-1);
    }

    // todo: this code looks just wrong i am sure we can cut this by half
    if(!pathOnly) {
        pathToParse = pathToParse.substring(firstDot, pathToParse.length);
        // check if there is a suffix
        var firstSlash = pathToParse.indexOf("/");
        if(firstSlash > 0) {
           suffix = pathToParse.substring(firstSlash, pathToParse.length);
           // i dont need the first dot, therefore i am starting with the next char
           pathToParse = pathToParse.substring(1, firstSlash);
        }
        var selectorArray = pathToParse.split(".");
        if(!_.isEmpty(selectorArray)) {
            // if there is only one element in the selector array
            // it is not a selector it is an extension
            if(selectorArray.length == 1) {
                var value = selectorArray[0];
                if(!_.isEmpty(value)) {
                    extension = value;
                }
            } else { // more elements, check the size
                var tmpSelectorString="";
                var lastElementExtension = selectorArray.pop();
                if(!_.isEmpty(lastElementExtension)) {
                    extension = lastElementExtension;
                }

                var first = true;
                for(var key in selectorArray) {
                    var value = selectorArray[key];
                    if(!_.isEmpty(value)) {
                       tmpSelectorString += (first) ? value : "." + value;
                       selectors.push(value);
                       first = false;
                    }
                }
                if(!_.isEmpty(tmpSelectorString)) {
                    selectorString = tmpSelectorString;
                }
            }
        }
    }

    var slingObject = {
        selectors: selectors,
        selectorString: selectorString,
        suffix: suffix,
        resourcePath: resourcePath,
        extension: extension
    }

    return _.assign(url, slingObject);
}


function cleanPath(path) {
    return (_.endsWith(path, "/")) ? path.substring(0,path.length-1) : path
}

function getAbsoluteParent(path, level){
    var idx = 0;
    var len = path.length;
    while (level >= 0 && idx < len) {
           idx = path.indexOf('/', idx + 1);
           if (idx < 0) {
                 idx = len;
            }
           level--;
     }
    return level >= 0 ? "" : path.substring(0, idx);
}


function getRelativeParent(path, level) {
    var idx = path.length;
    while (level > 0) {
            idx = path.lastIndexOf('/', idx - 1);
           if (idx < 0) {
                    return "";
                }
            level--;
        }
    return (idx == 0) ? "/" : path.substring(0, idx);
}



exports.decompose = decompose;

exports.getPathInfo = decompose;

exports.cleanPath = cleanPath;

exports.getAbsoluteParent = getAbsoluteParent;

exports.getRelativeParent = getRelativeParent;
