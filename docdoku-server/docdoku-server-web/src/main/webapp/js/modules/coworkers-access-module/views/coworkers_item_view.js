define([
    "text!modules/coworkers-access-module/templates/coworker_item_template.html",
    "i18n!localization/nls/coworkers-access-module-strings"
],function(template, i18n){

        var CoWorkersItemView = Backbone.View.extend({

            tagName : 'li',

            events : {
                "click .fa-video-camera" : "onVideoButtonClick",
                "click .fa-comments" : "onChatButtonClick",
                "click .fa-envelope" : "onMailButtonClick",
                "click .fa-globe" : "onCobrowsingButtonClick"
            },

            initialize:function(){
                var data;
                if (typeof(App) !== 'undefined' && App.sceneManager) {
                    data = {
                        user: this.model.login,
                        displayCobrowsingButton: this.model.workspaceId === APP_CONFIG.workspaceId
                    };
                } else {
                    data = {
                        user: this.model.login
                    };
                }
                this.template =  Mustache.render(template, data);
                _.bindAll(this);
                //Backbone.Events.on('EnableCollaborativeInvite',this.collaborativeInvite);
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
                            that.$(".fa-user").addClass("user-offline").removeClass("user-online").attr("title",i18n.OFFLINE);
                        }else if(message.status == "ONLINE"){
                            that.$(".fa-user").addClass("user-online").removeClass("user-offline").attr("title",i18n.ONLINE);
                        }
                    }
                });
                // trigger the status request
                Backbone.Events.trigger('UserStatusRequest', that.model.login);
            },

            onVideoButtonClick:function(){
                Backbone.Events.trigger('NewOutgoingCall', { remoteUser : this.model.login , context: APP_CONFIG.workspaceId });
            },

            onChatButtonClick:function(){
                Backbone.Events.trigger('NewChatSession', { remoteUser : this.model.login , context: APP_CONFIG.workspaceId });
            },

            onMailButtonClick:function(){
                window.location.href = encodeURI("mailto:"+this.model.email + "?subject="+APP_CONFIG.workspaceId);
            },

            onCobrowsingButtonClick:function(e){
                Backbone.Events.trigger('SendCollaborativeInvite',this.model.login);
            },

            collaborativeInvite:function(){
                /*var data = {
                    user :  this.model.login,
                    displayCobrowsingButton : this.model.workspaceId === APP_CONFIG.workspaceId
                };
                this.template =  Mustache.render(template, data);
                this.render();*/
            }

        });

        return CoWorkersItemView;

    });