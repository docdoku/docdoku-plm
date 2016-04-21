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

        initialize: function () {
            _.bindAll(this);
            this.editMode = (this.options.editMode) ? this.options.editMode : false;
            this.$el.on('remove', this.removeSubviews());
            this.baselinedParts = [];
            this.baselinedPartsViews = [];
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements: function () {
            this.partsUL = this.$('.baselined-parts');
        },

        renderList: function () {

            this.clear();

            var collection = this.model.getBaselinedParts();

            this.baselinedParts = [];
            this.baselinedPartsViews = [];

            _.each(collection, function (bpData) {

                var baselinedPart = new BaselinedPart(bpData);

                var baselinedPartItemView = new BaselinedPartListItemView({
                    model:baselinedPart,
                    editMode:this.editMode
                }).render();

                this.baselinedParts.push(baselinedPart);
                this.baselinedPartsViews.push(baselinedPartItemView);

                this.partsUL.append(baselinedPartItemView.$el);

            },this);
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
        },

        clear:function(){
            this.baselinedParts = [];
            this.baselinedPartsViews = [];
            this.removeSubviews();
            this.partsUL.empty();
        },

        removeSubviews: function () {
            _(this.baselinedPartsViews).invoke('remove');
        }

    });

    return BaselinedPartsView;
});
