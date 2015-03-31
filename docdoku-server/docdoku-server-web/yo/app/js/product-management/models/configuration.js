/*global _,$,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Configuration = Backbone.Model.extend({
        urlRoot: function () {
            if (this.productId) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + this.productId + '/configurations';
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/configurations';
        },
        initialize: function () {
            _.bindAll(this);
        },

        getName:function(){
            return this.get('name');
        },
        setName:function(name){
            this.set('name',name);
        },

        getDescription:function(){
            return this.get('description');
        },
        setDescription:function(description){
            this.set('description',description);
        },

        getSubstituteLinks:function(){},
        setSubstituteLinks:function(substituteLinks){},

        getOptionalUsageLinks:function(){},
        setOptionalUsageLinks:function(optionalUsageLinks){},

        getFormattedCreationDate:function(){}

    });
    return Configuration;
});
