/*global define*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baseline_list_item.html',
    'views/baselines/baseline_detail_view'
], function (Backbone, Mustache, template,BaselineDetailView) {
    'use strict';
    var BaselineItemView = Backbone.View.extend({
        tagName: 'li',

        className: 'baseline-item',

        events: {
            'change input[type=checkbox]': 'toggleStroke',
            'click a': 'toBaselineDetailView'
        },

        render: function () {
            this.$el.html(Mustache.render(template, {model: this.model}));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.$a = this.$('a');
            this.$checkbox = this.$('input[type=checkbox]');
        },

        toggleStroke: function () {
            this.$a.toggleClass('stroke');
        },

        isChecked: function () {
            return this.$checkbox.is(':checked');
        },

        toBaselineDetailView: function () {
            setTimeout(this.openBaselineDetailView.bind(this),500);
            this.$el.trigger('close-modal-request');
        },

        openBaselineDetailView :function(){
            var model = this.model;
            model.fetch().success(function(){
                new BaselineDetailView({model: model}, {productId: model.getConfigurationItemId()}).render();
            });
        }

    });

    return BaselineItemView;
});
