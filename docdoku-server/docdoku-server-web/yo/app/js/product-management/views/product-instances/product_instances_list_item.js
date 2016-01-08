/*global define,App,$*/
define([
    'backbone',
    'mustache',
    'text!templates/product-instances/product_instances_list_item.html',
    'views/product-instances/product_instance_modal',
    'common-objects/utils/date'
], function (Backbone, Mustache, template, ProductInstanceModalView, date) {
    'use strict';
    var ProductInstancesListItemView = Backbone.View.extend({

        events: {
            'click input[type=checkbox]': 'selectionChanged',
            'click td.reference': 'openEditView',
            'click td.has-path-to-path-link i.fa-exchange': 'openEditViewOnPathToPathLinkTab',
            'click td.has-path-data i.fa-asterisk': 'openEditViewOnPathDataTab',
            'click td.has-attached-files i': 'openEditViewOnFilesTab'
        },

        tagName: 'tr',

        initialize: function () {
            this._isChecked = false;
            this.listenTo(this.model,'change',this.render.bind(this));
            this.listenTo(this.model,'sync',this.render.bind(this));

        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n,
                bomUrl: this.model.getBomUrl(),
                sceneUrl:this.model.getSceneUrl(),
                zipUrl: this.model.getZipUrl(),
                isReadOnly: this.model.isReadOnly(),
                isFullAccess: this.model.isFullAccess()
            }));

            this.$checkbox = this.$('input[type=checkbox]');
            if (this.isChecked()) {
                this.check();
                this.trigger('selectionChanged', this);
            }

            this.bindUserPopover();
            date.dateHelper(this.$('.date-popover'));
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

        bindUserPopover: function () {
            this.$('.author-popover').userPopover(this.model.getUpdateAuthor(), this.model.getSerialNumber(), 'left');
        },

        unCheck: function () {
            this.$checkbox.prop('checked', false);
            this._isChecked = false;
        },

        openEditView: function () {
            var model = this.model;
            model.fetch().success(function () {
                var view = new ProductInstanceModalView({model: model});
                view.render();
                window.document.body.appendChild(view.el);
            });
        },

        openEditViewOnPathToPathLinkTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var view = new ProductInstanceModalView({model: model});
                window.document.body.appendChild(view.el);
                view.render();
                view.activePathToPathLinkTab();

            }.bind(this));
        },

        openEditViewOnPathDataTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var view = new ProductInstanceModalView({model: model});
                window.document.body.appendChild(view.el);
                view.render();
                view.activePathDataTab();

            }.bind(this));
        },

        openEditViewOnFilesTab: function () {
            var model = this.model;
            model.fetch().success(function () {
                var view = new ProductInstanceModalView({model: model});
                window.document.body.appendChild(view.el);
                view.render();
                view.activeFilesTab();

            }.bind(this));
        }

    });

    return ProductInstancesListItemView;
});
