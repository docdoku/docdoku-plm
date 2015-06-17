/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/linked/linked_change_item_collection',
    'common-objects/views/linked/linked_change_item',
    'text!common-objects/templates/linked/linked_change_items.html',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/collections/linked/linked_part_collection'
], function (Backbone, Mustache, LinkedChangeItemCollection, LinkedChangeItemView, template, LinkedDocumentCollection, LinkedPartCollection) {

    'use strict';

    var LinkedRequestsView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-items-view',

        initialize: function () {
            this.searchResults = [];
            this._subViews = [];
            this.options.linkedPartsView = (this.options.linkedPartsView) ? this.options.linkedPartsView : null;
            this.options.linkedDocumentsView = (this.options.linkedDocumentsView) ? this.options.linkedDocumentsView : null;
            var self = this;
            this.$el.on('remove', function () {
                _(self._subViews).invoke('remove');
            });
        },

        render: function () {
            var self = this;

            this.$el.html(Mustache.render(template,{
                i18n: App.config.i18n,
                editMode: this.options.editMode,
                label: this.options.label,
                view: this
            }));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function (linkedChangeItem) {
                self.initialAddLinkView(linkedChangeItem);
            });

            return this;
        },

        bindDomElements: function () {
            this.referenceInput = this.$('.linked-items-reference-typehead');
            this.linksUL = this.$('#linked-items-' + this.cid);
        },

        initialAddLinkView: function (linkedChangeItem) {
            var linkView = new LinkedChangeItemView({
                editMode: this.options.editMode,
                model: linkedChangeItem
            }).render();
            this._subViews.push(linkView);
            this.linksUL.append(linkView.el);
        },

        addLinkView: function (linkedChangeItem) {
            var alreadyExist = false;
            _.each(this._subViews, function (view) {
                if (view.model.getId() === linkedChangeItem.getId()) {
                    alreadyExist = true;
                }
            });
            if (!alreadyExist) {
                this.initialAddLinkView(linkedChangeItem);

                var self = this;
                var affectedDocuments = linkedChangeItem.getAffectedDocuments();

                var affectedDocumentsCollection = new LinkedDocumentCollection(affectedDocuments);
                affectedDocumentsCollection.each(function (linkedDocument) {
                    self.options.linkedDocumentsView.collection.add(linkedDocument);
                    self.options.linkedDocumentsView.addLinkView(linkedDocument);
                });


                var affectedParts = linkedChangeItem.getAffectedParts();

                var affectedPartsCollection = new LinkedPartCollection(affectedParts);
                affectedPartsCollection.each(function (linkedPart) {
                    self.options.linkedPartsView.collection.add(linkedPart);
                    self.options.linkedPartsView.addLinkView(linkedPart);
                });

            }
        }
    });
    return LinkedRequestsView;
});
