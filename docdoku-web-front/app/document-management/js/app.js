/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/workspace',
    'text!templates/content.html'
], function (Backbone, Mustache, Workspace, template) {
	'use strict';
    var AppView = Backbone.View.extend({

        el: '#content',

        initialize: function () {
            this.model = new Workspace({id: App.config.workspaceId});
        },

        render: function () {

            this.$el.html(Mustache.render(template, {model: this.model, i18n: App.config.i18n}));
            App.$documentManagementMenu = this.$('#document-management-menu');
            App.$documentManagementContent = this.$('#document-management-content');

            this.bindDomElements();

            App.$documentManagementMenu.customResizable({
                containment: this.$el
            });
            this.$el.show();
            return this;
        },

        showBaselineTooltip : function(){
            this.$foldersNav.popover('destroy');
            this.$foldersNav.popover({
                html: true,
                title:'',
                content: '<b>'+App.config.i18n.DOCUMENTS_CHOICE+'</b> <br />' + '<span>'+App.config.i18n.DOCUMENTS_CHOICE_EXPLANATION+'</span>',
                container: 'body',
                placement: 'right'
            }).popover('show');
        },

        bindDomElements:function(){
            this.$linksNav = this.$('.nav-header.links-nav');
            this.$foldersNav = this.$('#folder-nav');
        }
    });

    return AppView;
});
