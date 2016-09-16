/*global _,define*/
'use strict';
define(['backbone'], function (Backbone) {

    var BaselinedDocument = Backbone.Model.extend({
        initialize: function () {
            _.bindAll(this);
        },

        getDocumentMasterId: function () {
            return this.get('documentMasterId');
        },

        getTitle: function () {
            return this.get('title');
        },

        getVersion: function () {
            return this.get('version');
        },

        setVersion: function (version) {
            this.set('version', version);
        },

        getIteration: function () {
            return this.get('iteration');
        },

        setIteration: function (iteration) {
            this.set('iteration', iteration);
        },

        getAvailableIterations: function () {
            return this.get('availableIterations');
        }

    });

    return BaselinedDocument;
});
