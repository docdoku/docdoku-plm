/*global define,App,$*/
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
            'click a.product_id':'openProductDetailView',
            'click td.has-path-to-path-link':'openDetailViewOnPathToPathLinkTab'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
        },

        render: function () {

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                model: this.model,
                bomUrl: this.model.getBomUrl(),
                sceneUrl:this.model.getSceneUrl(),
                zipUrl: this.model.getZipUrl()
            }));
            this.$checkbox = this.$('input[type=checkbox]');
            this.bindUserPopover();
            this.trigger('rendered', this);
            //LINKS

            var zipUrl = this.model.getZipUrl();
            this.$('.download-zip').popover({
                title: '<b>'+App.config.i18n.DOWNLOAD_ZIP+'<br/>',
                animation: true,
                html: true,
                trigger: 'manual',
                content: '<a href="'+(zipUrl+ '&exportNativeCADFiles=true&exportDocumentLinks=false')+'">'+App.config.i18n.CAD_FILE+'</a> | ' +
                '<a href="'+(zipUrl+ '&exportNativeCADFiles=false&exportDocumentLinks=true')+'">'+App.config.i18n.LINKS+'</a> | ' +
                '<a href="'+(zipUrl+ '&exportNativeCADFiles=true&exportDocumentLinks=true')+'">'+App.config.i18n.EVERYTHING+'</a>',
                placement: 'top'
            }).click(function (e) {
                $(this).popover('show');
                e.stopPropagation();
                e.preventDefault();
                return false;
            });

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
            var model = this.model;
            model.fetch().success(function () {
                new BaselineDetailView({model: model}).render();
            });
        },

        openDetailViewOnPathToPathLinkTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var baselineView = new BaselineDetailView({model: model}).render();
                baselineView.activePathToPathLinkTab();
            }.bind(this));

        },

        openProductDetailView:function(){
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
