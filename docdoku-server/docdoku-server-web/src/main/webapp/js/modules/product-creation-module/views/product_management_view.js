define(
    ["text!modules/product-creation-module/templates/product_management_view.html",
        "i18n!localization/nls/product-creation-strings",
        "common-objects/collections/configuration_items",
        "modules/product-creation-module/views/configuration_item",
        "modules/product-creation-module/views/product_creation_view"
    ], function (template, i18n, ConfigurationItemCollection, ConfigurationItemView, ProductCreationView) {

    var ProductManagementView = Backbone.View.extend({

        events: {
            "hidden #product_creation_modal": "onHidden"
        },

        template: Mustache.compile(template),

        collection: new ConfigurationItemCollection(),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {

            var that = this ;
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();

            this.listenTo(this.collection,"reset",this.drawProducts);
            this.collection.fetch();
            this.productCreationview = new ProductCreationView({el:this.$("#tab-new-product")}).render();

            this.listenTo(this.productCreationview, 'product:created', this.onProductCreated);
            return this;
        },

        drawProducts:function(){

            this.productViews = [];
            this.$allProductsList.empty();
            var that = this ;

            this.collection.each(function(model){
                var productView = new ConfigurationItemView({model:model}).render();
                that.listenTo(model,"destroy",that.onProductRemoved);
                that.$allProductsList.append(productView.$el);
                that.productViews.push(productView);
            });

        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        },

        bindDomElements:function(){
            this.$modal = this.$('#product_management_modal');
            this.$allProductsList = this.$("#all-products-list");
        },

        onProductCreated: function(model) {
            this.trigger('product:created', model);
            this.closeModal();
        },
        onProductRemoved: function(model) {
            this.trigger('product:removed', model);
        },

        onError: function() {
            alert(i18n.CREATION_ERROR);
        }

    });

    return ProductManagementView;

});