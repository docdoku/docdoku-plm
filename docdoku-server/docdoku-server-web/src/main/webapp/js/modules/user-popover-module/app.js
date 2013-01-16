define(["common-objects/collections/users"],

    function (Users) {

        // popover content template
        var tipContent = "<div>"
            + "<span class='btn webRTC_invite_button'> Video <i class='icon-facetime-video'></i></span> "
            + "<span class='btn new_chat_session_button'>Chat <i class='icon-leaf'></i></span> "
            + "<span class='btn mailto_button'>Mail <i class='icon-envelope'></i></span>"
            + "</div>";

        //
        var users = new Users();

        $.fn.userPopover = function (userLogin, context, placement) {

            if (userLogin == APP_CONFIG.login) return $(this);

            var shown = false;

            var popoverLink = $(this).popover({
                title: "",
                html: true,
                content: tipContent,
                trigger: "manual",
                placement: placement
            }).click(function (e) {

                    var that = this;

                    var hideTip = function () {
                        $(that).popover('hide');
                        shown = false ;
                    };

                    var showTip = function(){
                        $(that).popover('show');
                        shown = true;
                    };

                    if (!shown) {
                        // Fetch and display user data
                        users.fetch({"async": true, "success": function () {

                            // find user in collection
                            var user = users.where({"login": userLogin})[0];

                            if (user){

                                showTip();

                                // get the popover tip element
                                var $tip = $(that).data('popover').$tip;

                                // set the title
                                $tip.find(".popover-title").html(user.get("name") + " | " + APP_CONFIG.workspaceId + " : " + context);

                                // handle webrtc button click event
                                $tip.find(".webRTC_invite_button").one("click", function (ev) {
                                    Backbone.Events.trigger('NewWebRTCSession', user.get("login"));
                                    hideTip();
                                });

                                // handle chat button click event
                                $tip.find(".new_chat_session_button").one("click", function (ev) {
                                    Backbone.Events.trigger('NewChatSession', {remoteUser: user.get("login"), context: APP_CONFIG.workspaceId + " : " + context});
                                    hideTip();
                                });

                                // handle mail button click event
                                $tip.find(".mailto_button").one("click", function (ev) {
                                    window.open("mailto:" + user.get("email"));
                                    hideTip();
                                });
                            }

                        }});
                    }
                    else {
                        hideTip();
                    }
                    e.stopPropagation();
                    e.preventDefault();
                    return false;

                });

            return popoverLink;

        };
    });