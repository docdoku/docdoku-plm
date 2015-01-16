/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/udf/user_defined_function.html',
    'collections/configuration_items',
    'common-objects/collections/baselines'
], function (Backbone, Mustache, template,ConfigurationItemCollection,Baselines) {

    'use strict';

    var UserDefinedFunctionView = Backbone.View.extend({

        events: {
            'hidden #user_defined_function_modal': 'onHidden',
            'submit #user_defined_function_form':'run',
            'change .user-defined-product-select':'fetchBaselines'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$modal= this.$('#user_defined_function_modal');
            this.$productList = this.$('.user-defined-product-select');
            this.$baselineList = this.$('.user-defined-baseline-select');
            this.fetchProducts();
            return this;
        },

        fetchProducts:function(){
            var productList = this.$productList;
            var baselineList = this.$baselineList;
            new ConfigurationItemCollection().fetch({success:function(products){
                products.each(function(product){
                    productList.append('<option value="'+product.getId()+'">'+product.getId()+'</option>');
                    baselineList.empty().append('<option value="latest">'+App.config.i18n.LATEST+'</option>');
                });
            }});
        },

        fetchBaselines:function(){
            var productId = this.$productList.val();
            var baselineList = this.$baselineList;
            baselineList.empty();
            baselineList.append('<option value="latest">'+App.config.i18n.LATEST+'</option>');
            if(productId){
                new Baselines({},{type:'product',productId:productId}).fetch({success:function(baselines) {
                    baselines.each(function(baseline){
                        baselineList.append('<option value="'+baseline.getId()+'">'+baseline.getName()+'</option>');
                    });
                }});
            }
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        run: function(e){
            alert('run');
            e.preventDefault();
            e.stopPropagation();
            return false;
        }

    });

    return UserDefinedFunctionView;

});
