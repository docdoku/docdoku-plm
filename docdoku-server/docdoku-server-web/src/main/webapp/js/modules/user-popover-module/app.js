define([
    "i18n!localization/nls/user-popover-module-strings",
    "common-objects/collections/users"
    ],

    function (i18n,Users) {

        var statusHtml = {
            OFFLINE : "<i class='icon-user user-offline'></i> "+i18n.OFFLINE,
            ONLINE  : "<i class='icon-user user-online'></i> "+i18n.ONLINE
        };

        // popover content template
        var tipContent = "<div>"
            + "<span class='user-status'></span>"
            + "<hr />"
            + "<span class='btn webRTC_invite_button'><i class='icon-facetime-video'></i> Video </span> "
            + "<span class='btn new_chat_session_button'><i class='icon-comments'></i> Chat </span> "
            + "<a class='btn mailto_button' href='' target='_blank'><i class='icon-envelope'></i> Mail </a>"
            + "</div>";

        var users = new Users();

        $.fn.userPopover = function (userLogin, context, placement) {

            // don't show the popover if user clicks on his name
            if (userLogin == APP_CONFIG.login){
                return $(this).addClass("is-connected-user");
            }

            var shown = false;

            var popoverLink = $(this).popover({
                title: "",
                html: true,
                content: tipContent,
                trigger:"manual",
                placement: placement
            }).click(function (e) {

                    var that = this;

                    if (!shown) {

                        shown = true;

                        // Fetch and display user data
                        users.fetch({"async": true, "success": function () {

                            // find user in collection
                            var user = users.where({"login": userLogin})[0];

                            if (user){

                                $(that).popover('show');

                                // get the popover tip element
                                var $tip = $(that).data('popover').$tip;

                                $tip.addClass("reach-user-popover");

                                // Listen for the status request done
                                Backbone.Events.on('UserStatusRequestDone', function(message){
                                    if(message.remoteUser == userLogin && message.status != null){
                                        $tip.find(".user-status").html(statusHtml[message.status]);
                                    }
                                });

                                // trigger the status request
                                Backbone.Events.trigger('UserStatusRequest', user.get("login"));

                                // set the popover title
                                $tip.find(".popover-title").html(user.get("name") + " | " + APP_CONFIG.workspaceId + " : " + context);

                                // handle webrtc button click event
                                $tip.find(".webRTC_invite_button").one("click", function (ev) {
                                    Backbone.Events.trigger('NewOutgoingCall', { remoteUser : user.get("login") , context: APP_CONFIG.workspaceId + " : " + context});
                                    $(that).popover('hide');
                                    shown = false;
                                });

                                // handle chat button click event
                                $tip.find(".new_chat_session_button").one("click", function (ev) {
                                    Backbone.Events.trigger('NewChatSession', {remoteUser: user.get("login"), context: APP_CONFIG.workspaceId + " : " + context});
                                    $(that).popover('hide');
                                    shown = false;
                                });

                                // handle mail button click event
                                var mailToString =  encodeURI("mailto:"+user.get("email") + "?subject="+APP_CONFIG.workspaceId + " : " + context);
                                $tip.find(".mailto_button").attr("href",mailToString).one("click", function (ev) {
                                    $(that).popover('hide');
                                    shown = false;
                                });
                            }

                        }});
                    }else{
                        $(that).popover('hide');
                        shown = false;
                    }
                    e.stopPropagation();
                    e.preventDefault();
                    return false;

                });

            return popoverLink;

        };
    });