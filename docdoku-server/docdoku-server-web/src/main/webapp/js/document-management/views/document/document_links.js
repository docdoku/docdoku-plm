define([
    "collections/document_iteration",
    "views/document/document_link",
    "text!templates/document/document_links.html",
    "i18n!localization/nls/document-management-strings"
], function(DocumentIterationCollection, DocumentLinkView, template, i18n) {
    var DocumentLinksView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-documents-view',

        events: {
            "click #toggle-links-edit-mode": "toggleLinksEditMode"
        },

        LinksEditMode: {
            IDLE: 0,
            EDITION: 1
        },

        initialize: function() {
            this.linksEditMode = this.LinksEditMode.IDLE;
            this.searchResults = [];
        },

        render: function() {
            var self = this;

            this.$el.html(Mustache.render(template,
                {
                    i18n: i18n,
                    editMode: this.options.editMode,
                    view: this
                }
            ));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function(linkedDocument) {
                self.addLinkView(linkedDocument);
            });

            return this;
        },

        bindDomElements: function() {
            this.documentReferenceInput = this.$("#document-reference-typeahead");
            this.toggleLinksEditModeButton = this.$("#toggle-links-edit-mode");
            this.linksUL = this.$("#linked-docs-" + this.cid);
        },

        addLinkView: function(linkedDocument) {
            var self = this;
            var linkView = new DocumentLinkView({
                editMode: this.options.editMode,
                model: linkedDocument
            }).render();

            this.linksUL.append(linkView.el);
        },

        bindTypeahead: function() {
            var self = this;

            this.documentReferenceInput.typeahead({
                source: function(query, process) {
                    $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/documents/docs_last_iter?q=' + query, function(data) {

                        self.searchResults = new DocumentIterationCollection(data);

                        // Remove documents that are already linked
                        var docsToRemove = [];
                        self.searchResults.each(
                            function(documentIteration) {
                                var linkedDoc = self.collection.find(
                                    function(linkedDocument) {
                                        return linkedDocument.getDocKey() == documentIteration.getDocKey();
                                    });
                                if(!_.isUndefined(linkedDoc))
                                    docsToRemove.push(documentIteration);
                            }
                        );
                        self.searchResults.remove(docsToRemove);

                        process(self.searchResults.map(function(docLastIter) {
                            return docLastIter.getDocKey();
                        }));
                    })
                },

                sorter: function(docsLastIterDocKey) {
                    return docsLastIterDocKey.sort();
                },

                updater: function(docLastIterDocKey) {
                    var linkedDocument = self.searchResults.find(function(docLastIter) {
                        return docLastIter.getDocKey() == docLastIterDocKey;
                    });
                    linkedDocument.collection.remove(linkedDocument);
                    self.collection.add(linkedDocument);

                    self.addLinkView(linkedDocument);
                }
            });
        },

        gotoEditionState: function(){
            this.linksEditMode = this.LinksEditMode.EDITION;
            this.linksUL.addClass("edition");
        },

        gotoIdleState: function(){
            this.linksEditMode = this.LinksEditMode.IDLE;
            this.linksUL.removeClass("edition");
        },

        toggleLinksEditMode: function() {
            switch(this.linksEditMode){
                case this.LinksEditMode.IDLE:
                    this.gotoEditionState();
                    break;
                case this.LinksEditMode.EDITION:
                    this.gotoIdleState();
                    break;
            }
        }

    });
    return DocumentLinksView;
});
