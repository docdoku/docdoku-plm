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
            'change .user-defined-product-select':'fetchValues',
            'change .user-defined-type-select':'fetchValues'
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.$modal= this.$('#user_defined_function_modal');
            this.$productList = this.$('.user-defined-product-select');
            this.$typeList = this.$('.user-defined-type-select');
            this.$valueList = this.$('.user-defined-value-select');
            this.$runButton = this.$('.run-udf');
            this.$udfResult = this.$('.udf-result');
            this.$userDefineInit = this.$('.user-defined-init');
            this.$userDefineFunctionDef = this.$('.user-defined-function-def');
            this.fetchProducts();
            return this;
        },

        fetchProducts: function () {
            var productList = this.$productList;
            var typeList = this.$typeList;
            var _this = this;
            new ConfigurationItemCollection().fetch({success:function(products){
                products.each(function(product){
                    productList.append('<option value="'+product.getId()+'">'+product.getId()+'</option>');
                    typeList.empty();
                    typeList.append('<option value="latest">'+App.config.i18n.LATEST_SHORT+'</option>');
                    typeList.append('<option value="baseline">'+App.config.i18n.BASELINE+'</option>');
                });
                _this.fetchValues();
            }});
        },

        fetchValues: function () {
            var productId = this.$productList.val();
            var typeId = this.$typeList.val();
            var valueList = this.$valueList;
            valueList.empty();

            if (productId) {
                if (typeId === 'latest') {
                    valueList.append('<option value="wip">'+App.config.i18n.HEAD_WIP+'</option>');
                    valueList.append('<option value="latest">'+App.config.i18n.HEAD_CHECKIN+'</option>');
                    valueList.append('<option value="latest-released">'+App.config.i18n.HEAD_RELEASED+'</option>');

                } else if (typeId === 'baseline') {
                    new Baselines({},{type:'product',productId:productId}).fetch({success:function(baselines) {
                        baselines.each(function(baseline){
                            valueList.append('<option value="'+baseline.getId()+'">'+baseline.getName()+'</option>');
                        });
                    }});

                }
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
            this.$udfResult.html('');
            var productId = this.$productList.val();
            var valueId = this.$valueList.val();
            var runButton = this.$runButton;
            var _this = this;

            runButton.html(App.config.i18n.LOADING +' ...').prop('disabled',true);

            var PartCollection = Backbone.Collection.extend({
                url: function () {
                    return this.urlBase() + '/filter?configSpec=' + valueId + '&depth=10&path=-1';
                },

                urlBase: function () {
                    return App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + productId;
                }
            });

            new PartCollection().fetch({
                success:function(rootComponent){
                    _this.doUDF(rootComponent,function(){
                        runButton.html(App.config.i18n.RUN).prop('disabled',false);
                    });
                }
            });


            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        doUDF:function(pRootComponent,callback){

            var Fn = Function;
            var memo;

            try {

                var initFunction = new Fn(this.$userDefineInit.val());
                var reduceFunction =  new Fn('part','memo',this.$userDefineFunctionDef.val());

                memo = initFunction();

                var assemblyVisited = 0;
                var instancesVisited = 0;

                var visit = function(rootComponent,fn){

                    rootComponent.attrs = {};

                    _.each(rootComponent.attributes,function(attr){
                        if(attr.type === 'NUMBER'){
                            rootComponent.attrs[attr.name] = parseFloat(attr.value);
                        }else{
                            rootComponent.attrs[attr.name] = attr.value;
                        }
                    });

                    function next(a){
                        visit(a,fn);
                    }

                    for(var i = 0 ; i < rootComponent.amount ; i++) {

                        if(rootComponent.components.length){
                            assemblyVisited++;
                        }else{
                            instancesVisited++;
                        }

                        memo = fn(rootComponent, memo);

                        _.each(rootComponent.components,next);

                    }

                };

                visit(pRootComponent.first().attributes,reduceFunction);

                if(typeof memo !== 'string'){
                    try{
                        memo = JSON.stringify(memo);
                    }catch(e1){
                        memo = App.config.i18n.ERROR + ' : "' + e1 +'"';
                    }
                }

            }catch(e2){
                memo = App.config.i18n.ERROR + ' : "' + e2+'"';
            }

            this.$udfResult.html('memo = ' + memo);

            callback();
        }

    });

    return UserDefinedFunctionView;

});
