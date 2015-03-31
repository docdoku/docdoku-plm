/*global _,$,define,App*/
define(['backbone',
    'common-objects/collections/attribute_collection',
    'common-objects/collections/file/attached_file_collection'
], function (Backbone,AttributeCollection,AttachedFileCollection) {
	'use strict';
    var ProductInstanceIteration = Backbone.Model.extend({
        idAttribute: 'iteration',

        initialize: function () {
            this.className = 'ProductInstanceIteration';
            _.bindAll(this);

            var attributes = new AttributeCollection(this.get('instanceAttributes'));

            var filesMapping = _.map(this.get('attachedFiles'), function (fullName) {
                return {
                    'fullName': fullName,
                    shortName: _.last(fullName.split('/')),
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
        initBaselinedParts: function (context, callbacks) {
            this.setConfigurationItemId(context.model.attributes.configurationItemId);
            var that = this;
            $.ajax({
                context: context,
                type: 'GET',
                url:  this.url()+'/baselined-parts',
                success: function (baselinedParts) {
                    that.setBaselinedParts(baselinedParts);
                    callbacks.success(this);
                }
            });
        },

        urlRoot: function () {
            if (this.getConfigurationItemId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.getConfigurationItemId() +
                    '/product-instances/' + this.getSerialNumber() + '/iterations/';
            } else {
                return this.prototype.urlRoot();
            }
        },
        getUploadBaseUrl: function () {
            return App.config.contextPath + '/api/files/'+this.getBaseName();

        },
        getBaseName: function () {
            return App.config.workspaceId  + '/product-instances/' + this.getSerialNumber() +'/'+this.getConfigurationItemId() + '/iterations/' + this.getIteration()+'/';
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
        getBasedOn:function(){
            return this.get('basedOn').name;
        },
        getUpdateAuthor: function () {
            return this.get('updateAuthor');
        },
        getUpdateAuthorName: function () {
            return this.get('updateAuthorName');
        },
        getUpdateDate: function () {
            return this.get('updateDate');
        },
        getBaselinedParts: function () {
            return this.get('baselinedParts');
        },
        getACL: function(){
          return this.get('acl');
        },
        getInstanceAttributes:  function(){
            return this.get('instanceAttributes');
        },
        getlinkedDocuments:  function(){
            return this.get('linkedDocuments');
        },
        getAttachedFiles: function(){
          return this.get('attachedFiles');
        },

        setInstanceAttributes:  function(instanceAttributes){
            return this.set('instanceAttributes',instanceAttributes);
        },

        setBaselinedParts: function (baselinedParts) {
            this.set('baselinedParts', baselinedParts);
        },
        setConfigurationItemId:function(configurationItemId){
            this.set('configurationItemId', configurationItemId);
        },
        getBaselinePartsWithReference: function (ref, callback) {
            var baselinedParts = null;
            $.ajax({
                type: 'GET',
                url: this.url() + '/baselined-parts?q=' + ref,
                contentType: 'application/json; charset=utf-8',
                success: function (data) {
                    baselinedParts = data;
                    if (callback && callback.success) {
                        callback.success(data);
                    }
                },
                error: function (data) {
                    if (callback && callback.error) {
                        callback.error(data);
                    }
                }
            });
            return baselinedParts;
        }
    });

    return ProductInstanceIteration;
});
