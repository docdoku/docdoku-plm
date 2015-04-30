/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/baselines_list_item.html',
    'views/baselines/baseline_detail_view',
    'views/product/product_details_view',
    'models/configuration_item'
], function (Backbone, Mustache, template, BaselineDetailView, ProductDetailsView, ConfigurationItem) {
    'use strict';
    var BaselineListItemView = Backbone.View.extend({
        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'openDetailView',
            'click a.product_id':'openProductDetailView'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                bomUrl: this.model.getBomUrl(),
                sceneUrl:this.model.getSceneUrl(),
                zipUrl: this.model.getZipUrl(),
                i18n: App.config.i18n
            }));
            this.$checkbox = this.$('input[type=checkbox]');
            this.bindUserPopover();
            this.trigger('rendered', this);
            return this;
        },

        selectionChanged: function () {
            this._isChecked = this.$checkbox.prop('checked');
            this.trigger('selectionChanged', this);
        },

        isChecked: function () {
            return this._isChecked;
        },

        check: function () {
            this.$checkbox.prop('checked', true);
            this._isChecked = true;
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        openDetailView: function () {
            new BaselineDetailView({model: this.model, isForBaseline: true}).render();
        },

        openProductDetailView:function(e){
            var model = new ConfigurationItem();
            model.set('_id',this.model.getConfigurationItemId());
            model.fetch().success(function(){
                var view = new ProductDetailsView({model:model});
                window.document.body.appendChild(view.render().el);
                view.openModal();
            });
        },

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), App.config.i18n.BASELINE, 'left');
        }
    });

    return BaselineListItemView;
});
