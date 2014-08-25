define([
    "models/part_template",
    "text!templates/part_template_list_item.html"
], function (
    PartTemplate,
    template
    ) {
    var PartTemplateListItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click input[type=checkbox]":"selectionChanged",
            "click td.reference":"toPartTemplateEditModal"
        },

        tagName:"tr",

        initialize: function () {
            _.bindAll(this);
            this._isChecked = false ;
            this.listenTo(this.model,"change",this.render);
        },

        render:function(){
            this.$el.html(this.template(this.model));
            this.$checkbox = this.$("input[type=checkbox]");
            if(this.isChecked()){
                this.check();
                this.trigger("selectionChanged",this);
            }
            this.bindUserPopover();
            this.trigger("rendered",this);
            return this;
        },

        selectionChanged:function(){
            this._isChecked = this.$checkbox.prop("checked");
            this.trigger("selectionChanged",this);
        },

        isChecked:function(){
            return this._isChecked;
        },

        check:function(){
            this.$checkbox.prop("checked", true);
            this._isChecked = true;
        },

        unCheck:function(){
            this.$checkbox.prop("checked", false);
            this._isChecked = false;
        },

        bindUserPopover:function(){
            this.$(".author-popover").userPopover(this.model.getAuthorLogin(),this.model.getId(),"left");
        },

        toPartTemplateEditModal:function(){
            var that = this;
            require(["views/part_template_edit_view"],function(PartTemplateEditView){
                var partTemplateEditView = new PartTemplateEditView({model:that.model}).render();
                partTemplateEditView.show();
            });
        }

    });

    return PartTemplateListItemView;

});
