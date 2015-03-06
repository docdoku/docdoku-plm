define(['backbone'], function (Backbone) {
    'use strict';
    var LOVModel = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        getLOVName:function(){
            return this.get('name');
        },

        setLOVName:function(newName){
            this.set('name', newName);
        },

        getLOVValues:function(){
            return this.get('values');
        },

        getNumberOfValue:function(){
            return this.get('values').length;
        }
    });

    return LOVModel;
});
