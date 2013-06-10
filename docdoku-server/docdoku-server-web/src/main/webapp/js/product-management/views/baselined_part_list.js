define([
    "text!templates/baselined_part_list.html",
    "views/baselined_part_list_item",
    "common-objects/models/baselined_part"
],function(template,BaselinedPartListItemView,BaselinedPart){

    var BaselinedPartsView = Backbone.View.extend({

        tagName:"ul",

        className:"baselined-parts",

        template:Mustache.compile(template),

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template());
            this.initItemViews();
            return this;
        },

        initItemViews:function(){
            var that = this ;
            this.baselinedParts = [];
            this.baselinedPartsViews = [];

            this.$el.empty();

            _.each(this.model.getBaselinedParts(),function(bpData){
                var baselinedPart = new BaselinedPart(bpData);
                var baselinedPartItemView = new BaselinedPartListItemView({model:baselinedPart}).render();
                that.baselinedParts.push(baselinedPart);
                that.baselinedPartsViews.push(baselinedPartItemView);
                that.$el.append(baselinedPartItemView.$el);
            });
        },

        getBaselinedParts:function(){

            var baselinedParts = [];

            _.each(this.baselinedParts,function(baselinedPart){
                baselinedParts.push({
                    number:baselinedPart.getNumber(),
                    version:baselinedPart.getVersion(),
                    iteration:baselinedPart.getIteration()
                });
            });

            return baselinedParts ;

        }

    });

    return BaselinedPartsView;

});