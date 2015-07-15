/*global define*/
define([
    'backbone',
    'common-objects/models/linked/linked_document_iteration'
], function (Backbone, LinkedDocumentIteration) {
    'use strict';
    var LinkedDocumentIterationCollection = Backbone.Collection.extend({

        model: LinkedDocumentIteration,

        comparator: function (linkedDocumentIteration) {
            return linkedDocumentIteration.getDocKey();
        }

    });

    return LinkedDocumentIterationCollection;
});
