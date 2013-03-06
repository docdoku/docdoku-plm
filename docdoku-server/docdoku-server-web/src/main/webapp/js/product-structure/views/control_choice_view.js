/*global sceneManager*/
define(["text!templates/control_choice.html","i18n!localization/nls/product-structure-strings"],function(template,i18n){

    var ControlChoiceView = Backbone.View.extend({

        template : Mustache.compile(template),

        events:{
            "click button#flying_mode_view_btn": "flyingView",
            "click button#tracking_mode_view_btn": "trackingView"
        },

        initialize:function(){
        },

        flyingView:function(){
            sceneManager.$blocker.show();
            sceneManager.updateNewCamera();
            sceneManager.setPointerLockControls();
            sceneManager.updateLayersManager();
        },

        trackingView:function(){
            sceneManager.$blocker.hide();
            sceneManager.updateNewCamera();
            sceneManager.setTrackBallControls();
            sceneManager.updateLayersManager();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }


    });

    return ControlChoiceView;

});