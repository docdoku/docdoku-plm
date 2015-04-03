/*global define,App,_*/
define([
        'backbone',
        'mustache',
        'text!templates/product-instance-data-modal.html',
        'models/product_instance_data'
    ], function (Backbone, Mustache, template, ProductInstanceDataModel) {

        'use strict';

        var ProductInstanceDataModalView = Backbone.View.extend({

            events: {
                'hidden .modal.product-instance-data-modal': 'onHidden'
            },



            initialize: function () {
                this.path = this.options.path ? this.options.path : '-1';
                this.serialNumber = this.options.serialNumber;
                this.model = new ProductInstanceDataModel({
                    path : this.path,
                    serialNumber : this.serialNumber
                });
                _.bindAll(this);
            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
                this.$modal = this.$('.modal.product-instance-data-modal');
                var self = this;
                var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/' + App.config.productId + '/product-instances/' + this.serialNumber + '/pathdata?path=' + this.path;
                $.ajax({
                    type: 'GET',
                    url : url,
                    success: function(data){
                        self.model = new ProductInstanceDataModel(data);
                        self.buildTabs();
                    },
                    error : function(){

                    }
                });

                return this;
            },

            buildTabs:function(){
                debugger
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return ProductInstanceDataModalView;
    }
);
