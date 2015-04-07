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
        }

    });

    return ProductInstanceDataModel;

});
