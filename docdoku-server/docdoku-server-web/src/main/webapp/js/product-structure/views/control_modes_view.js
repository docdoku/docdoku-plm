/*global App*/
define(
    [
        "views/select_participant_modal",
        "text!templates/control_modes.html",
        "i18n!localization/nls/product-structure-strings"
    ],function(SelectParticipantModalView, template, i18n){

    var ControlModesView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click button#flying_mode_view_btn": "flyingView",
            "click button#tracking_mode_view_btn": "trackingView",
            "click button#orbit_mode_view_btn": "orbitView",
            "click a#share_view":"selectUser",
            "click a#stop_collaborative": "stopCollaborativeMaster"
        },

        flyingView:function(){
            App.sceneManager.setPointerLockControls();
        },

        trackingView:function(){
            App.sceneManager.setTrackBallControls();
        },

        orbitView:function(){
            App.sceneManager.setOrbitControls();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            this.$("#stop_collaborative").hide();
            //this.shortcutsview = new ShortcutsView().render();
            //this.$(".nav-header").after(this.shortcutsview.$el);
            return this;
        },

        selectUser:function(){
            var spmv = new SelectParticipantModalView();
            $("body").append(spmv.render().el);
            spmv.openModal();
            this.$("#stop_collaborative").show();
        },

        stopCollaborativeMaster:function(){
            //this.$ControlsContainer.find("button").attr("disabled","false");
            App.sceneManager.stopCollaborativeMaster();
            this.$("#stop_collaborative").hide();
        }




    });

    return ControlModesView;

});