define(['backbone'], function (Backbone) {
    'use strict';
    var LOVModel = Backbone.Model.extend({
        initialize: function () {

        },

        getLOVName:function(){
            return this.get("name");
        },

        getLOVValues:function(){
            return this.get("values");
        },

        getNumberOfValue:function(){
            return this.get("values").length();
        }
    });

    return LOVModel;
});
