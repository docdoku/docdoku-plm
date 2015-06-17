/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/linked/linked_document_collection',
    'common-objects/views/linked/linked_document',
    'text!common-objects/templates/linked/linked_items.html'
], function (Backbone, Mustache, LinkedDocumentCollection, LinkedDocumentView, template) {
    'use strict';
    var LinkedDocumentsView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-items-view',

        initialize: function () {
            this.searchResults = [];
            this._subViews = [];
            var self = this;
            this.$el.on('remove', function () {
                _(self._subViews).invoke('remove');
            });
        },

        render: function () {
            var self = this;

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                editMode: this.options.editMode,
                commentEditable: this.options.commentEditable,
                label: App.config.i18n.ADD_DOCUMENT,
                view: this
            }));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function (linkedDocument) {
                self.addLinkView(linkedDocument);
            });

            return this;
        },

        bindDomElements: function () {
            this.documentReferenceInput = this.$('.linked-items-reference-typehead');
            this.linksUL = this.$('#linked-items-' + this.cid);
        },

        addLinkView: function (linkedDocument) {
            var linkView = new LinkedDocumentView({
                editMode: this.options.editMode,
                commentEditable: this.options.commentEditable,
                model: linkedDocument
            }).render();

            this._subViews.push(linkView);
            this.linksUL.append(linkView.el);
        },

        bindTypeahead: function () {
            var self = this;
            var itemsLimit = 15;

            this.documentReferenceInput.typeahead({
                items: itemsLimit,

                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/documents/doc_revs?q=' + query + '&l=' + itemsLimit, function (data) {

                        self.searchResults = new LinkedDocumentCollection(data);

                        if (self.options.documentIteration && self.options.documentIteration.className === 'DocumentIteration') {
                            data = _.reject(self.searchResults.models, function (linkedDocument) {
                                return linkedDocument.getDocKey() === self.options.documentIteration.getDocKey();
                            }, self);
                            self.searchResults = new LinkedDocumentCollection(data);
                        }

                        // Remove documents that are already linked
                        var docsToRemove = [];
                        self.searchResults.each(
                            function (searchLinkedDocument) {
                                var linkedDoc = self.collection.find(
                                    function (addedLinkedDocument) {
                                        return addedLinkedDocument.getDocKey() === searchLinkedDocument.getDocKey();
                                    });
                                if (!_.isUndefined(linkedDoc)) {
                                    docsToRemove.push(searchLinkedDocument);
                                }
                            }
                        );
                        self.searchResults.remove(docsToRemove);

                        process(self.searchResults.map(function (docLastIter) {
                            return docLastIter.getDisplayDocKey();
                        }));
                    });
                },

                sorter: function (docsLastIterDocKey) {
                    return docsLastIterDocKey.sort();
                },

                updater: function (docLastIterDocKey) {
                    var linkedDocument = self.searchResults.find(function (docLastIter) {
                        return docLastIter.getDisplayDocKey() === docLastIterDocKey;
                    });
                    linkedDocument.collection.remove(linkedDocument);
                    self.collection.add(linkedDocument);

                    self.addLinkView(linkedDocument);
                }
            });
        }

    });
    return LinkedDocumentsView;
});
