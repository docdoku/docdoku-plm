/*global _,define,App,$*/
define([
    'backbone',
    'common-objects/utils/date',
    'common-objects/collections/attribute_collection',
    'common-objects/collections/file/attached_file_collection'
], function (Backbone, date, AttributeCollection, AttachedFileCollection) {
	'use strict';
    var PartIteration = Backbone.Model.extend({

        idAttribute: 'iteration',

        initialize: function () {

            this.className = 'PartIteration';

            var attributes = new AttributeCollection(this.get('instanceAttributes'));
            this.set('instanceAttributes', attributes);
            this.resetNativeCADFile();

            var filesMapping = _.map(this.get('attachedFiles'), function (fullName) {
                return {
                    'fullName': fullName,
                    shortName: _.last(fullName.split('/')),
                    created: true
                };
            });

            var attachedFiles = new AttachedFileCollection(filesMapping);

            this.set('attachedFiles', attachedFiles);

        },

        resetNativeCADFile: function () {
            var nativeCADFullName = this.get('nativeCADFile');
            if (nativeCADFullName) {
                var nativeCad = {
                    fullName: nativeCADFullName,
                    shortName: _.last(nativeCADFullName.split('/')),
                    created: true
                };
                this._nativeCADFile = new AttachedFileCollection(nativeCad);
            } else {
                this._nativeCADFile = new AttachedFileCollection();
            }
        },


        defaults: {
            instanceAttributes: []
        },

        getAttributes: function () {
            return this.get('instanceAttributes');
        },

        getAttributeTemplates: function () {
            return this.get('instanceAttributeTemplates');
        },

        getWorkspace: function () {
            return this.get('workspaceId');
        },

        getReference: function () {
            return this.getPartKey() + '-' + this.getIteration();
        },

        getIteration: function () {
            return this.get('iteration');
        },

        getPartKey: function () {
            return  this.get('number') + '-' + this.get('version');
        },

        getAttachedFiles: function () {
            return this.get('attachedFiles');
        },

        getBaseName: function (subType) {
            return this.getWorkspace() + '/parts/' + this.getNumber() + '/' + this.getVersion() + '/' + this.get('iteration') + '/' + subType;
        },

        getNumber: function () {
            return this.collection.part.getNumber();
        },

        getVersion: function () {
            return this.collection.part.getVersion();
        },

        getComponents: function () {
            return this.get('components');
        },

        isAssembly: function () {
            var components = this.getComponents();
            return components && components.length > 0;
        },


        getLinkedDocuments: function () {
            return this.get('linkedDocuments');
        },

        setLinkedDocuments: function (linkedDocuments) {
            this.set('linkedDocuments', linkedDocuments);
        },

        getLifeCycleState: function () {
            return this.get('lifeCycleState');
        },

        getConversionUrl:function(){
            return App.config.contextPath +
                '/api/workspaces/' + this.getWorkspace() +
                '/parts/' + this.getNumber() + '-' + this.getVersion() +
                '/iterations/' + this.get('iteration') +
                '/conversion';
        },

        getConversionStatus:function(){
            return $.get(this.getConversionUrl());
        },

        launchConversion:function(){
            return $.ajax({method:'PUT',url:this.getConversionUrl()});
        },

        getAttachedFilesUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/' + this.getWorkspace() + '/parts/' + this.getNumber() + '/' + this.getVersion() + '/' + this.get('iteration') + '/attachedfiles/';
        },

        getNativeCadFileUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/' + this.getWorkspace() + '/parts/' + this.getNumber() + '/' + this.getVersion() + '/' + this.get('iteration') + '/nativecad/';
        }

    });

    return PartIteration;

});
