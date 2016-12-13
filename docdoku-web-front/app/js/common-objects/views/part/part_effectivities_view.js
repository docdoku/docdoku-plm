/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/effectivity',
    'text!common-objects/templates/part/part_effectivities.html',
    'common-objects/views/part/part_effectivity_view',
    'common-objects/views/part/part_update_effectivity_view',
    'common-objects/views/alert'
], function (Backbone, Mustache, Effectivity, template, PartEffectivityView, PartUpdateEffectivityView, AlertView) {

    'use strict';

    var PartEffectivitiesView = Backbone.View.extend({

        events: {
        },

        initialize: function () {
            this.model.effectivities = [];
            this.Effectivity = new Effectivity();
            this.selectedPartUsageLinkView = null;
            this.effectivities = null;
            this.effectivityViews = [];
            this.productId = this.options.productId;
        },

        render:function(){
            var self = this;
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n, editMode: this.options.editMode}));

            this.$effectivities = this.$('.effectivities');
            this.$notifications = this.options.notifications;

            this.model.getEffectivities().then(function(data) {
                self.model.effectivities = data;
                self.effectivities = data;
                self.onSuccessfulLoad();
            });

            return this;
        },

        onSuccessfulLoad: function() {
            var self = this;
            this.effectivityViews = [];
            this.$effectivities.empty();
            _(this.effectivities).each(function(effectivity) {
                var partEffectivityView = new PartEffectivityView({
                    el: '.effectivities',
                    effectivity: effectivity,
                    model: self.model
                }).render();
                partEffectivityView.$btnUpdate.click(function(e) {
                    self.onButtonUpdate(partEffectivityView);
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                });
                partEffectivityView.$btnDelete.click(function(e) {
                    self.onButtonDelete(partEffectivityView);
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                });
                self.effectivityViews.push(partEffectivityView);
            });
        },

        onButtonUpdate: function(partEffectivityView) {
            var self = this;
            this.partUpdateEffectivityView = new PartUpdateEffectivityView({
                model: this.model,
                productId: this.productId,
                effectivity: partEffectivityView.options.effectivity,
                updateCallback: function() {
                    self.render();
                }
            }).render();
            this.partUpdateEffectivityView.openModal();
        },

        onButtonDelete: function(partEffectivityView) {
            var self = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_PART_EFFECTIVITY,
                App.config.i18n.CANCEL,
                App.config.i18n.CONFIRM,
                function (result) {
                    if (result) {
                        self.options.model.deleteEffectivity(partEffectivityView.effectivity.id).then(function() {
                            self.effectivityViews = _.without(self.effectivityViews, partEffectivityView);
                            self.$(partEffectivityView.getIdSelector()).remove();
                            self.onSuccessNotification(self.options.model, App.config.i18n.PART_EFFECTIVITY_DELETE_SUCCESS);
                        }, function(error) {
                            self.onError(self.options.model, error);
                        });
                    }
                });
        },

        onSuccessNotification: function(model, success) {
            var successMessage = success ? success : model;

            this.$notifications.append(new AlertView({
                type: 'success',
                message: successMessage
            }).render().$el);
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

    });

    return PartEffectivitiesView;

});
