/*global define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/models/effectivity',
    'text!common-objects/templates/part/part_effectivities.html',
    'text!common-objects/templates/part/part_effectivity.html'
], function (Backbone, Mustache, Effectivity, template, partialEffectivity) {

    'use strict';

    var PartEffectivityView = Backbone.View.extend({

        initialize: function () {
            var context = this;
            this.Effectivity = new Effectivity();
            this.selectedPartUsageLinkView = null;
            this.effectivities = null;

            this.model.getEffectivities().then(function(data) {
                context.effectivities = data;
                context.onSuccess();
            });
        },

        render:function(){
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n, editMode: this.options.editMode, effectivities:this.effectivities}));

            this.$components = this.$('.components');
            this.$effectivities = this.$('.effectivities');

            return this;
        },

        onSuccess: function() {
            var context = this;
            this.$effectivities.empty();
            _(this.effectivities).each(function(effectivity) {
                var effectivityType = context.Effectivity.getEffectivityTypeById(effectivity.typeEffectivity);
                var start, end, productId;

                switch(effectivity.typeEffectivity) {
                    case context.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                        start = effectivity.startNumber;
                        end = effectivity.endNumber;
                        productId = effectivity.configurationItemKey.id;
                        break;
                    case context.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                        start = moment(effectivity.startDate).format('YYYY-MM-DD');
                        end = moment(effectivity.endDate).format('YYYY-MM-DD');
                        productId = null;
                        break;
                    case context.Effectivity.getEffectivityTypeById('LOTBASEDEFFECTIVITY').id:
                        start = effectivity.startLotId;
                        end = effectivity.endLotId;
                        productId = effectivity.configurationItemKey.id;
                        break;
                }

                context.$effectivities.append(Mustache.render(partialEffectivity, {
                    i18n: App.config.i18n,
                    name: effectivity.name,
                    product: productId,
                    description: effectivity.description,
                    type: App.config.i18n[effectivityType.name],
                    start: start,
                    end: end
                }));
            });
        }

    });

    return PartEffectivityView;

});
