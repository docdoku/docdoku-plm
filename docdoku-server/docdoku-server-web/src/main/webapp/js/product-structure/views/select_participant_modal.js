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

            render: function() {
                this.$el.html(this.template({i18n: i18n}));
                this.$modal = this.$("#selectParticipantModal");
                var $form = this.$("form");
                var users = new Users();

                users.fetch({reset: true, success: function () {

                    _.each(users.models, function(user){
                        var userName = user.attributes.login;
                        if(userName != APP_CONFIG.login){
                            $form.append('<label class="radio">  <input type="radio" name="user" value="'+userName+'"/>'+userName+'</label>');
                        }
                    });
                    $form.append('<button type="submit" class="btn btn-primary">Share my view</button>');
                }});
                return this;
            },

            openModal: function() {
                var that = this ;

                this.$modal.modal('show');
            },

            closeModal: function() {
                this.$modal.modal('hide');
            },

            onHidden: function() {
                this.remove();
            } ,
            onSubmit:function(e){
                var remoteUser = this.$("input:checked").val();
                //window.location.hash = "share-view/"+THREE.Math.generateUUID();
                //var path = document.location.href;
                //var pathHash  = path.substring( 0 ,path.lastIndexOf( "#" ) ); // THREE.Math.generateUUID()
                console.log(remoteUser);
                var message = {
                    type: ChannelMessagesType.CHAT_MESSAGE,
                    remoteUser: remoteUser,
                    message: "/inviteScene="+THREE.Math.generateUUID(),
                    context: APP_CONFIG.workspaceId+' '+APP_CONFIG.productId,
                    sender:APP_CONFIG.login
                };
                mainChannel.sendJSON(message);
                this.closeModal();
                App.sceneManager.setCollaborativeMode(remoteUser);
                return false;
            }


        });

        return SelectParticipantModalView;
    }
);
