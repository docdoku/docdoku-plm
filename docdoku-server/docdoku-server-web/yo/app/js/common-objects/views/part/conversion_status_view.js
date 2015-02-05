/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/conversion_status.html'
], function (Backbone,Mustache,template) {
    'use strict';
    var ConversionStatusView = Backbone.View.extend({
        className:'conversion-status',
        events:{
            'click .reload':'render',
            'click .launch':'launch'
        },
        render:function(){
            var _this = this;
            _this.$el.html('...');
            this.model.getConversionStatus().success(function(status){
                _this.$el.html(Mustache.render(template, {status:status,hasCadFile:_this.model.get('nativeCADFile'),i18n:App.config.i18n}));
            }).error(function(){
                _this.$el.html(Mustache.render(template, {status:null,hasCadFile:_this.model.get('nativeCADFile'),i18n:App.config.i18n}));
            });
            return this;
        },
        launch:function(){
            var self = this;
            this.model.launchConversion().success(function(){
                self.render();
            }).error(function(){
                self.render();
            });
        }
    });
    return ConversionStatusView;
});
