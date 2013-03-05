/*global sceneManager*/
define(
    [
        "text!templates/shortcuts.html",
        "views/controls_infos_modal_view",
        "i18n!localization/nls/scene-strings"
    ]
    ,function(template,ControlsInfosModalView,i18n){

    var ShortcutsView = Backbone.View.extend({

        tagName:"div",

        id:"shortcuts",

        template:Mustache.compile(template),

        events :{
            "click a":"clicked"
        },

        initialize:function(){

        },

        clicked:function(){
            var cimv;
            switch (sceneManager.stateControl) {
                case sceneManager.STATECONTROL.PLC:
                    cimv = new ControlsInfosModalView({isPLC:true, isTBC:false});
                    break;
                case sceneManager.STATECONTROL.TBC:
                    cimv = new ControlsInfosModalView({isPLC:false, isTBC:true});
                    break;
            }
            $("body").append(cimv.render().el);
            cimv.openModal();
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }

    });

    return ShortcutsView;

});