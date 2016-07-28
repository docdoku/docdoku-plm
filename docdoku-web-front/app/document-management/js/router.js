/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator',
    'views/folder_nav',
    'views/tag_nav',
    'views/search_nav',
    'views/template_nav',
    'views/baselines/baseline_nav',
    'views/checkedout_nav',
    'views/task_nav'
],
function (Backbone, singletonDecorator, FolderNavView, TagNavView, SearchNavView, TemplateNavView, BaselineNavView, CheckedoutNavView, TaskNavView) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/folders': 'folders',
            ':workspaceId/folders/*path': 'folder',
            ':workspaceId/tags': 'tags',
            ':workspaceId/tags/:id': 'tag',
            ':workspaceId/templates': 'templates',
            ':workspaceId/baselines': 'baselines',
            ':workspaceId/checkedouts': 'checkedouts',
            ':workspaceId/tasks': 'tasks',
            ':workspaceId/tasks/:filter': 'tasks',
            ':workspaceId/search/:query': 'search',
            ':workspaceId': 'home',
            ':workspaceId/*path': 'home'
        },

	    executeOrReload:function(workspaceId,fn){
		    if(workspaceId !== App.config.workspaceId && decodeURIComponent(workspaceId).trim() !== App.config.workspaceId) {
			    location.reload();
		    }else{
			    fn.bind(this).call();
		    }
	    },
	    initNavViews: function () {
		    FolderNavView.getInstance();
		    TagNavView.getInstance();
		    TemplateNavView.getInstance();
		    BaselineNavView.getInstance();
		    CheckedoutNavView.getInstance();
		    SearchNavView.getInstance();
		    TaskNavView.getInstance();
	    },

        folders: function (workspaceId) {
            this.executeOrReload(workspaceId, function() {
	            this.initNavViews();
                FolderNavView.getInstance().show();
            });
        },
        folder: function (workspaceId, path) {
            this.executeOrReload(workspaceId, function() {
	            this.initNavViews();
                FolderNavView.getInstance().show(decodeURIComponent(path));
            });
        },
        tags: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            TagNavView.getInstance().toggle();
            });
        },
        tag: function (workspaceId, id) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            TagNavView.getInstance().show(id);
            });
        },
        templates: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            TemplateNavView.getInstance().showContent();
            });
        },
        baselines: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            BaselineNavView.getInstance().showContent();
            });
        },
        checkedouts: function (workspaceId) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            CheckedoutNavView.getInstance().showContent();
            });
        },
        tasks: function (workspaceId, filter) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            TaskNavView.getInstance().showContent(filter);
            });
        },
        search: function (workspaceId, query) {
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
	            SearchNavView.getInstance().showContent(query);
            });
        },

        home:function(workspaceId){
            this.navigate(workspaceId+'/folders',{trigger:true});
        }
    });


    return singletonDecorator(Router);
});
