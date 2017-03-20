
var _ = require('underscore');
var pathInfo = sling.request.requestPathInfo;


/**
 *
 * @param {string} selector to be searched for
 * @return {boolean}
 */
function hasSelector(selector) {
    return _.contains(pathInfo.selectors, selector);
}

exports.extension = pathInfo.extension;
exports.hasSelector = hasSelector;
