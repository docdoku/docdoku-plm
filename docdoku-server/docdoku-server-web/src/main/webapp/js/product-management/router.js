define([
    "common-objects/common/singleton_decorator",
    "views/nav/product_nav",
    "views/nav/part_nav",
    "views/nav/part_template_nav"
],
    function (
        singletonDecorator,
        ProductNavView,
        PartNavView,
        PartTemplateNavView
        ) {
        var Router = Backbone.Router.extend({

            routes: {
                "products":"products",
                "parts":"parts",
                "part-templates":"partsTemplate",
                "parts-search/:query":"search",
                "":	"defaults"
            },

            products:function(){
                this.defaults();
                ProductNavView.getInstance().showContent();
            },

            parts:function(){
                this.defaults();
                PartNavView.getInstance().showContent();
            },

            search:function(query){
                PartNavView.getInstance().showContent(query);
            },

            partsTemplate:function(){
                this.defaults();
                PartTemplateNavView.getInstance().showContent();
            },

            defaults: function() {
                ProductNavView.getInstance();
                PartNavView.getInstance();
                PartTemplateNavView.getInstance();
            }

        });
        Router = singletonDecorator(Router);
        return Router;
    });
