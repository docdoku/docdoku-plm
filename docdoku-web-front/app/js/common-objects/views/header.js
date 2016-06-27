/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/header.html'
], function (Backbone, Mustache, template) {
    'use strict';



    var HeaderView = Backbone.View.extend({
        el: '#header',

        events:{
            'click #logout_link a':'logout'
        },

        render: function () {

            var $el = this.$el;

            var workspaces = App.config.workspaces;

            function isCurrent(workspace){
                workspace.isCurrent = workspace.id === App.config.workspaceId;
            }

            if(workspaces){
                _.each(workspaces.administratedWorkspaces, isCurrent);
                _.each(workspaces.nonAdministratedWorkspaces, isCurrent);
            }

            $el.html(Mustache.render(template, {
                connected:App.config.connected,
                currentWorkspace: App.config.workspaceId,
                contextPath: App.config.contextPath,
                administratedWorkspaces: workspaces ? workspaces.administratedWorkspaces: null,
                nonAdministratedWorkspaces: workspaces ?  workspaces.nonAdministratedWorkspaces: null,
                i18n: App.config.i18n,
                userName: App.config.userName,
                isDocumentManagement: window.location.pathname.match('/document-management/'),
                isProductManagement: window.location.pathname.match('/product-management/'),
                isProductStructure: window.location.pathname.match('/product-structure/'),
                isChangeManagement: window.location.pathname.match('/change-management/'),
                isWorkspaceManagement: window.location.pathname.match('/workspace-management/'),
                isAdmin:App.config.admin
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
        },

        logout:function(){
            delete localStorage.jwt;
            $.get(App.config.contextPath + '/api/auth/logout').complete(function () {
                window.location.href = App.config.contextPath + '/?logout=true';
            });
        }

    });

    return HeaderView;
});
