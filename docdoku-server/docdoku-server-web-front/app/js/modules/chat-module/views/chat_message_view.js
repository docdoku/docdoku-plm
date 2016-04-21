/*global define,_*/
define(['backbone'], function (Backbone) {
    'use strict';
    var ChatMessageView = Backbone.View.extend({

        tagName: 'li',

        template: _.template(
            '<span><b><%= chatMessage.sender %></b> : <%= chatMessage.message.replaceUrl() %></span>'
        ),

        render: function () {
            this.$el.html(this.template({chatMessage: this.model.attributes}));
            return this;
        }

    });

    return ChatMessageView;

});
