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
                "click a#collaborative_invite" : "invite",
                "click a.collaborative_kick": "kick",
                "click a.collaborative_give_hand": "giveHand",
                "click a.collaborative_withdraw_invitation": "withdrawInvitation",
                "click a#collaborative_exit": "exit"
            },

            template: Mustache.compile(template),

            initialize: function() {
            },

            render: function() {
                this.$el.html(this.template({master:this.master, roomKey:this.roomKey, users:this.users,pendingUsers:this.pendingUsers,i18n:i18n}));
                if (this.roomKey == null) {
                    this.$("a#collaborative_create").show();
                    this.$("table").hide();
                } else {
                    this.$("a#collaborative_create").hide();
                    this.$("table").show();
                    if (this.isMaster){
                        this.$("a.collaborative_kick").show();
                        this.$("a.collaborative_give_hand").show();
                        this.$("a.collaborative_withdraw_invitation").show();
                        this.$("a#collaborative_invite").show();
                    } else {
                        this.$("a.collaborative_kick").hide();
                        this.$("a.collaborative_give_hand").hide();
                        this.$("a.collaborative_withdraw_invitation").hide();
                        this.$("a#collaborative_invite").hide();
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
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_KICK_USER,
                    key: this.roomKey,
                    remoteUser: e.currentTarget.name
                });
            },

            withdrawInvitation: function(e){
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_WITHDRAW_INVITATION,
                    key: this.roomKey,
                    remoteUser: e.currentTarget.name,
                    messageBroadcast: {
                        context:APP_CONFIG.workspaceId+'/'+APP_CONFIG.productId
                    }
                });
            },

            giveHand: function(e){
                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_GIVE_HAND,
                    key: this.roomKey,
                    remoteUser: e.currentTarget.name
                });
                App.sceneManager.disableControlsObject();
                App.appView.setSpectatorView();
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
                        cameraInfos: App.sceneManager.getControlsContext()
                    }
                });

                mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_COMMANDS,
                    remoteUser: "",
                    key: this.roomKey,
                    messageBroadcast: {
                        smartPath: App.partsTreeView.getSmartPath()
                    }
                });

                App.sceneManager.sendEditedMeshes();

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
                this.isMaster = (this.master == APP_CONFIG.login);
                this.render();
            },

            setUsers: function(users) {
                this.users = users;
                this.render();
            },

            setPendingUsers: function(pendingUsers) {
                this.pendingUsers = pendingUsers;
                this.render();
            }
        });

        return CollaborativeView;
    }

);
