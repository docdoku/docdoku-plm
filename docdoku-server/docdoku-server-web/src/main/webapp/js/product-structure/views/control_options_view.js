/*global sceneManager*/
define(
    [
        "text!templates/control_options.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var ControlOptionsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click button#gridSwitch": "gridSwitch",
            "click button#materialsSwitch": "materialsSwitch"
        },

        initialize:function(){

        },

        gridSwitch:function(){
            $("#gridSwitch").toggleClass("active");
            if ($("#gridSwitch").hasClass("active")) {
                sceneManager.showGrid();
            } else {
                sceneManager.removeGrid();
            }
        },

        materialsSwitch:function(){
            $("#materialsSwitch").toggleClass("active");
            if ($("#materialsSwitch").hasClass("active")) {
                sceneManager.switchWireFrame(true);
            } else {
                sceneManager.switchWireFrame(false);
            }
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }


    });

    return ControlOptionsView;

});