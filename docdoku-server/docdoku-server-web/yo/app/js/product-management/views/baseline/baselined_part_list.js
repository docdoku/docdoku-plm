/*global define*/
'use strict';
define([
    'backbone',
    "mustache",
    "text!templates/baseline/baselined_part_list.html",
    "views/baseline/baselined_part_list_item",
    "common-objects/models/baselined_part"
], function (Backbone, Mustache, template, BaselinedPartListItemView, BaselinedPart) {

    var BaselinedPartsView = Backbone.View.extend({

        tagName: 'div',

        className: 'baselined-parts-list',

        removeSubviews: function () {
            _(this.baselinedPartsViews).invoke('remove');
        },

        initialize: function () {
            _.bindAll(this);
            this.isForBaseline = (this.options.isForBaseline) ? this.options.isForBaseline : false;
            this.isLocked = (this.options.isLocked) ? this.options.isLocked : false;
            this.$el.on('remove', this.removeSubviews());
            this.initPartsList();
        },

        render: function () {
            var _this = this;
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.resetList();
            this.partReferenceInput.on('input', function () {
                _this.resetList();
            });
            return this;
        },

        bindDomElements: function () {
            this.partReferenceInput = this.$('.baselined-parts-reference-typehead');
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
            var _this = this;
            if (this.model) {
                if (this.partReferenceInput.val()) {
                    this.model.getBaselinePartsWithReference(this.partReferenceInput.val(), {
                        success: _this.updateList
                    });
                } else {
                    this.updateList(this.model.getBaselinedParts());
                }
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
                    var data = {
                        model: baselinedPart
                    };
                    if (_this.isForBaseline) {
                        data.released = _this.model.isReleased();
                        data.isForBaseline = _this.isForBaseline;
                    } else {
                        data.isLocked = _this.isLocked;
                    }

                    var baselinedPartItemView = new BaselinedPartListItemView(data).render();
                    _this.baselinedPartsViews.push(baselinedPartItemView);
                    _this.partsUL.append(baselinedPartItemView.$el);
                }
            });
        },

        getBaselinedParts: function () {
            var baselinedParts = [];
            _.each(this.baselinedParts, function (baselinedPart) {
                if (!baselinedPart.isExcluded()) {
                    baselinedParts.push({
                        number: baselinedPart.getNumber(),
                        version: baselinedPart.getVersion(),
                        iteration: parseInt(baselinedPart.getIteration(), 10)
                    });
                }
            });
            return baselinedParts;
        }
    });

    return BaselinedPartsView;
});