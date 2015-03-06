/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baselined_part_list.html',
    'views/baselines/baselined_part_list_item',
    'models/baselined_part'
], function (Backbone, Mustache, template, BaselinedPartListItemView, BaselinedPart) {
	'use strict';
    var BaselinedPartsView = Backbone.View.extend({

        tagName: 'div',

        className: 'baselined-parts-list',

        removeSubviews: function () {
            _(this.baselinedPartsViews).invoke('remove');
        },

        initialize: function () {
            _.bindAll(this);
            this.editMode = (this.options.editMode) ? this.options.editMode : false;
            this.$el.on('remove', this.removeSubviews());
            this.initPartsList();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.resetList();
            return this;
        },

        bindDomElements: function () {
            this.partsUL = this.$('.baselined-parts');
        },

        initPartsList: function () {
            var _this = this;
            var collection = this.model.getBaselinedParts();
            this.baselinedParts = [];
            _.each(collection, function (bpData) {
                var baselinedPart = new BaselinedPart(bpData);
                _this.baselinedParts.push(baselinedPart);
            });
        },

        resetList: function () {
            if (this.model) {
                this.updateList(this.model.getBaselinedParts());
            }
        },

        updateList: function (collection) {
            var _this = this;
            this.removeSubviews();

            this.baselinedPartsViews = [];
            _.each(collection, function (bpData) {
                var baselinedPart = _.find(_this.baselinedParts, function (bp) {
                    return bp.getNumber() === bpData.number;
                });
                if (baselinedPart) {
                    var baselinedPartItemView = new BaselinedPartListItemView({
                        model:baselinedPart,
                        editMode:_this.editMode
                    }).render();
                    baselinedPartItemView.on('part-modal:open',_this.trigger.bind(_this));
                    _this.baselinedPartsViews.push(baselinedPartItemView);
                    _this.partsUL.append(baselinedPartItemView.$el);
                }
            });
        },

        getBaselinedParts: function () {
            var baselinedParts = [];
            _.each(this.baselinedParts, function (baselinedPart) {
                baselinedParts.push({
                    number: baselinedPart.getNumber(),
                    version: baselinedPart.getVersion(),
                    iteration: parseInt(baselinedPart.getIteration(), 10)
                });
            });
            return baselinedParts;
        }
    });

    return BaselinedPartsView;
});
