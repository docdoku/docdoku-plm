'use strict';
define([
    "common-objects/collections/baselines",
    "views/baseline/baseline_list_item"
],function(Baselines,BaselineListItemView){

    var BaselineListView = Backbone.View.extend({

        tagName:"ul",

        className:"baselines-list",

        initialize:function(attributes,options){
            _.bindAll(this);
            this.productId = options.productId;
        },

        render:function(){
            this.collection = new Baselines({},{productId:this.productId});
            this.listenToOnce(this.collection,"reset",this.onCollectionReset);
            this.collection.fetch({reset:true});
            return this;
        },

        onCollectionReset:function(){
            var that = this ;
            this.$el.empty();
            this.subViews = [];

            this.collection.each(function(baseline){
                var view = new BaselineListItemView({model:baseline}).render();

                that.subViews.push(view);
                that.$el.append(view.$el);

                that.listenToOnce(view,"baseline:to-edit-modal",function(baseline){
                    that.trigger("baseline:to-edit-modal",baseline);
                });
            });
        },

        getCheckedBaselines:function(){
            var checkedBaselines = [];
            _.each(this.subViews,function(subView){
                if(subView.isChecked()){
                    checkedBaselines.push(subView.model);
                }
            });
            return checkedBaselines;
        }


    });

    return BaselineListView;
});