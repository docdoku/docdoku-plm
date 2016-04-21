/*global _,define,App*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Baseline = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        getId: function () {
            return this.get('id');
        },

        getName: function () {
            return this.get('name');
        },

        getDescription:function(){
            return this.get('description');
        },

        getCreationDate: function () {
            return this.get('creationDate');
        },

        getBaselinedParts:function(){
            return this.get('baselinedParts');
        },

        getConfigurationItemId: function(){
            return this.get('configurationItemId');
        },

        setConfigurationItemId: function(configurationItemId){
            this.set('configurationItemId',configurationItemId);
        },

        getBomUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/'+this.getId()+'/bom' ;
        },

        getSceneUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/'+this.getId()+'/scene' ;
        },

        getZipUrl:function (){
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + encodeURIComponent(this.getConfigurationItemId()) + '/export-files?configSpecType=' + encodeURIComponent(this.getId());
        },

        getSubstitutesParts:function(){
            //can be null, and used as an array.
            return this.get('substitutesParts');
        },

        getOptionalsParts:function(){
            //can be null, and used as an array.
            return this.get('optionalsParts');
        },

        hasObsoletePartRevisions:function(){
            return this.get('hasObsoletePartRevisions');
        },

        hasPathToPathLink: function() {
            return this.getPathToPathLinks().length;
        },
        getPathToPathLinks: function () {
            return this.get('pathToPathLinks');
        }

    });

    return Baseline;
});
