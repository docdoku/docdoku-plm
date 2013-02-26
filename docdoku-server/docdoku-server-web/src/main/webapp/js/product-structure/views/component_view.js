define(  [
    "text!templates/component_view.html",
    "i18n!localization/nls/product-structure-strings"
],function(template, i18n) {

    var ComponentView = Backbone.View.extend({

        template: Mustache.compile(template),

        events: {
            "click a.remove":"onRemove",
            "change input[name=amount]":"changeAmount",
            "change input[name=number]":"changeNumber",
            "change input[name=name]":"changeName"
        },

        initialize: function() {
        },

        render: function() {
            this.$el.html(this.template({model: this.model.attributes, i18n:i18n}));
            return this;
        },

        onRemove : function(){
            if(this.options.removeHandler){
                this.options.removeHandler();
            }
        },

        changeAmount:function(e){
            this.model.set("amount", e.target.value);
        },
        changeNumber:function(e){
            this.model.get("component").number =  e.target.value;
        },
        changeName:function(e){
            this.model.get("component").name =  e.target.value;
        }


    });

    return ComponentView;
});
