/*global define,App*/
define(['backbone'], function (Backbone) {

    'use strict';

    var ProductInstanceDataModel = Backbone.Model.extend({

        defaults: {
            attachedFiles: [],
            instanceAttributes: []
        },

        getId: function(){
            return this.get('id');
        },

        getDocumentLinked: function(){
            return this.get('linkedDocuments');
        },

        setDocumentLinked: function(linkedDocuments){
            this.set('linkedDocuments', linkedDocuments);
        },

        getAttributes: function(){
            return this.get('instanceAttributes');
        },

        setAttributes: function(attributes){
            this.set('instanceAttributes', attributes);
        },

        getPath: function(){
            return this.get('path');
        },

        setPath: function(path){
            this.set('path', path);
        },

        getDescription: function(){
            return this.get('description');
        },

        setDescription: function(description){
            this.set('description', description);
        },

        //Files related :
        getAttachedFiles : function(){
            return this.get('attachedFiles');
        },

        setAttachedFiles : function(attachedFiles){
            this.set('attachedFiles', attachedFiles);
        },

        getUploadBaseUrl : function(serialNumber){
            return App.config.contextPath + '/api/files/' + App.config.workspaceId + '/product-instances/' + serialNumber + '/' + App.config.productId + '/pathdata/' + this.getId()+'/';
        },

        getDeleteBaseUrl: function(serialNumber){
            return App.config.contextPath + '/api/files/' + App.config.workspaceId + '/product-instances/' + serialNumber + '/' + App.config.productId + '/pathdata/' + this.getId()+'/';
        }

    });
    return ProductInstanceDataModel;

});


