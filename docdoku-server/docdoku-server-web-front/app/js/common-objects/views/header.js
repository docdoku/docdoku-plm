/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/header.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var HeaderView = Backbone.View.extend({
        el: '#header',

        render: function () {

            var $el = this.$el;

            var workspaces = App.config.workspaces;

            var otherWorkspaces = _.filter(workspaces.allWorkspaces, function (workspace) {
                return !_.findWhere(workspaces.administratedWorkspaces, workspace);
            });

            function isCurrent(workspace){
                workspace.isCurrent = workspace.id === App.config.workspaceId;
            }
            _.each(workspaces.administratedWorkspaces, isCurrent);
            _.each(otherWorkspaces, isCurrent);

            $el.html(Mustache.render(template, {
                currentWorkspace: App.config.workspaceId,
                contextPath: App.config.contextPath,
                administratedWorkspaces: workspaces.administratedWorkspaces,
                otherWorkspaces: otherWorkspaces,
                i18n: App.config.i18n,
                userName: App.config.userName,
                isDocumentManagement: window.location.pathname.match('/document-management/'),
                isProductManagement: window.location.pathname.match('/product-management/'),
                isProductStructure: window.location.pathname.match('/product-structure/'),
                isChangeManagement: window.location.pathname.match('/change-management/'),
                isWorkspaceManagement: window.location.pathname.match('/workspace-management/'),
            }));

            $el.show().addClass('loaded');

            if(this.CoWorkersView) {
                var CoWorkersView = this.CoWorkersView;
                new CoWorkersView().render();
            }

            return this;
        },

        removeActionDisabled: function () {
            this.$('#coworkers_access_module_entries').find('.fa-globe').removeClass('corworker-action-disable').addClass('corworker-action');
        },
        addActionDisabled: function () {
            this.$('#coworkers_access_module_entries').find('.fa-globe').removeClass('corworker-action').addClass('corworker-action-disable');
        },

        setCoWorkersView:function(View){
            this.CoWorkersView = View;
        }

    });

    return HeaderView;
});
