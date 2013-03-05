define(
    [
        "text!templates/blocker.html",
        "i18n!localization/nls/scene-strings"
    ],function(template,i18n){

    var BlockerView = Backbone.View.extend({

        tagName:"div",

        id:"blocker",

        template:Mustache.compile(template),

        initialize:function(){
        },

        render:function(){
            this.$el.html(this.template({i18n:i18n}));
            return this;
        }

    });

    return BlockerView;

});