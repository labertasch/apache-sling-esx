var Handlebars = require("handlebars");
var marked = require("marked");
var URL = require("url");

var renderer = new marked.Renderer();
renderer.blockquote = function (quote) {
  return "<blockquote class='blockquote'>" + quote + "</blockquote>";
}

function resolveTitle(res) {
   return  res.properties.navTitle || res.properties.title || res.properties["jcr:title"] || res.name;

}

function BlogComponent () {
  this.templateURL = "../../hbstemplates/layout.html"
  this.basePath = URL.getAbsoluteParent(currentNode.resource.path, 4);
  this.baseResource = require("resource!" + this.basePath);
  this.stylesheet = "";
  this.partialContentTemplateURL = "../../hbstemplates/content.html";
  this.model = currentNode;
  this.model.title = currentNode.properties.pageTitle || currentNode.properties.title || currentNode.properties["jcr:title"] || currentNode.resource.name;
}

BlogComponent.prototype.transformMarkdown = function (content) {
  return marked(content, { renderer: renderer});
}
BlogComponent.prototype.pages = function () {
  var pages = [];
  var homePage =
    pages.push({
        path: this.basePath,
        title: resolveTitle(this.baseResource),
        active: (currentNode.resource.path === this.basePath ? 'active' : '')
    });

  var rootPage = require("resource!" + this.basePath + "/pages");
   var children = rootPage.simpleResource.children;

    for(var key in children) {
        var child =  children[key];
        var nav = {};
        nav.path =child.path;
        nav.title =  resolveTitle(child) ;
        nav.active = (currentNode.resource.path === child.path ? 'active' : '');
        pages.push(nav);
    }

  return pages;
}

BlogComponent.prototype.render = function () {
  this.init();
  Handlebars.registerPartial('content',require("text!" + this.partialContentTemplateURL));


  var templateSource= require("text!" + this.templateURL);
  var template = Handlebars.compile(templateSource);
  this.model.style = this.stylesheet;
  this.model.pagesNav = this.pages();

  return template(this.model);
}

module.exports = BlogComponent;
