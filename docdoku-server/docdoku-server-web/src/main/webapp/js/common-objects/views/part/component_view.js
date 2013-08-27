define(  [
    "text!common-objects/templates/part/component_view.html",
    "common-objects/views/part/cad_instance_view",
    "i18n!localization/nls/product-structure-strings"
],function(template, CadInstanceView, i18n) {

    var ComponentView = Backbone.View.extend({

        template: Mustache.compile(template),

        events: {
            "click a.remove":"onRemove",
            "change input[name=amount]":"changeAmount",
            "change input[name=number]":"changeNumber",
            "change input[name=name]":"changeName",
            "click .add-cadInstance" :"addCadInstance"
        },

        initialize: function() {
        },

        render: function() {
            this.$el.html(this.template({model: this.model.attributes, i18n:i18n, editMode:this.options.editMode}));
            this.bindDomElements();
            this.initCadInstanceViews();
            return this;
        },

        bindDomElements:function(){
            this.$cadInstances = this.$(".cadInstances");
            this.$amount = this.$("input[name=amount]");
        },

        initCadInstanceViews:function(){
            var self = this ;
            _(this.model.get("cadInstances")).each(function(instance){
                self.addCadInstanceView(instance);
            });
        },

        addCadInstanceView:function(instance){
            var self = this ;
            var instanceView = new CadInstanceView();
            instanceView.setInstance(instance).render();
            self.$cadInstances.append(instanceView.$el);
            instanceView.on("instance:remove",function(){
                self.onRemoveCadInstance(instance);
            })
        },

        onRemove : function(){
            if(this.options.removeHandler && this.options.editMode){
                this.options.removeHandler();
            }
        },

        onRemoveCadInstance:function(instance){
            this.model.set("cadInstances", _(this.model.get("cadInstances")).without(instance));
            this.$amount.val(parseInt(this.$amount.val())-1);
        },

        addCadInstance:function(){
            var instance = {tx:0,ty:0,tz:0,rx:0,ry:0,rz:0,positioning:"ABSOLUTE"};
            this.model.get("cadInstances").push(instance);
            this.addCadInstanceView(instance);
            this.$amount.val(parseInt(this.$amount.val())+1);
        },

        changeNumber:function(e){
            this.model.get("component").number =  e.target.value;
        },
        changeName:function(e){
            this.model.get("component").name =  e.target.value;
        }


    });

    return ComponentView;
});
