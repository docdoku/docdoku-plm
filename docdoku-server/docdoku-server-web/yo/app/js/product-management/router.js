/*global define*/
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
            contentSelector: '#product-management-content',
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
		        if(workspaceId !== APP_CONFIG.workspaceId) {
			        location.reload();
		        }else{
			        fn.bind(this).call();
		        }
	        },

	        initNavViews: function () {
		        ProductNavView.getInstance();
		        BaselinesNavView.getInstance();
		        ProductInstancesNavView.getInstance();
		        PartNavView.getInstance();
		        PartTemplateNavView.getInstance();
	        },

            products: function (workspaceId) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            ProductNavView.getInstance().showContent();
	            });
            },
            baselines: function (workspaceId) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            BaselinesNavView.getInstance().showContent(this.contentSelector);
	            });
            },
            productInstances: function (workspaceId) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            ProductInstancesNavView.getInstance().showContent(this.contentSelector);
	            });
            },
            parts: function (workspaceId) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            PartNavView.getInstance().showContent();
	            });
            },
            search: function (workspaceId, query) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            PartNavView.getInstance().showContent(query);
	            });
            },
            partsTemplate: function (workspaceId) {
	            this.executeOrReload(workspaceId,function(){
		            this.initNavViews();
		            PartTemplateNavView.getInstance().showContent();
	            });
            }
        });
        Router = singletonDecorator(Router);
        return Router;
    });
