/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/notification-edit.html',
    'common-objects/models/workspace',
    'common-objects/models/user_group',
    'common-objects/models/user',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, Workspace, UserGroupModel, UserModel, AlertView) {
    'use strict';

    var NotificationEditView = Backbone.View.extend({

        events: {
            'click .toggle-checkbox':'toggleCheckbox',
            'click .remove-tag-subscription':'removeTagSubscription'
        },

        initialize: function () {
        },

        render: function () {
            var _this = this;

            if (this.options.id) {
                this.getTagSubscriptions()
                    .then(function(tagSubscriptions){
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

        toggleCheckbox:function(e){
            // TODO
        },

        removeTagSubscription:function(){
            // TODO
        }

    });

    return NotificationEditView;
});
