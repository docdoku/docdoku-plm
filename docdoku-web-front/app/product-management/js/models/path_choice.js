/*global _,define*/
'use strict';
define(['backbone'], function (Backbone) {

    var PathChoice = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        getResolvedPath:function(){
            return this.get('resolvedPath');
        },

        getPartUsageLink: function () {
            return this.get('partUsageLink');
        },

        getPartUsageLinkId: function () {
            return this.get('partUsageLink').fullId;
        },

        getResolvedPathAsString:function(){
            return this.getResolvedPath().map(function(resolvedPath){
                return resolvedPath.partLink.fullId;
            }).join('-');
        },

        getKey:function(){
            return this.getResolvedPath().map(function(resolvedPath){
                return resolvedPath.partIteration.number + '-' + resolvedPath.partIteration.version;
            }).join('-');
        }

    });

    return PathChoice;
});
