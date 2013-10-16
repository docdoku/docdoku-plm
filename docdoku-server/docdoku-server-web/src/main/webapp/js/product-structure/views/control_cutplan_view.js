/*global sceneManager*/
define(
    [
        "text!templates/control_cutplan.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var CutPlanOptionsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group cut_plan_control disabled",

        events:{
            "change input#slider-cut-plan":"moveCutPlan",
            "click .change-axis " : "changeAxis"
        },

        initialize:function(){
            _.bindAll(this);
            this.axis = "X";
            this.onScene = false;
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.bindDomElements();
            this.$planSwitch.bootstrapSwitch();
            this.$planSwitch.bootstrapSwitch('setState', false);
            this.$planSwitch.on('switch-change',this.switchPlanVisibility);
            return this;
        },

        bindDomElements:function(){
            this.$planSwitch = this.$(".plan-switch");
            this.$slider = this.$("#slider-cut-plan");
        },


        moveCutPlan:function(e){
            if(this.onScene){
                sceneManager.moveCutPlan(this.axis,e.target.value);
            }
            else{
                e.preventDefault();
                e.stopPropagation();
                return false;
            }

        },

        changeAxis:function(e){
            this.$slider.val(0);
            this.axis = $(e.target).data("axis");
            if(this.onScene){
                sceneManager.setCutPlanAxis(this.axis);
            }
            else{
                e.preventDefault();
                e.stopPropagation();
                return false;
            }
        },

        switchPlanVisibility:function (e, data) {
            this.onScene = data.value;
            this.$el.toggleClass("disabled");
            if(data.value){
                sceneManager.showCutPlan();
            }else{
                sceneManager.removeCutPlan();
            }
        }

    });

    return CutPlanOptionsView;

});