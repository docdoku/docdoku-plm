define([
        "text!templates/product-instances/product_instances_creation.html",
        "i18n!localization/nls/product-instances-strings",
        "common-objects/models/product_instance",
        "common-objects/collections/configuration_items",
        "common-objects/collections/baselines"
], function (template, i18n, ProductInstanceModel, ConfigurationItemCollection, BaselinesCollection) {

    var ProductInstanceCreationView = Backbone.View.extend({
        model: new ProductInstanceModel(),

        events: {
            "submit #product_instance_creation_form" : "onSubmitForm",
            "hidden #product_instance_creation_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            this._subViews = [];
            _.bindAll(this);
            this.$el.on("remove",this.removeSubviews);                                                                  // Remove cascade
        },

        removeSubviews: function(){
            _(this._subViews).invoke("remove");
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            new ConfigurationItemCollection().fetch({success: this.fillConfigurationItemList});
            return this;
        },

        fillConfigurationItemList:function(list){
            var self = this ;
            list.each(function(product){
                self.$inputConfigurationItem.append("<option value='"+product.getId()+"'"+">"+product.getId()+"</option>");
            });
            this.$inputConfigurationItem.change(function(){self.fillBaselineList();});
        },
        fillBaselineList:function(){
            var self = this ;
            this.$inputBaseline.empty();
            this.$inputBaseline.attr('disabled', 'disabled');
            new BaselinesCollection({},{productId:self.$inputConfigurationItem.val()}).fetch({
                success: function(list){
                    list.each(function(baseline){
                        self.$inputBaseline.append("<option value='"+baseline.getId()+"'"+">"+baseline.getName()+"</option>");
                    });
                    self.$inputBaseline.removeAttr('disabled');
                }
            });
        },

        bindDomElements:function(){
            this.$modal = this.$('#product_instance_creation_modal');
            this.$inputSerialNumber = this.$('#inputSerialNumber');
            this.$inputConfigurationItem = this.$('#inputConfigurationItem');
            this.$inputBaseline = this.$('#inputBaseline');
        },

        onSubmitForm: function(e) {
            var data ={
                serialNumber: this.$inputSerialNumber.val(),
                configurationItemId: this.$inputConfigurationItem.val(),
                baselineId:this.$inputBaseline.val()
            };

            if(data.serialNumber && data.configurationItemId && data.baselineId){
                this.model.unset("serialNumber");
                this.model.save(data,{
                    success: this.onProductInstanceCreated,
                    error: this.onError,
                    wait: true
                });
            }

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onProductInstanceCreated: function(model){
            this.collection.fetch();
            this.closeModal();
        },

        onError: function(model, error){
            alert(i18n.CREATION_ERROR + " : " + error.responseText);
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        }
    });

    return ProductInstanceCreationView;
});