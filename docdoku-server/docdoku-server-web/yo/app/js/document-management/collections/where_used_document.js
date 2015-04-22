/*global define,App*/
define([
    'backbone',
    'models/document'
], function (Backbone, Document) {
    'use strict';
    var WhereUsedDocumentList = Backbone.Collection.extend({

        model: Document,

        comparator: function (document) {
            return document.get('id');
        }

    });

    WhereUsedDocumentList.className = 'WhereUsedDocumentList';

    return WhereUsedDocumentList;
});
