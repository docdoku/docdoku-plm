/*global _,$,define,App*/
define(['backbone','common-objects/utils/date'], function (Backbone,date) {
    'use strict';
    var Configuration = Backbone.Model.extend({
        urlRoot: function () {

            if (this.configurationItemId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.configurationItemId + '/configurations';
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/configurations';
        },
        initialize: function () {
            _.bindAll(this);
            this.configurationItemId = this.get('configurationItemId');
        },

        getName:function(){
            return this.get('name');
        },
        setName:function(name){
            this.set('name',name);
        },

        getConfigurationItemId:function(){
            return this.get('configurationItemId');
        },

        getDescription:function(){
            return this.get('description');
        },
        setDescription:function(description){
            this.set('description',description);
        },

        getSubstituteLinks:function(){},

        getSubstitutesParts:function(){
            return this.get('substitutesParts');
        },
        setSubstituteLinks:function(substituteLinks){},

        getOptionalUsageLinks:function(){},

        getOptionalsParts:function(){
            return this.get('optionalsParts');
        },

        setOptionalUsageLinks:function(optionalUsageLinks){},

        getCreatedDate:function(){
            return this.get('createdDate');
        },

        getFormattedCreationDate:function(){
            return date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreatedDate()
            );
        }

    });
    return Configuration;
});
