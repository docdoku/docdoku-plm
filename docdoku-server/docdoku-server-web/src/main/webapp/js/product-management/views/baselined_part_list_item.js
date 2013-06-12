define([
    "text!templates/baselined_part_list_item.html",
    "i18n!localization/nls/baseline-strings"
],function(template,i18n){

    var BaselinedPartListItemView = Backbone.View.extend({

        tagName:"li",

        className:"baselined-part-item",

        events:{
            "change input[name=iteration]":"changeIteration",
            "change input[name=exclude]":"excludePart"
        },

        template:Mustache.compile(template),

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({model:this.model,i18n:i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
        },

        changeIteration:function(e){
            if(e.target.value){
                this.model.setIteration(e.target.value);
            }
        },
        excludePart:function(e){
            if(e.target.checked){
                this.$el.css("opacity",0.5);
            }else{
                this.$el.css("opacity",1);
            }
            this.model.setExcluded(e.target.checked);
        }

    });

    return BaselinedPartListItemView;

});