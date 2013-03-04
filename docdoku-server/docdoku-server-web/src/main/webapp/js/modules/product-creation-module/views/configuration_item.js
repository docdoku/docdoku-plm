define(["text!modules/product-creation-module/templates/configuration_item.html",
    "i18n!localization/nls/product-creation-strings"],function(template, i18n) {

    var ConfigurationItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click .remove":"removeProduct"
        },

        tagName:'li',
        className:'well',

        initialize: function() {

        },

        render: function() {
           this.$el.html(this.template({model:this.model,i18n: i18n}));
           return this;
        },

        removeProduct:function(){
            var that = this ;
            var confirmation = confirm(i18n.DELETE_PRODUCT_CONFIRM);
            if(confirmation){
                this.model.destroy({success:function(model,response) {
                    that.remove();
                },error:function(model,response) {
                    alert(i18n.DELETE_PRODUCT_ERROR);
                }});
            }
        }

    });

    return ConfigurationItemView;

});
