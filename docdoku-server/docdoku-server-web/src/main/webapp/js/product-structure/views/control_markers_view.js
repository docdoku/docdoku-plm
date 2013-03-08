/*global sceneManager*/
define(["text!templates/control_markers.html","i18n!localization/nls/product-structure-strings"],function(template,i18n){

    var ControlMarkersView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "click button#markerZoomLess": "markerZoomLess",
            "click button#markerState": "markerState",
            "click button#markerZoomMore": "markerZoomMore"
        },

        initialize:function(){
        },

        markerZoomLess:function(){
        },
        markerState:function(){
        },
        markerZoomMore:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }


    });

    return ControlMarkersView;

});