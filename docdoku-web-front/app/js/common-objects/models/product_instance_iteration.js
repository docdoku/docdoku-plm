/*global _,define,App*/
define(['backbone'
], function (Backbone) {
    'use strict';
    var ProductInstanceIteration = Backbone.Model.extend({
        idAttribute: 'iteration',

        initialize: function () {
            this.className = 'ProductInstanceIteration';
            _.bindAll(this);
        },
        defaults: {
            attachedFiles: [],
            instanceAttributes: []
        },

        urlRoot: function () {
            if (this.getConfigurationItemId) {
                return App.config.contextPath + '/api/workspaces/' +
                    App.config.workspaceId + '/product-instances/' +
                    this.getConfigurationItemId() + '/instances/' +
                    this.getSerialNumber() + '/iterations/';
            } else {
                return this.prototype.urlRoot();
            }
        },
        getUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/' + this.getBaseName();

        },
        getBaseName: function () {
            return App.config.workspaceId + '/product-instances/' + this.getSerialNumber() +
                '/' + this.getConfigurationItemId() + '/iterations/' + this.getIteration() + '/';
        },
        getSerialNumber: function () {
            return this.get('serialNumber');
        },
        getIteration: function () {
            return this.get('iteration');
        },
        setIteration: function (iteration) {
            return this.set('iteration', iteration);
        },
        getIterationNote: function () {
            return this.get('iterationNote');
        },
        setIterationNote: function (iterationNote) {
            this.set('iterationNote', iterationNote);
        },
        getConfigurationItemId: function () {
            return this.get('configurationItemId');
        },
        getBasedOnName: function () {
            return this.get('basedOn').name;
        },
        getBasedOnId: function () {
            return this.get('basedOn').id;
        },
        getUpdateAuthor: function () {
            return this.get('updateAuthor');
        },
        getUpdateAuthorName: function () {
            return this.get('updateAuthorName');
        },
        getCreationDate: function () {
            return this.get('creationDate');
        },
        getModificationDate: function () {
            return this.get('modificationDate');
        },
        getBaselinedParts: function () {
            return this.get('baselinedParts');
        },
        getACL: function () {
            return this.get('acl');
        },
        getInstanceAttributes: function () {
            return this.get('instanceAttributes');
        },
        getlinkedDocuments: function () {
            return this.get('linkedDocuments');
        },
        getPathToPathLinks: function () {
            return this.get('pathToPathLinks');
        },

        hasPathData: function () {
            return this.getPathData().length;
        },

        getPathData: function () {
            return this.get('pathDataMasterList');
        },

        getPathDataPaths: function () {
            return this.get('pathDataPaths');
        },

        getAttachedFiles: function () {
            return this.get('attachedFiles');
        },

        setInstanceAttributes: function (instanceAttributes) {
            return this.set('instanceAttributes', instanceAttributes);
        },

        setBaselinedParts: function (baselinedParts) {
            this.set('baselinedParts', baselinedParts);
        },
        setConfigurationItemId: function (configurationItemId) {
            this.set('configurationItemId', configurationItemId);
        },

        getSubstitutesParts: function () {
            return this.get('substitutesParts');
        },
        getOptionalsParts: function () {
            return this.get('optionalsParts');
        },

        setLinkedDocuments: function (linkedDocuments) {
            this.set('linkedDocuments', linkedDocuments);
        }
    });

    return ProductInstanceIteration;
});
