/*global _,$,define*/
define(['backbone'], function (Backbone) {
	'use strict';
    var Baseline = Backbone.Model.extend({
        initialize: function () {
            _.bindAll(this);
        },
        getId: function () {
            return this.get('id');
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
        getBaselinedParts:function(){
            return this.get('baselinedParts');
        },
        getConfigurationItemId: function(){
            return this.get('configurationItemId');
        },
        setConfigurationItemId: function(configurationItemId){
            this.set('configurationItemId',configurationItemId);
        },
        getBaselinePartsWithReference:function(ref,callback){
            var baselinedParts=null;
            $.ajax({
                type: 'GET',
                url: this.url()+'/parts?q='+ref,
                contentType: 'application/json; charset=utf-8',
                success: function(data){
                    baselinedParts=data;
                    if(callback && callback.success){
                        callback.success(data);
                    }
                },
                error: function(data){
                    if(callback && callback.error){
                        callback.error(data);
                    }
                }
            });
            return baselinedParts;
        },

        duplicate:function(args){
            var _this = this;
            $.ajax({
                type: 'POST',
                url: this.url() + '/duplicate',
                data: JSON.stringify(args.data),
                contentType: 'application/json; charset=utf-8',
                success: function (data) {
                    var duplicatedBaseline = new Baseline(data);
                    duplicatedBaseline.setConfigurationItemId(_this.getConfigurationItemId());
                    _this.collection.add(duplicatedBaseline);
                    args.success(duplicatedBaseline);
                },
                error: args.error
            });
        },

        getBomUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/'+this.getId()+'/bom' ;
        },

        getSceneUrl: function () {
            return App.config.contextPath + '/product-structure/#' + App.config.workspaceId + '/' + encodeURIComponent(this.getConfigurationItemId()) + '/config-spec/'+this.getId()+'/scene' ;
        }

    });

    return Baseline;
});
