define([
    "common-objects/common/singleton_decorator",
    "views/nav/product_nav",
    "views/nav/baselines_nav",
    "views/nav/part_nav",
    "views/nav/part_template_nav"
],
    function (
        singletonDecorator,
        ProductNavView,
        BaselinesNavView,
        PartNavView,
        PartTemplateNavView
        ) {
        var Router = Backbone.Router.extend({
            contentSelector: "#product-management-content",
            routes: {
                "products"          : "products",
                "baselines"         : "baselines",
                "parts"             : "parts",
                "part-templates"    : "partsTemplate",
                "parts-search/:query":"search",
                ""                  : "defaults"
            },

            products:function(){
                this.defaults();
                ProductNavView.getInstance().showContent();
            },

            baselines: function(){
                this.defaults();
                BaselinesNavView.getInstance().showContent(this.contentSelector);
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
                BaselinesNavView.getInstance();
                PartNavView.getInstance();
                PartTemplateNavView.getInstance();
            }

        });
        Router = singletonDecorator(Router);
        return Router;
    });
