define([
    "common-objects/collections/linked/linked_change_item_collection",
    "common-objects/views/linked/linked_change_item",
    "text!common-objects/templates/linked/linked_change_items.html",
    "i18n!localization/nls/change-management-strings"
], function(LinkedChangeItemCollection, LinkedChangeItemView, template, i18n) {
    var LinkedRequestsView = Backbone.View.extend({

        tagName: 'div',
        className: 'linked-items-view',

        initialize: function() {
            this.searchResults = [];
            this._subViews = [];
            this.options.linkedPartsView = (this.options.linkedPartsView) ? this.options.linkedPartsView : null;
            this.options.linkedDocumentsView = (this.options.linkedDocumentsView) ? this.options.linkedDocumentsView : null;
            var self = this;
            this.$el.on("remove",function(){
                _(self._subViews).invoke("remove");
            });
        },

        render: function() {
            var self = this;

            this.$el.html(Mustache.render(template,
                {
                    i18n: i18n,
                    editMode: this.options.editMode,
                    label: this.options.label,
                    view: this
                }
            ));

            this.bindDomElements();
            this.bindTypeahead();

            this.collection.each(function(linkedChangeItem) {
                self.initialAddLinkView(linkedChangeItem);
            });

            return this;
        },

        bindDomElements: function() {
            this.referenceInput = this.$(".linked-items-reference-typehead");
            this.linksUL = this.$("#linked-items-" + this.cid);
        },

        initialAddLinkView: function(linkedChangeItem){
            var linkView = new LinkedChangeItemView({
                editMode: this.options.editMode,
                model: linkedChangeItem
            }).render();
            this._subViews.push(linkView);
            this.linksUL.append(linkView.el);
        },

        addLinkView: function(linkedChangeItem) {
            var alreadyExist = false;
            $.each(this._subViews,function(index,value){
                if(value.model.getId()==linkedChangeItem.getId()){
                    alreadyExist = true;
                }
            });
            if(!alreadyExist){
                this.initialAddLinkView(linkedChangeItem);

                var self = this;
                var affectedDocuments = linkedChangeItem.getAffectedDocuments();
                require(["common-objects/collections/linked/linked_document_collection"],function(LinkedDocumentCollection){
                    var affectedDocumentsCollection = new LinkedDocumentCollection(affectedDocuments);
                    affectedDocumentsCollection.each(function(linkedDocument){
                        self.options.linkedDocumentsView.collection.add(linkedDocument);
                        self.options.linkedDocumentsView.addLinkView(linkedDocument);
                    });
                });

                var affectedParts = linkedChangeItem.getAffectedParts();
                require(["common-objects/collections/linked/linked_part_collection"],function(LinkedPartCollection){
                    var affectedPartsCollection = new LinkedPartCollection(affectedParts);
                    affectedPartsCollection.each(function(linkedPart){
                        self.options.linkedPartsView.collection.add(linkedPart);
                        self.options.linkedPartsView.addLinkView(linkedPart);
                    });
                });
            }
        }
    });
    return LinkedRequestsView;
});