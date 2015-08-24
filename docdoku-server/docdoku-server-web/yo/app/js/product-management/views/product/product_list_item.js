/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'text!templates/product/product_list_item.html',
    'views/product/product_details_view',
    'common-objects/views/part/part_modal_view',
    'common-objects/models/part'
], function (Backbone, Mustache, template, ProductDetailsView, PartModalView, Part) {
    'use strict';
    var ProductListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.product_id': 'openDetailsView',
            'click a.design_item': 'openPartView'
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
            this.trigger('rendered', this);

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

            this.$('.author-popover').userPopover(this.model.getAuthorLogin(), this.model.getId(), 'left');

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

        openDetailsView: function () {
            var that = this;
            var pdv = new ProductDetailsView({model: that.model});
            pdv.on('pathToPathLink:remove', function() {
                that.syncProduct();
            });
            window.document.body.appendChild(pdv.render().el);
            pdv.openModal();
        },

        syncProduct: function() {
            var that = this;
            this.model.fetch().success(function() {
                that.render();
            }).error(function() {
            });
        },

        openPartView:function(){
            var part = new Part({partKey:this.model.getDesignItemNumber() + '-' +this.model.getDesignItemLatestVersion()});

            part.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: part
                });
                partModalView.show();
            });
        }

    });

    return ProductListItemView;
});
