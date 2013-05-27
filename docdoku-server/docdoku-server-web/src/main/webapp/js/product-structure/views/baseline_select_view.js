define([
    "collections/baselines",
    "text!templates/baseline_select.html",
    "i18n!localization/nls/product-structure-strings"
],function(Baselines,template,i18n){

    var BaselineSelectView = Backbone.View.extend({

        template : Mustache.compile(template),

        events:{
            "change select" : "onSelectorChanged"
        },

        initialize:function(){
            this.collection = new Baselines();
            this.listenToOnce(this.collection,"reset",this.onCollectionReset);
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.collection.fetch({reset:true});
            return this ;
        },

        bindDomElements:function(){
            this.$select = this.$("select");
        },

        onCollectionReset:function(){
            var that = this ;
            this.collection.each(function(baseline){
                that.$select.append("<option value='"+baseline.getId()+"'>"+baseline.getName()+"</option>");
            });
        },

        onSelectorChanged:function(e){
            this.trigger("config_spec:changed", e.target.value);
        }

    });

    return BaselineSelectView;
});