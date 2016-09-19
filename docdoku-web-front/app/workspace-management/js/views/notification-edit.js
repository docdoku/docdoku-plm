/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/notification-edit.html',
    'common-objects/models/workspace',
    'common-objects/models/user_group',
    'common-objects/models/user',
    'common-objects/models/tag_subscription',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Workspace, UserGroupModel, UserModel, TagSubscription, AlertView) {
    'use strict';

    var NotificationEditView = Backbone.View.extend({

        events: {
            'click .change':'notificationOptionsHaveChanged',
            'click .remove':'removeTagSubscription'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;

            if (this.options.id) {
                this.getTagSubscriptions()
                    .then(function(tagSubscriptions){
                        _this.tagSubscriptions = tagSubscriptions;

                        _this.$el.html(Mustache.render(template, {
                            i18n: App.config.i18n,
                            tagSubscriptions:tagSubscriptions
                        }));
                        _this.bindDOMElements();
                    });
            }

            return this;
        },

        getTagSubscriptions: function () {
            if (this.options.type === 'group') {
                return UserGroupModel.getTagSubscriptions(App.config.workspaceId, this.options.id);
            } else {
                return UserModel.getTagSubscriptions(App.config.workspaceId, this.options.id);
            }
        },

        bindDOMElements:function(){
            this.$notifications = this.$('.notifications');
        },

        onError:function(error){
            this.$notifications.append(new AlertView({
                type: 'error',
                message: error.responseText
            }).render().$el);
        },

        notificationOptionsHaveChanged:function(e){
            var changedTagSubscription = new TagSubscription();

            _.each(this.tagSubscriptions, function(tagSubscription) {
                if (tagSubscription.tag === e.currentTarget.dataset.id) {
                    changedTagSubscription.setTag(tagSubscription.tag);

                    if (e.currentTarget.dataset.option === 'state') {
                        changedTagSubscription.setOnStateChange(e.currentTarget.checked);
                        changedTagSubscription.setOnIterationChange(tagSubscription.onIterationChange);

                    } else if (e.currentTarget.dataset.option === 'iteration') {
                        changedTagSubscription.setOnStateChange(tagSubscription.onStateChange);
                        changedTagSubscription.setOnIterationChange(e.currentTarget.checked);
                    }
                }
            });

            if (this.options.type === 'group') {
                UserGroupModel.addOrEditTagSubscription(App.config.workspaceId, this.options.id, changedTagSubscription)
                    .then(this.render.bind(this), this.onError.bind(this));

            } else {
                UserModel.addOrEditTagSubscription(App.config.workspaceId, this.options.id, changedTagSubscription)
                    .then(this.render.bind(this), this.onError.bind(this));
            }
        },

        removeTagSubscription:function(e){
            if (this.options.type === 'group') {
                UserGroupModel.removeTagSubscription(App.config.workspaceId, this.options.id, e.currentTarget.dataset.id)
                    .then(this.render.bind(this), this.onError.bind(this));

            } else {
                UserModel.removeTagSubscription(App.config.workspaceId, this.options.id, e.currentTarget.dataset.id)
                    .then(this.render.bind(this), this.onError.bind(this));
            }
        }

    });

    return NotificationEditView;
});
