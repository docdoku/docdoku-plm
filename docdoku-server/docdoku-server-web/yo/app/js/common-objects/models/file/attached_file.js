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

        getSubType: function () {
            if (this.getFullName().indexOf('nativecad') > 0) {
                return 'nativecad';
            } else if (this.getFullName().indexOf('attachedfiles') > 0) {
                return 'attachedfiles';
            } else {
                return '';
            }
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
