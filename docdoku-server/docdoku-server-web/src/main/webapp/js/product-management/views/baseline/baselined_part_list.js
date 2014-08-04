define([
    "views/baseline/baselined_part_list_item",
    "common-objects/models/baselined_part"
],function(BaselinedPartListItemView,BaselinedPart){

    var BaselinedPartsView = Backbone.View.extend({

        tagName:"ul",

        className:"baselined-parts",

        initialize:function(){
            this.isForBaseline = (this.options.isForBaseline) ? this.options.isForBaseline : false;
            this.isLocked = (this.options.isLocked) ? this.options.isLocked : false;
        },

        render:function(){
            this.initItemViews();
            return this;
        },

        initItemViews:function(){
            var that = this ;
            this.baselinedParts = [];
            this.baselinedPartsViews = [];

            this.$el.empty();

            if(this.model) {
                _.each(this.model.getBaselinedParts(), function (bpData) {
                    var baselinedPart = new BaselinedPart(bpData);
                    var data = {
                        model: baselinedPart
                    };
                    if (that.isForBaseline) {
                        data.released = that.model.isReleased();
                        data.isForBaseline = that.isForBaseline;
                    }else{
                        data.isLocked =that.isLocked;
                    }
                    var baselinedPartItemView = new BaselinedPartListItemView(data).render();
                    that.baselinedParts.push(baselinedPart);
                    that.baselinedPartsViews.push(baselinedPartItemView);
                    that.$el.append(baselinedPartItemView.$el);
                });
            }
        },

        getBaselinedParts:function(){
            var baselinedParts = [];
            _.each(this.baselinedParts,function(baselinedPart){
                if(!baselinedPart.isExcluded()){
                    baselinedParts.push({
                        number:baselinedPart.getNumber(),
                        version:baselinedPart.getVersion(),
                        iteration:baselinedPart.getIteration()
                    });
                }
            });
            return baselinedParts ;
        }
    });

    return BaselinedPartsView;
});