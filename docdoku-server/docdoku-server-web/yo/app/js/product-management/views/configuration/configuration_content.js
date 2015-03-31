/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/configuration/configuration_content.html',
    'collections/configurations',
    'views/configuration/configuration_list',
    'common-objects/views/alert',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/new_configuration_button.html'
], function (Backbone, Mustache, template, ConfigurationCollection, ConfigurationListView, AlertView, deleteButton, newConfigurationButton) {
    'use strict';
	var ConfigurationContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton,
            newConfigurationButton: newConfigurationButton
        },

        events: {
            'click button.new-configuration': 'newConfiguration',
            'click button.delete': 'deleteConfiguration'
        },

        initialize: function () {
            _.bindAll(this);
        },
        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

            if(!this.configurationCollection){
                this.configurationCollection = new ConfigurationCollection();
            }

            if(this.listView){
                this.listView.remove();
            }

            this.listView = new ConfigurationListView({
                el: this.$('#configuration_table'),
                collection: this.configurationCollection
            }).render();

            this.bindEvent();

            return this;
        },
        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
        },
        bindEvent: function(){
            this.delegateEvents();
            this.listView.on('error', this.onError);
            this.listView.on('warning', this.onWarning);
            this.listView.on('info', this.onInfo);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
        },

        deleteConfiguration: function () {
            this.listView.deleteSelectedConfigurations();
        },

        changeDeleteButtonDisplay: function (state) {
            this.deleteButton.toggle(state);
        },

        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        onWarning:function(model, error){
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'warning',
                message: errorMessage
            }).render().$el);
        },

        onInfo:function(message){
            this.$notifications.append(new AlertView({
                type: 'info',
                message: message
            }).render().$el);
        }

    });
    return ConfigurationContentView;
});
