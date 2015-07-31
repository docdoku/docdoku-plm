/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/collaborative_view.html',
    'common-objects/websocket/channelMessagesType'
], function (Backbone, Mustache, template, ChannelMessagesType) {
	'use strict';
    var CollaborativeView = Backbone.View.extend({

        events: {
            'click a#collaborative_create': 'create',
            'click i.collaborative_kick': 'kick',
            'click i.collaborative_give_hand': 'giveHand',
            'click i.collaborative_withdraw_invitation': 'withdrawInvitation',
            'click i#collaborative_exit': 'exit',
            'click li': 'toggleExpandCommandsOnParticipant'
        },


        initialize: function () {
            Backbone.Events.on('SendCollaborativeInvite', this.sendInvite, this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {master: this.master, roomKey: this.roomKey, users: this.users, pendingUsers: this.pendingUsers, i18n: App.config.i18n}));
            if (!this.roomKey) {
                this.$('a#collaborative_create').show();
                this.$('#collaborative_room').hide();
            } else {
                this.$('a#collaborative_create').hide();
                this.$('#collaborative_room').show();
                if (this.isMaster) {
                    App.headerView.removeActionDisabled();
                    App.appView.leaveSpectatorView();
                    App.sceneManager.enableControlsObject();
                    this.reduceAllCommands();
                } else {
                    App.headerView.addActionDisabled();
                    App.sceneManager.disableControlsObject();
                    App.appView.setSpectatorView();
                    this.$('.fa-chevron-right').hide();
                    this.$('.fa-chevron-left').hide();
                    this.$('.collaborative_kick').hide();
                    this.$('.collaborative_give_hand').hide();
                    this.$('.collaborative_withdraw_invitation').hide();
                    if (this.noMaster) {
                        this.$('i#collaborative_master').removeClass('master');
                        this.$('i#collaborative_master').addClass('no-master');
                    }
                }
            }
            return this;
        },
        create: function () {
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_CREATE,
                remoteUser: ''
            });
        },

        sendInvite: function (user) {
            if (this.isMaster) {
                App.mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_INVITE,
                    key: this.roomKey,
                    messageBroadcast: {
                        url: '#' + App.config.workspaceId + '/' + App.config.productId + '/config-spec/' + App.config.productConfigSpec,
                        context: App.config.workspaceId
                    },
                    remoteUser: user
                });
            }
        },

        invite: function () {
            Backbone.Events.trigger('collaboration:invite');
        },

        kick: function (e) {
            var name = e.currentTarget.parentElement.id.substr(12);
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_KICK_USER,
                key: this.roomKey,
                remoteUser: name
            });
        },

        withdrawInvitation: function (e) {
            var name = e.currentTarget.parentElement.id.substr(12);
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_WITHDRAW_INVITATION,
                key: this.roomKey,
                remoteUser: name,
                messageBroadcast: {
                    context: App.config.workspaceId
                }
            });
        },

        giveHand: function (e) {
            var name = e.currentTarget.parentElement.id.substr(12);
            App.mainChannel.sendJSON({
                type: ChannelMessagesType.COLLABORATIVE_GIVE_HAND,
                key: this.roomKey,
                remoteUser: name
            });
            this.setMaster(name);

            //App.sceneManager.disableControlsObject();
            //App.appView.setSpectatorView();
        },

        exit: function () {
            if (this.isMaster) {
                App.mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_KILL,
                    key: this.roomKey,
                    remoteUser: 'null'
                });
            } else {
                App.mainChannel.sendJSON({
                    type: ChannelMessagesType.COLLABORATIVE_EXIT,
                    key: this.roomKey,
                    remoteUser: 'null'
                });
                App.appView.leaveSpectatorView();
                App.sceneManager.enableControlsObject();
            }
            this.reset();
        },

        roomCreated: function (key, master) {
            this.setRoomKey(key);
            this.setMaster(master);

			App.collaborativeController.sendSmartPath(App.partsTreeView.getSmartPath());
            App.collaborativeController.sendCameraInfos();
            App.collaborativeController.sendEditedObjects();
            App.collaborativeController.sendColourEditedObjects();
            App.collaborativeController.sendExplodeValue(App.$ControlsContainer.find('#slider-explode').val());

            window.location.hash = [App.config.workspaceId , App.config.productId, 'config-spec', App.config.productConfigSpec, 'room', this.roomKey].join('/');

            this.invite();
        },

        reset: function () {
            this.setRoomKey(null);
            this.setMaster(null);
            this.setMaster(null);
            this.setUsers(null);
            this.setPendingUsers(null);
        },

        setRoomKey: function (key) {
            this.roomKey = key;
            this.render();
        },

        setMaster: function (master) {
            this.master = master;
            this.noMaster = false;
            this.isMaster = (this.master === App.config.login);
            this.render();
        },

        setLastMaster: function (lastMaster) {
            this.master = lastMaster;
            this.noMaster = true;
            this.isMaster = (this.lastMaster === App.config.login);
            this.render();
        },

        setUsers: function (users) {
            this.users = users;
            this.render();
        },

        setPendingUsers: function (pendingUsers) {
            this.pendingUsers = pendingUsers;
            this.render();
        },

        toggleExpandCommandsOnParticipant: function (e) {
            if (this.isMaster) {
                var el = e.currentTarget;
                if (this.$(el).find('.fa-chevron-right').is(':visible')) {
                    this.expandCommandsOnParticipant(el);
                } else {
                    this.reduceCommandsOnParticipant(el);
                }
            }
        },

        expandCommandsOnParticipant: function (el) {
            this.$(el).find('.fa-chevron-right').hide();
            this.$(el).find('.fa-chevron-left').show();
            this.$(el).find('.collaborative_give_hand').show();
            this.$(el).find('.collaborative_kick').show();
            this.$(el).find('.collaborative_withdraw_invitation').show();
        },

        reduceCommandsOnParticipant: function (el) {
            this.$(el).find('.fa-chevron-right').show();
            this.$(el).find('.fa-chevron-left').hide();
            this.$(el).find('.collaborative_give_hand').hide();
            this.$(el).find('.collaborative_kick').hide();
            this.$(el).find('.collaborative_withdraw_invitation').hide();
        },

        reduceAllCommands: function () {
            this.$('.fa-chevron-right').show();
            this.$('.fa-chevron-left').hide();
            this.$('.collaborative_give_hand').hide();
            this.$('.collaborative_kick').hide();
            this.$('.collaborative_withdraw_invitation').hide();
        }
    });
    return CollaborativeView;
});
