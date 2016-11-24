/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/effectivity',
    'text!common-objects/templates/part/part_effectivities.html',
    'common-objects/views/part/part_effectivity_view',
    'views/part/part_update_effectivity_view',
    'common-objects/views/alert'
], function (Backbone, Mustache, Effectivity, template, PartEffectivityView, PartUpdateEffectivityView, AlertView) {

    'use strict';

    var PartEffectivitiesView = Backbone.View.extend({

        initialize: function () {
            this.model.effectivities = [];
            this.Effectivity = new Effectivity();
            this.selectedPartUsageLinkView = null;
            this.effectivities = null;
            this.effectivityViews = [];
            this.productId = this.options.productId;
        },

        render:function(){
            console.log('render');
            var context = this;
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n, editMode: this.options.editMode}));

            this.$components = this.$('.components');
            this.$effectivities = this.$('.effectivities');
            this.$notifications = this.options.notifications;

            this.model.getEffectivities().then(function(data) {
                context.model.effectivities = data;
                context.effectivities = data;
                context.onSuccessfulLoad();
            });

            return this;
        },

        onSuccessfulLoad: function() {
            var context = this;
            this.effectivityViews = [];
            this.$effectivities.empty();
            _(this.effectivities).each(function(effectivity) {
                var partEffectivityView = new PartEffectivityView({
                    el: '.effectivities',
                    effectivity: effectivity,
                    model: context.model
                }).render();
                partEffectivityView.$btnUpdate.click(function(e) {
                    context.onButtonUpdate(partEffectivityView);
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                });
                partEffectivityView.$btnDelete.click(function(e) {
                    context.onButtonDelete(partEffectivityView);
                    e.preventDefault();
                    e.stopPropagation();
                    return false;
                });
                context.effectivityViews.push(partEffectivityView);
            });
        },

        onButtonUpdate: function(partEffectivityView) {
            this.partUpdateEffectivityView = new PartUpdateEffectivityView({
                model: this.model,
                productId: this.productId,
                effectivity: partEffectivityView.options.effectivity
            }).render();
            this.partUpdateEffectivityView.openModal();
            // TODO : See also common-objects/views/part/part_update_effectivity_view
            // When update, has to update effectivities of the current context to render the changes
        },

        onButtonDelete: function(partEffectivityView) {
            var context = this;
            bootbox.confirm(App.config.i18n.CONFIRM_DELETE_PART_EFFECTIVITY,
                App.config.i18n.CANCEL,
                App.config.i18n.CONFIRM,
                function (result) {
                    if (result) {
                        context.options.model.deleteEffectivity(partEffectivityView.effectivity.id).then(function() {
                            context.effectivityViews = _.without(context.effectivityViews, partEffectivityView);
                            context.$(partEffectivityView.getIdSelector()).remove();
                            context.onSuccessNotification(context.options.model, App.config.i18n.PART_EFFECTIVITY_DELETE_SUCCESS);
                        }, function(error) {
                            context.onError(context.options.model, error);
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
