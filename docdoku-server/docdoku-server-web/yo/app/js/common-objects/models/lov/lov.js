define(['backbone'], function (Backbone) {
    'use strict';
    var LOVModel = Backbone.Model.extend({

        idAttribute:'id',

        /*
           The backend name is the name use to modified the lov from backend side
           Since the name can be modified, we save the original name to update the good lov
         */
        backendName : null,

        initialize: function () {
            var name = this.getLOVName();
            if(name !== ''){
                this.backendName = name;
            }
            _.bindAll(this);
            this.on('sync', function(){
                //this.setNew(false);
                this.backendName = this.getLOVName();
            })
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
            if(this.backendName){
                endUrl = this.isNew()?'':'/'+ encodeURIComponent(this.backendName);
            }
            return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/lov'+ endUrl;
        }

    });

    return LOVModel;
});
