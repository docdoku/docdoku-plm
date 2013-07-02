/*global sceneManager*/
define(
    [
        "views/shortcuts_view",
        "text!templates/control_modes.html",
        "i18n!localization/nls/product-structure-strings"
    ],function(ShortcutsView, template, i18n){

    var ControlModesView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click button#flying_mode_view_btn": "flyingView",
            "click button#tracking_mode_view_btn": "trackingView"
        },

        initialize:function(){
        },

        flyingView:function(){
            sceneManager.$blocker.show();
            sceneManager.updateNewCamera();
            sceneManager.updateLayersManager();
            sceneManager.setPointerLockControls();
        },

        trackingView:function(){
            sceneManager.$blocker.hide();
            sceneManager.updateNewCamera();
            sceneManager.updateLayersManager();
            sceneManager.setTrackBallControls();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.shortcutsview = new ShortcutsView().render();
            this.$(".nav-header").after(this.shortcutsview.$el);
            return this;
        }


    });

    return ControlModesView;

});