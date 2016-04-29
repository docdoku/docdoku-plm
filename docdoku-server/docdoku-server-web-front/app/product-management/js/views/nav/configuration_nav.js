/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/common/singleton_decorator',
    'text!templates/nav/configuration_nav.html',
    'views/configuration/configuration_content'
], function (Backbone, Mustache, singletonDecorator, template, ConfigurationContentView) {
    'use strict';
    var ConfigurationNavView = Backbone.View.extend({
        el: '#configuration-nav',

        initialize: function () {
            this.render();
            this.configurationContentView = undefined;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, workspaceId: App.config.workspaceId}));
        },

        setActive: function () {
            if (App.$productManagementMenu) {
                App.$productManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.nav-list-entry').first().addClass('active');
        },

        showContent: function () {
            this.setActive();
			this.cleanView();
            if(!this.configurationContentView){
                this.configurationContentView = new ConfigurationContentView();
            }
            this.configurationContentView.render();
            App.$productManagementContent.html(this.configurationContentView.el);
        },

        cleanView: function () {
            if (this.configurationContentView) {
                this.configurationContentView.undelegateEvents();
                App.$productManagementContent.html('');
            }
        }

    });

    ConfigurationNavView = singletonDecorator(ConfigurationNavView);
    return ConfigurationNavView;

});
