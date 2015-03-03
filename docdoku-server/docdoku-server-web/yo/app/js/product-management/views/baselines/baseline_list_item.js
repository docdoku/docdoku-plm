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
            this.trigger('open-baseline-modal');
            setTimeout(function(){
                new BaselineDetailView({model: this.model}, {productId: this.model.getConfigurationItemId()}).render();
            }.bind(this),200)
        }

    });

    return BaselineItemView;
});
