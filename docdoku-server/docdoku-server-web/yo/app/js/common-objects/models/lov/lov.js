/*global _,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var LOVModel = Backbone.Model.extend({

        idAttribute:'id',

        initialize: function () {
            _.bindAll(this);
        },

        getLOVId:function(){
            return this.get('id');
        },

        setLOVId:function(){
            this.set('id', this.getLOVName());
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
        },

        getWorkspaceId:function(){
            return this.get('workspaceId');
        },

        isDeletable:function(){
            return this.get('deletable');
        },
        /*
           Override the isNew function of backbone to send the good request POST for new and PUT for update
         */
        /*setNew:function(isNew){
            this.isNew = function(){
                return isNew;
            };
        },*/

        url:function(){
            var endUrl = '';
            if(this.getLOVName()){
                endUrl = this.isNew()?'':'/'+ encodeURIComponent(this.getLOVName());
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/lov'+ endUrl;
        }

    });

    return LOVModel;
});
