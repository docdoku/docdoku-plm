/*global $,define,App*/
define(['backbone','common-objects/collections/users'],
function (Backbone, Users) {
    'use strict';

    var statusHtml = {
        OFFLINE: '<i class="fa fa-user user-offline"></i> ' + App.config.i18n.OFFLINE,
        ONLINE: '<i class="fa fa-user user-online"></i> ' + App.config.i18n.ONLINE
    };

    // popover content template
    var tipContent = '<div>' +
        '<span class="user-status"></span>' +
        '<hr />' +
        '<button type="button" class="btn btn-success webRTC_invite_button"><i class="fa fa-video-camera"></i> Video </button> ' +
        '<button type="button" class="btn btn-info new_chat_session_button"><i class="fa fa-comments"></i> Chat </button> ' +
        '<a class="btn btn-warning mailto_button" href="" target="_blank"><i class="fa fa-envelope"></i> Mail </a>' +
        '</div>';

    var users = new Users();

    $.fn.userPopover = function (userLogin, context, placement) {

        // don't show the popover if user clicks on his name
        if (userLogin === App.config.login) {
            return $(this).addClass('is-connected-user');
        }

        var popoverLink = $(this).popover({
            title: '',
            html: true,
            content: tipContent,
            container: 'body',
            trigger: 'manual',
            placement: placement
        }).click(function (e) {

            var that = this;

            // Fetch and display user data
            users.fetch({reset: true, success: function () {

                // find user in collection
                var user = users.findWhere({login: userLogin});

                if (user) {

                    $(that).popover('show').on('hidden', function(e) {
                        // Fix bug (modal closing on popover close https://github.com/twbs/bootstrap/issues/6942)
                        e.stopPropagation();
                    });

                    // get the popover tip element
                    var $tip = $(that).data('popover').$tip;
                    $tip.addClass('reach-user-popover');
                    $tip.toggleClass('above-modal-popover',$('.modal').length > 0);

                    // Listen for the status request done
                    Backbone.Events.on('UserStatusRequestDone', function (message) {
                        if (message.remoteUser === userLogin && message.status !== null) {
                            $tip.find('.user-status').html(statusHtml[message.status]);
                        }
                    });

                    // trigger the status request
                    Backbone.Events.trigger('UserStatusRequest', user.get('login'));

                    // set the popover title
                    $tip.find('.popover-title').html(user.get('name') + ' | ' + App.config.workspaceId + ' : ' + context);

                    // handle webrtc button click event
                    $tip.find('.webRTC_invite_button').one('click', function () {
                        Backbone.Events.trigger('NewOutgoingCall', { remoteUser: user.get('login'), context: App.config.workspaceId + ' : ' + context});
                        $(that).popover('hide');
                    });

                    // handle chat button click event
                    $tip.find('.new_chat_session_button').one('click', function () {
                        Backbone.Events.trigger('NewChatSession', {remoteUser: user.get('login'), context: App.config.workspaceId + ' : ' + context});
                        $(that).popover('hide');
                    });

                    // handle mail button click event
                    var mailToString = encodeURI('mailto:' + user.get('email') + '?subject=' + App.config.workspaceId + ' : ' + context);
                    $tip.find('.mailto_button').attr('href', mailToString).one('click', function () {
                        $(that).popover('hide');
                    });
                }

            }});

            e.stopPropagation();
            e.preventDefault();
            return false;

        });

        return popoverLink;

    };
});
