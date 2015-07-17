/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/udf/calculation.html'
],function(Backbone, Mustache, template){

    'use strict';

    var CalculationView = Backbone.View.extend({

        className:'calculation',

        events:{
            'click .remove':'onRemove'
        },

        initialise:function(){
            _.bindAll(this);
        },

        resetCalculation:function(){
            this.memo = 0;
            this.visitedAssemblies = 0;
            this.visitedInstances = 0;
            this.$memo.text('');
            this.$assembliesVisited.text('');
            this.$instancesVisited.text('');
            this.$result.hide();
        },

        render:function(){
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, attributeNames:this.options.attributeNames}));
            this.binDOMElements();
            this.resetCalculation();
            return this;
        },

        binDOMElements:function(){
            this.$operator = this.$('[name="operator"]');
            this.$attributeName = this.$('[name="attributeName"]');
            this.$result = this.$('.result');
            this.$memo = this.$('.memo');
            this.$instancesVisited = this.$('.instances-visited');
            this.$assembliesVisited = this.$('.assemblies-visited');
        },

        getOperator:function(){
            return this.$operator.val();
        },

        getAttributeName:function(){
            return this.$attributeName.val();
        },

        getMemo:function(){
            return this.memo;
        },

        setMemo:function(memo){
            this.memo = memo;
        },

        incVisitedAssemblies:function(){
            this.visitedAssemblies++;
        },

        incVisitedInstances:function(){
            this.visitedInstances++;
        },

        onEnd:function(){

            var visitedNodes = this.visitedAssemblies + this.visitedInstances;

            if(visitedNodes){
                if(this.getOperator() === 'AVG'){
                    this.memo = this.memo / visitedNodes;
                }
            }

            this.$memo.text(this.memo);
            this.$assembliesVisited.text(this.visitedAssemblies);
            this.$instancesVisited.text(this.visitedInstances);
            this.$result.show();
        },

        onRemove:function(){
            this.trigger('removed');
            this.remove();
        }

    });

    return CalculationView;

});
