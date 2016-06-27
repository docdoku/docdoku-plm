/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/conversion_status.html'
], function (Backbone,Mustache,template) {
    'use strict';
    var ConversionStatusView = Backbone.View.extend({
        className:'conversion-status',
        hasNewFiles: false,
        events:{
            'click .reload':'render',
            'click .launch':'launch'
        },
        render:function(){
            var _this = this;
            this.model.getConversionStatus().success(function(status){
                _this.$el.html(Mustache.render(template, {status:status,hasCadFile:_this.model.get('nativeCADFile') || _this.hasNewFiles,i18n:App.config.i18n}));
                if(status && status.pending){
                    setTimeout(function() {
                        _this.render();
                    }, 3000);
                }
            }).error(function(){
                _this.$el.html(Mustache.render(template, {status:null,hasCadFile:_this.model.get('nativeCADFile'),i18n:App.config.i18n}));
            });
            return this;
        },
        launch:function(){
            this.hasNewFiles = true;
            var self = this;
            this.$el.html(Mustache.render(template, {status:{pending:true},hasCadFile:this.model.get('nativeCADFile'),i18n:App.config.i18n}));
            this.model.launchConversion().success(function(){
                self.render();
            }).error(function(){
                self.render();
            });
            setTimeout(function() {
                self.render();
            }, 3000);
        }
    });
    return ConversionStatusView;
});
