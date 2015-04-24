/*global define,_*/
define(['backbone'], function (Backbone) {

    'use strict';

    var ProductInstancePathIterationDataModel = Backbone.Model.extend({


        initialize: function () {
            _.bindAll(this);
        },

        url: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.getSerialNumber() + '/pathdata/' + this.getId() + '/' + this.getIteration();
        },

        getId: function () {
            return this.get('id');
        },

        setId: function (id) {
            this.set('id', id);
        },

        getDocumentLinked: function () {
            return this.get('linkedDocuments');
        },

        setDocumentLinked: function (linkedDocuments) {
            this.set('linkedDocuments', linkedDocuments);
        },

        getInstanceAttributes: function (attributes) {
            return this.get('instanceAttributes');
        },

        setInstanceAttributes: function (attributes) {
            this.set('instanceAttributes', attributes);
        },

        getPath: function () {
            return this.get('path');
        },

        setPath: function (path) {
            this.set('path', path);
        },

        setSerialNumber: function (serialNumber) {
            this.set('serialNumber', serialNumber);
        },

        getSerialNumber: function () {
            return this.get('serialNumber');
        },

        getPartsPath: function () {
            return this.get('partsPath').parts;
        },

        getIterationNote: function () {
            return this.get('noteIteration');
        },

        setIterationNote: function (noteIteration) {
            this.set('noteIteration', noteIteration);
        },

        setIteration: function (iteration) {
            this.set('iteration', iteration);
        },

        getIteration: function () {
            return this.get('iteration');
        },

        getAttachedFiles: function () {
            return this.get('attachedFiles');
        },

        setAttachedFiles: function (attachedFiles) {
            this.set('attachedFiles', attachedFiles);
        },

        isLastIteration: function (iterationNumber) {
            // return TRUE if the iteration is the very last (check or uncheck)
            return this.get('lastIterationNumber') === iterationNumber;
        },

        getUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/' + App.config.workspaceId + '/product-instances/' + this.getSerialNumber() + '/' + App.config.productId + '/pathdata/' + this.getId() + '/iterations/' + this.getIteration() + '/';
        },

        getDeleteBaseUrl: function () {
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.getSerialNumber() + '/pathdata/' + this.getId() + '/iterations/' + this.getIteration();
        }

    });

    return ProductInstancePathIterationDataModel;

});


