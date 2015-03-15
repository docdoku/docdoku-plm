/*global $,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Folder = Backbone.Model.extend({
        defaults: {
            home: false
        },
        initialize: function () {
            this.className = 'Folder';
        },
        getPath: function () {
            return this.get('path');
        },
        getName: function () {
            return this.get('name');
        },
        url: function () {
            if (this.get('id')) {
                return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/folders/' + this.get('id');
            } else if (this.collection) {
                return this.collection.url;
            }
        },
        moveTo:function(other){
            return $.ajax({method:'PUT',contentType:'application/json',url:this.url()+'/move',data:JSON.stringify(other)});
        }
    });
    return Folder;
});
