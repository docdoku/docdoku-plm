define(
    [
        "text!modules/coworkers-access-module/templates/coworker_item_template.html",
        "i18n!localization/nls/coworkers-access-module-strings"
    ],

    function(template, i18n){

        CoWorkersItemView = Backbone.View.extend({

            tagName : 'li',

            events : {
                "click .icon-facetime-video" : "onVideoButtonClick",
                "click .icon-leaf" : "onChatButtonClick",
                "click .icon-envelope" : "onMailButtonClick"
            },

            initialize:function(){
                this.template =  Mustache.render(template, {user : this.model});
                _.bindAll(this);
                return this ;
            },

            render:function(){
                $(this.el).html(this.template);
                return this ;
            },

            refreshAvailability:function(){
                var that = this ;
                // Listen for the status request done
                Backbone.Events.on('UserStatusRequestDone', function(message){
                    if(message.remoteUser == that.model.login && message.status != null){
                        if(message.status == "OFFLINE"){
                            that.$(".icon-user").css("opacity",0.5).attr("title",i18n.OFFLINE);
                        }else if(message.status == "ONLINE"){
                            that.$(".icon-user").css("opacity",1).attr("title",i18n.ONLINE);
                        }
                    }
                });
                // trigger the status request
                Backbone.Events.trigger('UserStatusRequest', that.model.login);
            },

            onVideoButtonClick:function(e){
                Backbone.Events.trigger('NewWebRTCSession', { remoteUser : this.model.login , context: APP_CONFIG.workspaceId });
            },

            onChatButtonClick:function(e){
                Backbone.Events.trigger('NewChatSession', { remoteUser : this.model.login , context: APP_CONFIG.workspaceId });
            },

            onMailButtonClick:function(e){
                window.location.href = encodeURI("mailto:"+this.model.email + "?subject="+APP_CONFIG.workspaceId);
            }

        });

        return CoWorkersItemView;

    });