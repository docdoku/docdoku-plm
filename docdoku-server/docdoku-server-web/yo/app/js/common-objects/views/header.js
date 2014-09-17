/*global define*/
define([
	'backbone',
	'mustache',
	'text!common-objects/templates/header.html'
],function (Backbone, Mustache, template) {
	'use strict';
	var HeaderView = Backbone.View.extend({
         el: $("#header"),

         render: function () {

	         var $el = this.$el;

			var workspaces = APP_CONFIG.workspaces;

                var otherWorkspaces = _.filter(workspaces.allWorkspaces, function (workspace) {
                    return !_.findWhere(workspaces.administratedWorkspaces, workspace);
                });

                _.each(workspaces.administratedWorkspaces,function(workspace){
                    workspace.isCurrent = workspace.id === APP_CONFIG.workspaceId;
                });

                _.each(otherWorkspaces,function(workspace){
                    workspace.isCurrent = workspace.id === APP_CONFIG.workspaceId;
                });

                $el.html(Mustache.render(template, {
                    currentWorkspace: APP_CONFIG.workspaceId,
                    contextPath: APP_CONFIG.contextPath,
                    administratedWorkspaces: workspaces.administratedWorkspaces,
                    otherWorkspaces: otherWorkspaces,
                    i18n: APP_CONFIG.i18n,
                    userName: APP_CONFIG.userName,
                    isDocumentManagement: window.location.pathname.match('/document-management/'),
                    isProductManagement: window.location.pathname.match('/product-management/'),
                    isProductStructure: window.location.pathname.match('/product-structure/'),
                    isChangeManagement: window.location.pathname.match('/change-management/')
                }));

                $el.show();

                return this;
            }

        });

        return HeaderView;
    });