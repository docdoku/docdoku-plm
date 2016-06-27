/*global define*/
define([
    'backbone',
    'common-objects/models/linked/linked_document'
], function (Backbone, LinkedDocument) {
	'use strict';
    var LinkedDocumentCollection = Backbone.Collection.extend({

        model: LinkedDocument,

        comparator: function (linkedDocument) {
            return linkedDocument.getDocKey();
        }

    });

    return LinkedDocumentCollection;
});
