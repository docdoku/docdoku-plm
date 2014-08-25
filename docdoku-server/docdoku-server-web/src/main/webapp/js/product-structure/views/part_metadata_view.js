'use strict';
define (['text!templates/part_meta_data.html','i18n!localization/nls/product-structure-strings'],function(template,i18n) {
    var PartMetadataView = Backbone.View.extend({

        tagName:'div',

        id:'part_metadata_container',

        template: Mustache.compile(template),

        events:{
            'click .author-join':'authorClicked'
        },

        className:'side_control_group',

        initialize: function() {
            this.listenTo(this.model, 'change' , this.render);
        },

        setModel:function(model){
            this.model=model;
            return this;
        },

        render: function() {
            var permalink = this.model.getPermalink ? this.model.getPermalink() : ('/parts/' + APP_CONFIG.workspaceId + '/' + this.model.getNumber() + '/' + this.model.getVersion());
            this.$el.html(this.template({model:this.model, i18n:i18n, permalink:permalink}));
            return this;
        },

        authorClicked:function(){
            if(this.model.getAuthorLogin() !== APP_CONFIG.login) {
                Backbone.Events.trigger('NewChatSession',{remoteUser:this.model.getAuthorLogin(),context:this.model.getNumber()});
            }
        },

        reset:function(){
            this.$el.empty();
        }
        
    });

    return PartMetadataView;
});