define([
    "common-objects/common/singleton_decorator",
    "views/product_nav",
    "views/part_nav",
    "views/part_template_nav"
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
