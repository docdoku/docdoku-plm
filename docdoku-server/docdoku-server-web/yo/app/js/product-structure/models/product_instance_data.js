/*global define,_*/
define(['backbone'], function (Backbone) {

    'use strict';

    var ProductInstanceDataModel = Backbone.Model.extend({

        getDocumentLinked: function(){
            return this.get('linkedDocuments');
        },

        //Files related :
        getAttachedFiles : function(){
            return this.get('attachedFiles');
        },

        getUploadBaseUrl : function(){
            return '';
        },

        getDeleteBaseUrl: function(){
            return '';
        },

        getAttributes: function(attributes){
            return this.get('instanceAttributes');
        },

        setAttributes: function(attributes){
            this.set('instanceAttributes', attributes);
        }

    });

    return ProductInstanceDataModel;

});
