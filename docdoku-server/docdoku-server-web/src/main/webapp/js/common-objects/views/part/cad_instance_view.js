define(  [
    "text!common-objects/templates/part/cad_instance.html",
    "i18n!localization/nls/product-structure-strings"
],function(template, i18n) {

    var CadInstanceView = Backbone.View.extend({

        template: Mustache.compile(template),

        className:"cadInstance",

        events: {
            "change input[name=tx]":"changeTX",
            "change input[name=ty]":"changeTY",
            "change input[name=tz]":"changeTZ",
            "change input[name=rx]":"changeRX",
            "change input[name=ry]":"changeRY",
            "change input[name=rz]":"changeRZ",
            "change select[name=position]":"changePosition",
            "click .remove-cadInstance" : "removeCadInstance"
        },

        initialize: function() {
        },

        setInstance:function(instance){
            this.instance = instance;
            return this;
        },

        render: function() {
            this.$el.html(this.template({instance: this.instance, i18n:i18n}));
            return this;
        },

        changeTX:function(e){this.instance.tx= e.target.value;},
        changeTY:function(e){this.instance.ty= e.target.value;},
        changeTZ:function(e){this.instance.tz= e.target.value;},
        changeRX:function(e){this.instance.rx= e.target.value;},
        changeRY:function(e){this.instance.ry= e.target.value;},
        changeRZ:function(e){this.instance.rz= e.target.value;},

        removeCadInstance:function(){
            this.trigger("instance:remove");
            this.remove();
        }

    });

    return CadInstanceView;
});
