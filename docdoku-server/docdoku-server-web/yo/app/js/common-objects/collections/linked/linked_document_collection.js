/*global define*/
'use strict';
define([
    'backbone',
    "common-objects/models/linked/linked_document"
], function (Backbone, LinkedDocument) {
    var LinkedDocumentCollection = Backbone.Collection.extend({

        model: LinkedDocument,

        comparator: function (linkedDocument) {
            return linkedDocument.getDocKey();
        }

    });

    return LinkedDocumentCollection;
});
