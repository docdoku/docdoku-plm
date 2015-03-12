/*global _,define*/
'use strict';
define(['backbone'], function (Backbone) {

    var PathChoice = Backbone.Model.extend({
        initialize: function () {
            _.bindAll(this);
        },

        getPartRevisionsKeys: function () {
            return this.get('partRevisionsKeys');
        },

        getPaths: function () {
            return this.get('paths');
        },

        getPath:function(){
            return this.get('paths').join('-');
        },

        getPartUsageLink: function () {
            return this.get('partUsageLink');
        },

        getPartUsageLinkPath: function () {
            return this.getPath()+'-'+this.getPartUsageLinkId();
        },

        getPartUsageLinkId: function () {
            return this.get('partUsageLink').id;
        }
    });

    return PathChoice;
});
