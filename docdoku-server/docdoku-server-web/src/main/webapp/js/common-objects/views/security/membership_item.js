define([
    "text!common-objects/templates/security/membership_item.html",
    "i18n!localization/nls/security-strings"
], function (
    template,
    i18n
    ) {
    var MembershipItemView = Backbone.View.extend({

        template: Mustache.compile(template),

        events:{
            "change input[type=radio]":"change"
        },

        initialize: function() {
            _.bindAll(this);
        },

        change:function(e){
            this.model.set("permission", e.target.value);
        },

        render:function(){
            var permission = i18n[this.model.getPermission()];
            this.$el.html(this.template({membership:this.model,i18n:i18n, editMode:this.options.editMode,permission:permission,radioName:this.model.key() + "-radio-"+this.cid}));
            return this;
        }

    });

    return MembershipItemView;
});
