/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/effectivity',
    'text!common-objects/templates/part/part_effectivity.html'
], function (Backbone, Mustache, Effectivity, template) {

    'use strict';

    var PartEffectivityView = Backbone.View.extend({

        initialize: function () {
            this.Effectivity = new Effectivity();
            this.effectivity = this.options.effectivity;
            this.model = this.options.model;
        },

        getIdSelector: function() {
            return '#effectivity-' + this.effectivity.id;
        },

        render:function(){
            var context = this;

            var effectivityType = context.Effectivity.getEffectivityTypeById(context.effectivity.typeEffectivity);
            var start, end, productId;

            switch(context.effectivity.typeEffectivity) {
                case context.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                    start = context.effectivity.startNumber;
                    end = context.effectivity.endNumber;
                    productId = context.effectivity.configurationItemKey.id;
                    break;
                case context.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                    start = moment(context.effectivity.startDate).format('YYYY-MM-DD');
                    end = moment(context.effectivity.endDate).format('YYYY-MM-DD');
                    productId = null;
                    break;
                case context.Effectivity.getEffectivityTypeById('LOTBASEDEFFECTIVITY').id:
                    start = context.effectivity.startLotId;
                    end = context.effectivity.endLotId;
                    productId = context.effectivity.configurationItemKey.id;
                    break;
            }

            this.$el.append(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId: App.config.workspaceId,
                effectivityId: context.effectivity.id,
                name: context.effectivity.name,
                product: productId,
                description: context.effectivity.description,
                type: App.config.i18n[effectivityType.name],
                start: start,
                end: end
            }));

            this.$effectivity = this.$('#effectivity-' + context.effectivity.id);
            this.$btnUpdate = this.$effectivity.find('.effectivity-update-link');
            this.$btnDelete = this.$effectivity.find('.effectivity-remove-link');
            return this;
        }

    });

    return PartEffectivityView;

});
