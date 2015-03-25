/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/modification_notification_list_item.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var ModificationNotificationListItemView = Backbone.View.extend({

        events: {
            'click .action-acknowledge': 'acknowledge'
        },

        initialize: function () {
            _.bindAll(this);
            this.listenTo(this.model, 'change', this.render);
        },

        render: function () {
            var data = {
                modificationNotification: this.model,
                i18n: App.config.i18n
            };

            this.$el.html(Mustache.render(template, data));

            return this;
        },

        acknowledge: function () {
            var data = {ackComment: this.getAcknowledgementComment()};
            this.model.setAcknowledged(data);
        },

        getAcknowledgementComment: function () {
            var comment;
            if (_.isEqual(this.$('#acknowledgement-comment').val(), '')) {
                comment = null;
            } else {
                comment = this.$('#acknowledgement-comment').val();
            }
            return comment;
        }

    });

    return ModificationNotificationListItemView;
});
