/*global APP_CONFIG*/
'use strict';
define([
	'common-objects/common/singleton_decorator',
	'views/folder_nav',
	'views/tag_nav',
	'views/search_nav',
	'views/template_nav',
	'views/checkedout_nav',
	'views/task_nav'
],
function (
	singletonDecorator,
	FolderNavView,
	TagNavView,
	SearchNavView,
	TemplateNavView,
	CheckedoutNavView,
	TaskNavView
) {
	var Router = Backbone.Router.extend({
		routes: {
			'configspec/:configSpec/*path':   'configspec',
			'folders':			              'folders',
			'folders/*path':	              'folder',
			'tags':				              'tags',
			'tags/:id':                       'tag',
			'templates':		              'templates',
			'checkedouts':		              'checkedouts',
			'tasks':	                      'tasks',
			'tasks/:filter':	              'tasks',
			'search/:query':	              'search',
			'':					              'defaults'
		},
		configspec: function(configSpec,path){
			APP_CONFIG.configSpec =  configSpec;
			var slashPosition = path.indexOf('/');
			var module;
			var option;
			if(slashPosition>0){
				module = path.substring(0, path.indexOf('/'));
				option = path.substring(path.indexOf('/')+1);
			}else{
				module = path;
			}

			switch (module){
				case 'tag':
					if(option){
						this.tag(option);
					}else{
						this.tags();
					}
					break;
				case 'search':
					if(option) {
						this.search(option);
					}
					break;
				default:
					if(option){
						this.folder(option);
					}else{
						this.folders();
						FolderNavView.getInstance().hide();
					}
					break;
			}
			if(configSpec!=='latest'){
				// Hide some nav view when you selected a baseline
				TagNavView.getInstance().$el.hide();
				TemplateNavView.getInstance().$el.hide();
				CheckedoutNavView.getInstance().$el.hide();
				TaskNavView.getInstance().$el.hide();
			}else{
				// Show all nav view when you on latest
				TagNavView.getInstance().$el.show();
				TemplateNavView.getInstance().$el.show();
				CheckedoutNavView.getInstance().$el.show();
				TaskNavView.getInstance().$el.show();
			}
		},
		folders: function() {
			this.defaults();
			FolderNavView.getInstance().toggle();
		},
		folder: function(path) {
			this.defaults();
            FolderNavView.getInstance().show(decodeURIComponent(path));
        },
		tags: function() {
			this.defaults();
			TagNavView.getInstance().toggle();
		},
		tag: function(id) {
			this.defaults();
			TagNavView.getInstance().show(id);
		},
		templates: function() {
			this.defaults();
			TemplateNavView.getInstance().showContent();
		},
		checkedouts: function() {
			this.defaults();
			CheckedoutNavView.getInstance().showContent();
		},
		tasks: function(filter) {
			this.defaults();
            TaskNavView.getInstance().showContent(filter);
		},
        search: function(query) {
            this.defaults();
            SearchNavView.getInstance().showContent(query);
        },
		defaults: function() {
			FolderNavView.getInstance();
			TagNavView.getInstance();
			TemplateNavView.getInstance();
			CheckedoutNavView.getInstance();
            SearchNavView.getInstance();
            TaskNavView.getInstance();
		}
	});
	Router = singletonDecorator(Router);
	return Router;
});