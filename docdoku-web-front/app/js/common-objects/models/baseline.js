/*global _,define,App*/
define(['backbone', 'common-objects/utils/date'], function (Backbone, Date) {
	'use strict';
    var Baseline = Backbone.Model.extend({

        initialize: function () {
            _.bindAll(this);
        },

        getId: function () {
            return this.get('id');
        },

        getType:function(){
            return this.get('type');
        },

        isReleased:function(){
            return this.get('type')==='RELEASED';
        },

        getName: function () {
            return this.get('name');
        },

        getDescription:function(){
            return this.get('description');
        },

        getCreationDate: function () {
            return this.get('creationDate');
        },

        getFormattedCreationDate: function () {
            return Date.formatTimestamp(
                App.config.i18n._DATE_FORMAT,
                this.getCreationDate()
            );
        },

        getAuthor:function(){
            return this.get('author').name;
        },

        getAuthorLogin: function () {
            return this.get('author').login;
        }

    });

    return Baseline;
});
