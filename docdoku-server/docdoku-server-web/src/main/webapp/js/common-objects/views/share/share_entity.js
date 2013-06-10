define(
    [
        "text!common-objects/templates/share/share_entity.html",
        "text!common-objects/templates/share/shared_entity.html",
        "i18n!localization/nls/share-strings"
    ],
    function(template, templateShared, i18n) {

    var ShareView = Backbone.View.extend({

        tagName: 'div',

        template : Mustache.compile(template),

        templateShared : Mustache.compile(templateShared),

        events: {
            "click #generate-private-share":"createShare"
        },

        initialize: function() {
        },

        render:function(){
            var that = this ;
            var title = "";

            switch(this.options.entityType){
                case "documents" :
                    title = i18n.SHARED_DOCUMENTS_TITLE + " : " + this.model.getReference() + "-" + this.model.getVersion();
                break;
                case "parts" :
                    title = i18n.SHARED_PARTS_TITLE + " : " + this.model.getNumber() + "-" + this.model.getVersion();
                break;
                default : break;
            }

            this.$el.html(this.template({i18n:i18n, title:title, permalink:this.model.getPermalink()}));
            this.bindDomElements();

            this.$badPasswordLabel.hide();

            this.$publicSharedSwitch.bootstrapSwitch();
            this.$publicSharedSwitch.bootstrapSwitch('setState', this.model.get("publicShared"));

            this.$publicSharedSwitch.on('switch-change', function (e, data) {
                if(that.model.get("publicShared")){
                    that.model.unpublish({success:function(){
                        that.model.fetch();
                    }});
                }
                else {
                    that.model.publish({success:function(){
                        that.model.fetch();
                    }});
                }
            });


            return this;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        },

        bindDomElements:function(){
            this.$modal = this.$('#share-modal');
            this.$password = this.$('.password');
            this.$confirmPassword = this.$('.confirm-password');
            this.$badPasswordLabel = this.$('.help-block');
            this.$passwordControl = this.$('#password-control');
            this.$expireDate = this.$('.expire-date');
            this.$publicSharedSwitch = this.$(".public-shared-switch");
            this.$privateShare = this.$("#private-share");
        },

        setEntityType:function(entityType){
            this.entityType = entityType;
        },

        createShare:function(e){
            var that = this;
            var data = {};
            switch(this.options.entityType){
                case "documents" :
                    data = {documentMasterId: this.model.getReference(), documentMasterVersion:this.model.getVersion()};
                    break;
                case "parts" :
                    data = {partMasterNumber: this.model.getNumber(), partMasterVersion:this.model.getVersion()};
                    break;
                default : break;
            }

            if(this.$password.val()!==this.$confirmPassword.val()) {
                this.$passwordControl.addClass("error");
                this.$badPasswordLabel.show();
            } else {
                data.password=this.$password.val() ? this.$password.val():null;
                data.expireDate=this.$expireDate.val() ? this.$expireDate.val():null;

                this.model.createShare({data:data,success:function(pData){
                    that.$privateShare.empty();
                    that.$privateShare.html(that.templateShared({i18n:i18n,generatedUrl:that.generateUrlFromUUID(pData.uuid)}));
                }});
            }

        },

        generateUrlFromUUID:function(uuid){
            return window.location.origin+"/shared/"+uuid;
        }

    });
    return ShareView;
});