/*global define,App*/
define([
    'backbone',
    'mustache',
    'moment',
    'common-objects/models/effectivity',
    'text!common-objects/templates/part/part_effectivity.html'
], function (Backbone, Mustache, moment, Effectivity, template) {

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

            var effectivityType = this.Effectivity.getEffectivityTypeById(this.effectivity.typeEffectivity);
            var productKey = this.effectivity.configurationItemKey;
            var productId = productKey ? productKey.id : null;
            var start, end;

            switch(this.effectivity.typeEffectivity) {
                case this.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                    start = this.effectivity.startNumber;
                    end = this.effectivity.endNumber;
                    break;
                case this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                    start = moment(this.effectivity.startDate).format('YYYY-MM-DD');
                    end = this.effectivity.endDate ? moment(this.effectivity.endDate).format('YYYY-MM-DD') : null;
                    break;
                case this.Effectivity.getEffectivityTypeById('LOTBASEDEFFECTIVITY').id:
                    start = this.effectivity.startLotId;
                    end = this.effectivity.endLotId;
                    break;
            }

            this.$el.append(Mustache.render(template, {
                i18n: App.config.i18n,
                workspaceId: App.config.workspaceId,
                effectivityId: this.effectivity.id,
                name: this.effectivity.name,
                product: productId,
                description: this.effectivity.description,
                type: App.config.i18n[effectivityType.name],
                start: start,
                end: end
            }));

            this.$effectivity = this.$('#effectivity-' + this.effectivity.id);
            this.$btnUpdate = this.$effectivity.find('.effectivity-update-link');
            this.$btnDelete = this.$effectivity.find('.effectivity-remove-link');
            return this;
        }

    });

    return PartEffectivityView;

});
