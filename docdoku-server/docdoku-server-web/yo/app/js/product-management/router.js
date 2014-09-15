/*global define*/
define([
        'backbone',
        "common-objects/common/singleton_decorator",
        "views/nav/product_nav",
        "views/nav/baselines_nav",
        "views/nav/product_instances_nav",
        "views/nav/part_nav",
        "views/nav/part_template_nav"
    ],
    function (Backbone, singletonDecorator, ProductNavView, BaselinesNavView, ProductInstancesNavView, PartNavView, PartTemplateNavView) {
        var Router = Backbone.Router.extend({
            contentSelector: "#product-management-content",
            routes: {
                ":workspaceId/products": "products",
                ":workspaceId/baselines": "baselines",
                ":workspaceId/product-instances": "productInstances",
                ":workspaceId/parts": "parts",
                ":workspaceId/part-templates": "partsTemplate",
                ":workspaceId/parts-search/:query": "search",
                ":workspaceId": "defaults"
            },

            products: function (workspaceId) {
                this.defaults(workspaceId);
                ProductNavView.getInstance().showContent();
            },

            baselines: function (workspaceId) {
                this.defaults(workspaceId);
                BaselinesNavView.getInstance().showContent(this.contentSelector);
            },

            productInstances: function (workspaceId) {
                this.defaults(workspaceId);
                ProductInstancesNavView.getInstance().showContent(this.contentSelector);
            },

            parts: function (workspaceId) {
                this.defaults(workspaceId);
                PartNavView.getInstance().showContent();
            },

            search: function (workspaceId, query) {
                PartNavView.getInstance().showContent(query);
            },

            partsTemplate: function (workspaceId) {
                this.defaults(workspaceId);
                PartTemplateNavView.getInstance().showContent();
            },

            defaults: function (workspaceId) {

                if (workspaceId != APP_CONFIG.workspaceId) {
                    location.reload();
                    return;
                }

                ProductNavView.getInstance();
                BaselinesNavView.getInstance();
                ProductInstancesNavView.getInstance();
                PartNavView.getInstance();
                PartTemplateNavView.getInstance();
            }

        });
        Router = singletonDecorator(Router);
        return Router;
    });
