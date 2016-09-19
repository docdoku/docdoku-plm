/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/baselines',
    'text!templates/baselines/baseline_content.html',
    'views/baselines/baseline_list',
    'text!common-objects/templates/buttons/delete_baseline_button.html',
    'text!common-objects/templates/buttons/snap_button.html',
    'common-objects/views/alert',
    'views/baselines/baseline_creation_view'
], function (Backbone, Mustache, BaselinesCollection, template, BaselinesListView, deleteButton, snapButton, AlertView, BaselineCreationView) {
    'use strict';

    var BaselineContentView = Backbone.View.extend({
        partials: {
            deleteButton: deleteButton,
            snapButton:snapButton
        },

        events: {
            'click button.delete-baseline': 'deleteBaseline',
            'click button.new-baseline': 'createBaseline'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();
            this.createBaselineButton.show();

            this.bindEvent();
            this.createBaselineView();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete-baseline');
            this.createBaselineButton = this.$('.new-baseline');
        },


        // TODO: determine if this is still useful
        bindEvent: function(){
            this.delegateEvents();
        },

        createBaseline: function () {
            var baselineCreationView = new BaselineCreationView();
            window.document.body.appendChild(baselineCreationView.render().el);
            baselineCreationView.on('warning', this.onWarning);
            baselineCreationView.openModal();
        },

        createBaselineView: function () {
            if (this.listView) {
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
            }

            this.listView = new BaselinesListView({
                collection: new BaselinesCollection({}, {type:'document'})
            }).render();

            this.$el.append(this.listView.el);
            this.listView.on('error', this.onError);
            this.listView.on('warning', this.onWarning);
            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
        },

        deleteBaseline: function () {
            this.listView.deleteSelectedBaselines();
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

    return BaselineContentView;
});
