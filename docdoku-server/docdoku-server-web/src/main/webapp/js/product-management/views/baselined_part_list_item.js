define(["text!templates/baselined_part_list_item.html"],function(template){

    var BaselinedPartListItemView = Backbone.View.extend({

        tagName:"li",

        className:"baselined-part-item",

        events:{
            "change input[name=iteration]":"changeIteration"
        },

        template:Mustache.compile(template),

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({model:this.model}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
        },

        changeIteration:function(e){
            if(e.target.value){
                this.model.setIteration(e.target.value);
            }
        }

    });

    return BaselinedPartListItemView;

});