/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/baselines/document_revision_list_item.html',
    'common-objects/models/document/document_revision'
], function (Backbone, Mustache, template, DocumentRevision) {
    'use strict';
    var DocumentRevisionListItemView = Backbone.View.extend({

        events: {
            'click .delete-document': 'remove',
            'click a.reference': 'toDocumentDetailView'
        },

        template: Mustache.parse(template),

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model,
                i18n: App.config.i18n,
                editMode: this.options.editMode,
                multiple: this.options.multiple
            }));

            return this;
        },

        remove: function() {
            this.trigger('remove', this);
        },

        toDocumentDetailView: function () {
            setTimeout(this.openDocumentDetailView, 500);
            this.$el.trigger('close-modal-request');
        },

        openDocumentDetailView: function () {
            var documentRevision = new DocumentRevision({
                id: this.model.documentMasterId + '-' + this.model.version
            });

            var self = this;

            documentRevision.fetch().success(function () {
                require(['common-objects/views/document/document_iteration'], function (IterationView) {
                    var view = new IterationView({
                        model: documentRevision,
                        iteration: self.model.iteration
                    });
                    view.show();
                });

            });
        }
    });

    return DocumentRevisionListItemView;
});
