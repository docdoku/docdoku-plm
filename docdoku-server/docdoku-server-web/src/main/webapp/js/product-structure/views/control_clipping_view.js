/*global App*/
define(
    [
        "text!templates/control_clipping.html",
        "i18n!localization/nls/product-structure-strings"
    ],
    function(template,i18n){

    var ControlOptionsView = Backbone.View.extend({

        template : Mustache.compile(template),

        className:"side_control_group",

        events:{
            "slide .slider":"clipping"
        },

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));

            this.slider = this.$('.slider');
            this.slider.noUiSlider({
                start: [ 1, 1e5 ],
                range: {
                    'min': 1,
                    'max': 1e5
                }
            });

            return this;
        },

        clipping:function(e){
            var values = this.slider.val();
            var near = parseFloat(values[0])/10 +1;
            var far = parseFloat(values[1]);
            App.sceneManager.setCameraNearFar(near,far);
        }

    });

    return ControlOptionsView;

});