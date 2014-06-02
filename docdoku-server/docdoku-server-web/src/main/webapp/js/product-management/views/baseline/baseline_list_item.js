define(["text!templates/baseline/baseline_list_item.html"],function(template){

    var BaselineItemView = Backbone.View.extend({

        tagName:"li",

        className:"baseline-item",

        events:{
            "change input[type=checkbox]":"toggleStroke",
            "click a":"toBaselineEditView"
        },

        template:Mustache.compile(template),

        render:function(){
            this.$el.html(this.template({model:this.model}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$a = this.$("a");
            this.$checkbox = this.$("input[type=checkbox]");
        },

        toggleStroke:function(){
            this.$a.toggleClass("stroke");
        },

        isChecked:function(){
            return this.$checkbox.is(":checked");
        },

        toBaselineEditView:function(){
            this.trigger("baseline:to-edit-modal",this.model);
        }

    });

    return BaselineItemView;

});