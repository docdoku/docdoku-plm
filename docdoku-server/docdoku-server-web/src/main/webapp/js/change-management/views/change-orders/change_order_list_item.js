define([
    "text!templates/change-orders/change_order_list_item.html",
    "i18n!localization/nls/change-management-strings"
], function (
    template,
    i18n
    ) {
    var ChangeOrderListItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "click input[type=checkbox]":"selectionChanged",
            "click td.change_order_name":"openEditionView"
        },

        tagName:"tr",

        initialize: function () {
            this._isChecked = false ;
            this.listenTo(this.model, 'change', this.render);
        },

        render:function(){
            this.$el.html(this.template({model:this.model, i18n:i18n}));
            this.$checkbox = this.$("input[type=checkbox]");
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
            this.trigger("selectionChanged",this);
        },

        unCheck:function(){
            this.$checkbox.prop("checked", false);
            this._isChecked = false;
            this.trigger("selectionChanged",this);
        },

        openEditionView:function(){
            var that = this;
            this.model.fetch();
            require(["views/change-orders/change_order_edition"],function(ChangeOrderEditionView){
                var editionView = new ChangeOrderEditionView({
                    collection:that.collection,
                    model:that.model
                });
                $("body").append(editionView.render().el);
                editionView.openModal();
            });
        }
    });

    return ChangeOrderListItemView;
});