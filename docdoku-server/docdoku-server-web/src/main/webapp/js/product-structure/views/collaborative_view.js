/*global App,mainChannel,ChannelMessagesType*/
define(    [
        "views/select_participant_modal",
        "text!templates/collaborative_view.html",
        "i18n!localization/nls/product-structure-strings"
    ],

    function (SelectParticipantModalView, template, i18n) {

        var CollaborativeView = Backbone.View.extend({

            events: {
                "click a#collaborative_create" : "create",
                "click i#collaborative_invite" : "invite",
                "click i.collaborative_kick": "kick",
                "click i.collaborative_give_hand": "giveHand",
                "click i.collaborative_withdraw_invitation": "withdrawInvitation",
                "click i#collaborative_exit": "exit",
                "click li": "toggleExpand"
            },

            template: Mustache.compile(template),

            initialize: function() {
            },

            render: function() {
                this.$el.html(this.template({master:this.master, roomKey:this.roomKey, users:this.users,pendingUsers:this.pendingUsers,i18n:i18n}));
                if (this.roomKey == null) {
                    this.$("a#collaborative_create").show();
                    this.$("ul").hide();
                } else {
                    this.$("a#collaborative_create").hide();
                    this.$("ul").show();
                    if (this.isMaster){
                        App.appView.leaveSpectatorView();
                        App.sceneManager.enableControlsObject();
                        this.$("i#collaborative_invite").show();
                        this.reduceAll();
                    } else {
                        App.sceneManager.disableControlsObject();
                        App.appView.setSpectatorView();
                        this.$(".fa-chevron-right").hide();
                        this.$(".fa-chevron-left").hide();
                        this.$(".collaborative_kick").hide();
                        this.$(".collaborative_give_hand").hide();
                        this.$(".collaborative_withdraw_invitation").hide();
                        this.$("i#collaborative_invite").hide();
                        if (this.noMaster){
                            this.$("i#collaborative_master").removeClass("master");
                            this.$("i#collaborative_master").addClass("no-master");
                        }
                    }
                }
                return this;
            },
            create : function(){
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_CREATE,
                    remoteUser: ""
                });
            },

            invite : function(){
                var spmv = new SelectParticipantModalView();
                spmv.setRoomKey(this.roomKey);
                $("body").append(spmv.render().el);
                spmv.openModal();
            },

            kick: function(e){
                var name = e.currentTarget.parentElement.id.substr(12);
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_KICK_USER,
                    key: this.roomKey,
                    remoteUser: name
                });
            },

            withdrawInvitation: function(e){
                var name = e.currentTarget.parentElement.id.substr(12);
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_WITHDRAW_INVITATION,
                    key: this.roomKey,
                    remoteUser: name,
                    messageBroadcast: {
                        context:APP_CONFIG.workspaceId
                    }
                });
            },

            giveHand: function(e){
                var name = e.currentTarget.parentElement.id.substr(12);
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_GIVE_HAND,
                    key: this.roomKey,
                    remoteUser: name
                });
                this.setMaster(name);

               // App.sceneManager.disableControlsObject();
                //App.appView.setSpectatorView();
            },

            exit: function() {
                if (this.isMaster){
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_KILL,
                        key: this.roomKey,
                        remoteUser: "null"
                    });
                } else {
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_EXIT,
                        key: this.roomKey,
                        remoteUser: "null"
                    });
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                }
                this.reset();
            },

            roomCreated : function(key,master){
                this.setRoomKey(key);
                this.setMaster(master);

                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    remoteUser: "",
                    key: this.roomKey,
                    messageBroadcast: {
                        smartPath: App.partsTreeView.getSmartPath()
                    }
                });
                App.sceneManager.sendCameraInfos();
                App.sceneManager.sendEditedMeshes();
                App.sceneManager.sendColourEditedMeshes();
                App.sceneManager.sendExplodeValue(document.getElementById("slider-explode").value);

                window.location.hash = "room="+this.roomKey;

                this.invite();
            },

            reset: function(){
                this.setRoomKey(null);
                this.setMaster(null);
                this.setMaster(null);
                this.setUsers(null);
                this.setPendingUsers(null);
            },

            setRoomKey: function(key) {
                this.roomKey = key;
                this.render();
            },

            setMaster: function(master) {
                this.master = master;
                this.noMaster = false;
                this.isMaster = (this.master === APP_CONFIG.login);
                this.render();
            },

            setLastMaster: function(lastMaster) {
                this.master = lastMaster;
                this.noMaster = true;
                this.isMaster = (this.lastMaster === APP_CONFIG.login);
                this.render();
            },

            setUsers: function(users) {
                this.users = users;
                this.render();
            },

            setPendingUsers: function(pendingUsers) {
                this.pendingUsers = pendingUsers;
                this.render();
            },

            toggleExpand : function(e) {
                if (this.isMaster){
                    var el = e.currentTarget;
                    if (this.$(el).find(".fa-chevron-right").is(":visible")) {
                        this.expand(el);
                    } else {
                        this.reduce(el);
                    }
                }
            },

            expand : function(el){
                this.$(el).find(".fa-chevron-right").hide();
                this.$(el).find(".fa-chevron-left").show();
                this.$(el).find(".collaborative_give_hand").show();
                this.$(el).find(".collaborative_kick").show();
                this.$(el).find(".collaborative_withdraw_invitation").show();
            },

            reduce : function(el){
                this.$(el).find(".fa-chevron-right").show();
                this.$(el).find(".fa-chevron-left").hide();
                this.$(el).find(".collaborative_give_hand").hide();
                this.$(el).find(".collaborative_kick").hide();
                this.$(el).find(".collaborative_withdraw_invitation").hide();
            },

            reduceAll : function(){
                this.$(".fa-chevron-right").show();
                this.$(".fa-chevron-left").hide();
                this.$(".collaborative_give_hand").hide();
                this.$(".collaborative_kick").hide();
                this.$(".collaborative_withdraw_invitation").hide();
            }


        });

        return CollaborativeView;
    }

);
