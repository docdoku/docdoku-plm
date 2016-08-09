/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'views/nav/product_nav',
    'views/nav/configuration_nav',
    'views/nav/baselines_nav',
    'views/nav/product_instances_nav',
    'views/nav/part_nav',
    'views/nav/part_template_nav',
    'views/nav/checkedouts_nav',
    'views/nav/tag_nav'
],
function (Backbone, singletonDecorator, ProductNavView, ConfigurationNavView, BaselinesNavView, ProductInstancesNavView, PartNavView, PartTemplateNavView, CheckedOutNavView, TagNavView) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/products': 'products',
            ':workspaceId/configurations': 'configurations',
            ':workspaceId/baselines': 'baselines',
            ':workspaceId/product-instances': 'productInstances',
            ':workspaceId/parts': 'parts',
            ':workspaceId/tags/:tag': 'tags',
            ':workspaceId/checkedouts': 'checkedoutsParts',
            ':workspaceId/part-templates': 'partsTemplate',
            ':workspaceId/parts-search/:query': 'search',
            ':workspaceId': 'products',
            ':workspaceId/*path': 'products'
        },

        executeOrReload:function(workspaceId,fn){
	        if(workspaceId !== App.config.workspaceId && decodeURIComponent(workspaceId).trim() !== App.config.workspaceId) {
		        location.reload();
	        }else{
		        fn.bind(this).call();
	        }
        },

        initNavViews: function () {
	        ProductNavView.getInstance().cleanView();
            ConfigurationNavView.getInstance().cleanView();
	        BaselinesNavView.getInstance().cleanView();
	        ProductInstancesNavView.getInstance().cleanView();
	        PartNavView.getInstance().cleanView();
	        PartTemplateNavView.getInstance().cleanView();
	        CheckedOutNavView.getInstance().cleanView();
            TagNavView.getInstance();
        },

        products: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            ProductNavView.getInstance().showContent();
            });
        },
        configurations: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
                ConfigurationNavView.getInstance().showContent();
            });
        },
        baselines: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            BaselinesNavView.getInstance().showContent();
            });
        },
        productInstances: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            ProductInstancesNavView.getInstance().showContent();
            });
        },
        parts: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            PartNavView.getInstance().showContent();
            });
        },
        checkedoutsParts:function(workspaceId){
            this.executeOrReload(workspaceId,function(){
                this.initNavViews();
                CheckedOutNavView.getInstance().showContent();
            });
        },
        tags:function(workspaceId,tag){
            this.executeOrReload(workspaceId,function(){
                this.initNavViews();
                TagNavView.getInstance().showContent(tag);
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
