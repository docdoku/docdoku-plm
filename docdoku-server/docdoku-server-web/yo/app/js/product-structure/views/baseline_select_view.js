/*global define,APP_CONFIG*/
'use strict';
define([
    'backbone',
    "mustache",
    'common-objects/collections/baselines',
    'text!templates/baseline_select.html'
], function (Backbone, Mustache, Baselines, template) {

    var BaselineSelectView = Backbone.View.extend({

        el: '#config_spec_container',

        events: {
            'change select': 'onSelectorChanged'
        },

        initialize: function () {
            this.collection = new Baselines({}, {productId: APP_CONFIG.productId});
            this.listenToOnce(this.collection, 'reset', this.onCollectionReset);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
            this.bindDomElements();
            this.collection.fetch({reset: true});
            return this;
        },

        bindDomElements: function () {
            this.$select = this.$('select');
        },

        onCollectionReset: function () {
            var that = this;
            this.collection.each(function (baseline) {
                that.$select.append('<option value="' + baseline.getId() + '">' + baseline.getName() + '</option>');
            });
        },

        onSelectorChanged: function (e) {
            this.trigger('config_spec:changed', e.target.value);
        }

    });

    return BaselineSelectView;
});