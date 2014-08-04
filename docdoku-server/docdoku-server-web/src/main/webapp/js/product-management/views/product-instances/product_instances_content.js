define([
    "common-objects/collections/product_instances",
    "common-objects/collections/configuration_items",
    "text!templates/product-instances/product_instances_content.html",
    "i18n!localization/nls/product-instances-strings",
    "views/product-instances/product_instances_list",
    "text!common-objects/templates/buttons/delete_button.html"
], function (
    ProductInstancesCollection,
    ConfigurationItemCollection,
    template,
    i18n,
    ProductInstancesListView,
    delete_button
    ) {
    var BaselinesContentView = Backbone.View.extend({

        template: Mustache.compile(template),

        partials:{
            delete_button: delete_button
        },

        events:{
            "click button.new-product-instance":"newProductInstance",
            "click button.delete":"deleteBaseline"
        },

        initialize: function () {
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n},this.partials));

            this.bindDomElements();
            new ConfigurationItemCollection().fetch({success: this.fillProductList});
            var self = this ;
            this.$inputProductId.change(function(){self.createProductInstancesView()});
            this.createProductInstancesView();
            return this;
        },

        bindDomElements:function(){
            this.deleteButton = this.$(".delete");
            this.$inputProductId = this.$("#inputProductId");
        },

        newProductInstance:function(){
            var self = this;
            this.lockButton(true);
            require(["views/product-instances/product_instances_creation"], function(ProductInstanceCreationView){
                var productInstanceCreationView = new ProductInstanceCreationView({
                    collection:self.collection
                });
                $("body").append(productInstanceCreationView.render().el);
                productInstanceCreationView.openModal();
                self.lockButton(false);
            });
        },

        fillProductList: function(list){
            var self = this ;
            if(list){
                list.each(function(product){
                    self.$inputProductId.append("<option value='"+product.getId()+"'"+">"+product.getId()+"</option>");
                });
                this.$inputProductId.combobox({bsVersion: '2'});
            }
        },

        createProductInstancesView: function(){
            if(this.listView){
                this.listView.remove();
                this.changeDeleteButtonDisplay(false);
            }
            if(this.$inputProductId.val()){
                this.collection = new ProductInstancesCollection({},{productId:this.$inputProductId.val()});
            }else{
                this.collection = new ProductInstancesCollection({});
            }
            this.listView = new ProductInstancesListView({
                collection:this.collection
            }).render();
            this.$el.append(this.listView.el);
            this.listView.on("delete-button:display", this.changeDeleteButtonDisplay);
        },

        deleteBaseline:function(){
            this.listView.deleteSelectedProductInstances();
        },

        changeDeleteButtonDisplay:function(state){
            if(state){
                this.deleteButton.show();
            }else{
                this.deleteButton.hide();
            }
        },

        lockButton: function(state){
            if(state){
                $("button.new-product-instance").attr("disabled", "disabled");
            }else{
                $("button.new-product-instance").removeAttr("disabled");
            }
        }
    });

    return BaselinesContentView;
});
