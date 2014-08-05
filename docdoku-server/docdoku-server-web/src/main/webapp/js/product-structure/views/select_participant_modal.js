/*global mainChannel,ChannelMessagesType*/
define(    [
        "common-objects/collections/users",
        "text!templates/select_participant_modal.html",
        "i18n!localization/nls/product-structure-strings"
    ],

    function (Users,template, i18n) {

        var SelectParticipantModalView = Backbone.View.extend({

            events: {
                "hidden #selectParticipantModal": "onHidden",
                "submit form": "onSubmit"
            },

            template: Mustache.compile(template),

            initialize: function() {
                _.bindAll(this);
            },

            setRoomKey: function(key){
                this.roomKey = key;
            },

            render: function() {
                this.$el.html(this.template({i18n: i18n}));
                this.$modal = this.$("#selectParticipantModal");
                var $form = this.$("form");
                var users = new Users();

                // Listen for the status request done
                Backbone.Events.on('UserStatusRequestDone', function(message){
                    if(message.status != null){
                        if(message.status == "OFFLINE"){
                            $form.prepend('<label class="radio">  <input type="checkbox" name="user" value="'+message.remoteUser+'"/>'+message.remoteUser+' <i class="fa fa-user user-offline" title="offline"></i></label>');
                        }else if(message.status == "ONLINE"){
                            $form.prepend('<label class="radio">  <input type="checkbox" name="user" value="'+message.remoteUser+'"/>'+message.remoteUser+' <i class="fa fa-user user-online" title="online"></i></label>');
                        }
                    }
                });

                users.fetch({reset: true, success: function () {
                    _.each(users.models, function(user){
                        var login = user.attributes.login;
                        if (login != APP_CONFIG.login) { // trigger the status request
                            Backbone.Events.trigger('UserStatusRequest', login);
                        }
                    });
                }});

                $form.append('<button type="submit" class="btn btn-primary">Share my view</button>');
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
            } ,
            onSubmit:function(e){
                var remoteUsers = this.$("input:checked");
                var that = this;
                //var path = document.location.href;

                remoteUsers.each(function(index, element){
                    mainChannel.sendJSON({
                        type: ChannelMessagesType.COLLABORATIVE_INVITE,
                        key: that.roomKey,
                        messageBroadcast: {
                            context:APP_CONFIG.workspaceId+'/'+APP_CONFIG.productId
                        },
                        remoteUser: element.value
                    });
                });

                this.closeModal();
                return false;
            }


        });

        return SelectParticipantModalView;
    }
);
