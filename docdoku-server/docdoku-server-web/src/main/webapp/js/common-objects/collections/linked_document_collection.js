define([
    "common-objects/models/linked_document"
], function (
    LinkedDocument
    ) {
    var LinkedDocumentCollection = Backbone.Collection.extend({

        model: LinkedDocument,

        comparator: function(linkedDocument) {
            return linkedDocument.getDocKey();
        }

    });

    return LinkedDocumentCollection;
});
