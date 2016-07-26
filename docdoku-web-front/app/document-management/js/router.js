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
            ':workspaceId/(configspec/:configSpec/)folders':   'folders',
            ':workspaceId/(configspec/:configSpec/)folders/*path':   'folder',
            ':workspaceId/tags': 'tags',
            ':workspaceId/tags/:id': 'tag',
            ':workspaceId/templates': 'templates',
            ':workspaceId/baselines': 'baselines',
            ':workspaceId/checkedouts': 'checkedouts',
            ':workspaceId/tasks': 'tasks',
            ':workspaceId/tasks/:filter': 'tasks',
            ':workspaceId/search/:query': 'search',
            ':workspaceId': 'home'
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

        folders: function (workspaceId,configSpec) {
            if(!configSpec){
                return this.home(workspaceId);
            }
            App.config.documentConfigSpec =  configSpec || 'latest';
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
                this.configSpecAdaptMenu(configSpec);
                FolderNavView.getInstance().show();
            });
        },
        folder: function (workspaceId, configSpec, path) {
            if(!configSpec){
                return this.navigate(workspaceId+ '/configspec/latest/folders/'+ path,{trigger:true});
            }
            App.config.documentConfigSpec =  configSpec || 'latest';
            this.executeOrReload(workspaceId,function(){
	            this.initNavViews();
                this.configSpecAdaptMenu(configSpec);
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
            this.navigate(workspaceId+'/configspec/latest/folders',{trigger:true});
        },

	    configSpecAdaptMenu:function(configSpec){

            // Hide/show some nav view when you selected a baseline
            var isLatest = configSpec === 'latest';

            TagNavView.getInstance().$el.toggle(isLatest);
            TemplateNavView.getInstance().$el.toggle(isLatest);
            CheckedoutNavView.getInstance().$el.toggle(isLatest);
            TaskNavView.getInstance().$el.toggle(isLatest);
		    SearchNavView.getInstance().$el.toggle(isLatest);
            App.appView.$linksNav.toggle(isLatest);

            FolderNavView.getInstance().refresh();
	    }
    });


    return singletonDecorator(Router);
});
