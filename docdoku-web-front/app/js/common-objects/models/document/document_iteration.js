/*global _,define,App*/
define([
	'backbone',
	'common-objects/utils/date',
	'common-objects/collections/attribute_collection',
	'common-objects/collections/file/attached_file_collection'
], function (Backbone, date, AttributeCollection, AttachedFileCollection) {
	'use strict';
	var DocumentIteration = Backbone.Model.extend({

        idAttribute: 'iteration',

		url: function () {
			if (this.getIteration()) {
                return this.baseUrl();
            }
			return this.collection.baseUrl();
		},

		baseUrl: function () {
			return this.collection.baseUrl() + '/' + this.getIteration();
		},

		initialize: function () {

			this.className = 'DocumentIteration';

			var attributes = new AttributeCollection(this.get('instanceAttributes'));

			var filesMapping = _.map(this.get('attachedFiles'), function (binaryResource) {
				return {
					fullName: binaryResource.fullName,
					shortName: _.last(binaryResource.fullName.split('/')),
					created: true
				};
			});
			var attachedFiles = new AttachedFileCollection(filesMapping);

			//'attributes' is a special name for Backbone
			this.set('instanceAttributes', attributes);
			this.set('attachedFiles', attachedFiles);
		},

		defaults: {
			attachedFiles: [],
			instanceAttributes: []
		},

		getAttachedFiles: function () {
			return this.get('attachedFiles');
		},

		getAttributes: function () {
			return this.get('instanceAttributes');
		},

		getWorkspace: function () {
			return this.get('workspaceId');
		},

		getReference: function () {
			console.warn('Usage of getReference() is deprecated use getId()');
			return this.getId();
		},
		getId: function () {
			return this.get('id');
		},

		getIteration: function () {
			return this.get('iteration');
		},

		getDocumentMasterId: function () {
			return  this.get('documentMasterId');
		},

		getVersion: function () {
			return  this.get('version');
		},

        getTitle: function () {
			return  this.get('title');
		},

		getDocKey: function () {
			return this.getDocumentMasterId() + '-' + this.getVersion();
		},

		getLinkedDocuments: function () {
			return this.get('linkedDocuments');
		},

		/**
		 * file Upload uses the old servlet, not the JAXRS Api         *
		 * return /files/{workspace}/documents/{docId}/{version}/{iteration}/
		 * @returns string
		 */
		getUploadBaseUrl: function () {
			return App.config.contextPath + '/api/files/' + this.getBaseName() + '/';
		},

		getBaseName: function () {
			return this.getWorkspace() + '/documents/' + this.getDocumentMasterId() + '/' + this.getVersion() + '/' + this.getIteration();
		},

        getUsedByDocuments: function () {
            return [];
        },

        getUsedByParts: function () {
            return [];
        }
	});
	return DocumentIteration;
});
