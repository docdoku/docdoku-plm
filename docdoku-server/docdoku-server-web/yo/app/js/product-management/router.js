/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'views/nav/product_nav',
    'views/nav/baselines_nav',
    'views/nav/product_instances_nav',
    'views/nav/part_nav',
    'views/nav/part_template_nav'
],
function (Backbone, singletonDecorator, ProductNavView, BaselinesNavView, ProductInstancesNavView, PartNavView, PartTemplateNavView) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/products': 'products',
            ':workspaceId/baselines': 'baselines',
            ':workspaceId/product-instances': 'productInstances',
            ':workspaceId/parts': 'parts',
            ':workspaceId/part-templates': 'partsTemplate',
            ':workspaceId/parts-search/:query': 'search',
            ':workspaceId': 'products'
        },

        executeOrReload:function(workspaceId,fn){
	        if(workspaceId !== App.config.workspaceId) {
		        location.reload();
	        }else{
		        fn.bind(this).call();
	        }
        },

        cleanContent: function () {
            ProductNavView.getInstance().cleanView();
            BaselinesNavView.getInstance().cleanView();
            ProductInstancesNavView.getInstance().cleanView();
            PartNavView.getInstance().cleanView();
            PartTemplateNavView.getInstance().cleanView();
        },

        products: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
                this.cleanContent();
	            ProductNavView.getInstance().showContent();
            });
        },
        baselines: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
                this.cleanContent();
	            BaselinesNavView.getInstance().showContent();
            });
        },
        productInstances: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
                this.cleanContent();
	            ProductInstancesNavView.getInstance().showContent();
            });
        },
        parts: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
                this.cleanContent();
	            PartNavView.getInstance().showContent();
            });
        },
        search: function (workspaceId, query) {
            this.executeOrReload(workspaceId,function(){
	            this.cleanContent();
	            PartNavView.getInstance().showContent(query);
            });
        },
        partsTemplate: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.cleanContent();
	            PartTemplateNavView.getInstance().showContent();
            });
        }
    });
    Router = singletonDecorator(Router);
    return Router;
});
