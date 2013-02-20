/*global mainChannel*/
define(["i18n!localization/nls/chat-module-strings",
        "modules/chat-module/collections/chat_message_collection",
        "modules/chat-module/views/chat_message_view"],
    function(i18n,ChatMessageCollection,ChatMessageView){

    var ChatMessageSessionView = Backbone.View.extend({

        template: _.template(
            "<div class='chat_session'>"
            +"<div class='chat_session_header'>"
            +"<span class='chat_session_title'><i class='icon-comments'></i>  <%= chatSession.remoteUser %> | <%= chatSession.context %></span>"
            +"<i class='icon-remove'></i>"
            +"<i class='icon-facetime-video'></i>"
            +"</div>"
            +"<ul class='chat_session_messages'></ul>"
            +"<div class='chat_webrtc_invite'>"
                +"<span><b></b><%= chatSession.remoteUser %></b> "+i18n.VIDEO_INVITE_TEXT+"</span>"
                +"<a class='btn btn btn-success accept-webrtc'> <i class='icon-facetime-video'></i> "+i18n.VIDEO_INVITE_ACCEPT+"</a><a class='btn btn-danger reject-webrtc'> <i class='icon-remove'></i> "+i18n.VIDEO_INVITE_REJECT+"</a></div>"
            +"<div class='chat_session_reply'>"
                +"<form class='chat_session_reply_form'>"
                    +"<input type='text' name='chat_message_session_input'/> "
                    +"<input class='btn btn-primary' type='submit' value='"+i18n.SEND+"'>"
                +"</form>"
            +"</div>"
            +"</div>"
        ),

        events : {
            "submit form" : "onSubmitForm",
            "click .icon-remove" : "onClose",
            "click .icon-facetime-video" : "onVideoButtonClick",
            "click .chat_session_title":"onGlobalClick",
            "click .chat_session_messages":"onGlobalClick",
            "click input[name=chat_message_session_input]":"onGlobalClick",
            "click a.accept-webrtc" : "onWebRTCAccept",
            "click a.reject-webrtc" : "onWebRTCReject"
        },

        initialize : function() {

            this.isOnTop = false;

            this._messagesViews = [];

            this.collection = new ChatMessageCollection();

            this.collection.bind('add', this.add, this);

            _(this).bindAll("onSubmitForm","onClose","onGlobalClick","onVideoButtonClick");

        },

        add : function(chatMessage) {

            var that = this ;

            var cmv = new ChatMessageView({
                model : chatMessage
            });

            this._messagesViews.push(cmv);

            if (this._rendered) {
                this.$el.show();

                // append new message
                var $ul = this.$("ul.chat_session_messages") ;
                $ul.append($(cmv.render().el));
                $ul[0].scrollTop = $ul[0].scrollHeight;

                // stick to bottom if needed
                var thatElHeight = that.$el.height();
                var offsetTop = that.$el.offset().top;
                var windowHeight = $(window).height();

                if(windowHeight - thatElHeight - offsetTop < 15){
                    this.$el.css({"bottom":"0","top":"auto"});
                }
            }

        },

        refreshTitle:function(){
            this.$(".chat_session_title").html("<i class='icon-envelope'></i> "+this.remoteUser + " | " + this.context);
            return this;
        },

        onNewChatMessage:function(message){
            if(message.error){
                this.onError(message);
            }else{
                this.collection.push(message);

                if(message.sender != APP_CONFIG.login){
                    Backbone.Events.trigger("NotificationSound");
                }

            }
            return this;
        },

        setRemoteUser : function(remoteUser){
            this.remoteUser = remoteUser ;
            return this ;
        },

        setContext : function(context){
            this.context = context ;
            return this ;
        },

        render : function() {

            var that = this ;

            this.$el = $(this.template({
                chatSession : {
                    remoteUser : this.remoteUser,
                    context: this.context
                }
            }));

            this._rendered = true;
            this.delegateEvents();

            this.$el.draggable({
                handle: "div.chat_session_header" ,
                containment: 'body',
                position:"absolute",
                addClasses: false ,
                start:function( event, ui ){
                    that.$el.css("bottom","auto");
                }
            });

            // override jquery style, better behavior
            this.$el.css("position","absolute");

            this.$WebRTCInvitation = this.$(".chat_webrtc_invite");

            return this;
        },

        onSubmitForm:function(event){

            if(!mainChannel.isReady()) {
                this.$("ul.chat_session_messages").append("<li class='chat_message_error'>"+i18n.ERROR+" : "+i18n.CHANNEL_NOT_READY_ERROR+"</li>");
                event.stopPropagation();
                event.preventDefault();
                return false;
            }

            var textInput = event.target.children[0];

            if(!textInput.value.length){
                return false;
            }

            // build the message
            var message = {
                type: ChannelMessagesType.CHAT_MESSAGE,
                remoteUser: this.remoteUser,
                message: textInput.value,
                context: this.context,
                sender:APP_CONFIG.login
            };

            // send it to remote
            mainChannel.sendJSON(message);

            // trigger message render on local
            //Backbone.Events.trigger('NewChatMessage', message);

            textInput.value = "";

            event.stopPropagation();
            event.preventDefault();
            return false ;

        },

        onClose:function(){
            this.$el.hide();
        },

        onError:function(message){
            if (this._rendered) {
                this.$("ul.chat_session_messages").append("<li class='chat_message_error'>"+i18n.ERROR+" : "+i18n[message.error]+"</li>");
                this.$el.show();
            }
        },

        focusInput:function(){
            this.$("input[name=chat_message_session_input]").focus();
            return this;
        },

        onGlobalClick:function(e){
            if(!this.isOnTop){
                Backbone.Events.trigger("ChatSessionFocusRequired",this);
            }
        },

        onVideoButtonClick:function(){
            Backbone.Events.trigger('NewOutgoingCall', {remoteUser:this.remoteUser,context:this.context});
        },

        setOnTop:function(){
            this.$el.addClass("chat_session_on_top");
            this.isOnTop = true ;
            this.focusInput();
            return this;
        },

        removeFromTop:function(){
            this.$el.removeClass("chat_session_on_top");
            this.isOnTop = false;
            return this;
        },

        onWebRTCInvitation:function(webRTCInvitation){
            this.webRTCInvitation = webRTCInvitation;
            this.$WebRTCInvitation.show();
        },

        onWebRTCAccept:function(){
            if(this.webRTCInvitation){
                this.webRTCInvitation.accept();
                this.webRTCInvitation = null;
                this.hideWebRTCInvitation();
            }
        },

        onWebRTCReject:function(){
            if(this.webRTCInvitation){
                this.webRTCInvitation.reject();
                this.webRTCInvitation = null;
                this.hideWebRTCInvitation();
            }
        },

        hideWebRTCInvitation:function(){
            this.$WebRTCInvitation.hide();
        }

    });

    return ChatMessageSessionView;
});