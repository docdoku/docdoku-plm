/*global define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var AttachedFile = Backbone.Model.extend({
        idAttribute: 'fullName',

        getFullName:function(){
            return this.get('fullName');
        },

        setFullName:function(name){
            this.set('fullName',name);
        },

        getShortName:function(){
            return this.get('shortName');
        },

        setShortName:function(name){
            this.set('shortName',name);
        },

        rewriteUrl:function(){
            var name = this.getShortName();
            var url = this.url;
            var index = url.lastIndexOf('/');
            this.url = url.substr(0,index)+'/'+name;
        }

    });



    return AttachedFile;

});
