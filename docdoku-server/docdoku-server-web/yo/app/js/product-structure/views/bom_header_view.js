/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/bom_header.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var BomHeaderView = Backbone.View.extend({

        events: {
            'click .checkout': 'actionCheckout',
            'click .undocheckout': 'actionUndocheckout',
            'click .checkin': 'actionCheckin',
            'click .edit-acl': 'actionUpdateACL'
        },

        actionCheckout: function () {
            this.trigger('actionCheckout');
        },

        actionUndocheckout: function () {
            this.trigger('actionUndocheckout');
        },

        actionCheckin: function () {
            this.trigger('actionCheckin');
        },

        actionUpdateACL: function () {
            this.trigger('actionUpdateACL');
        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.append(Mustache.render(template, {i18n: App.config.i18n}));
            this.checkoutGroup = this.$('.checkout-group');
            this.checkoutButton = this.$('.checkout');
            this.undoCheckoutButton = this.$('.undocheckout');
            this.checkinButton = this.$('.checkin');
            this.aclButton = this.$('.edit-acl');
            return this;
        },

        onSelectionChange: function (checkedViews) {
            switch (checkedViews.length) {
                case 0:
                    this.onNoComponentSelected();
                    break;
                case 1:
                    this.onOneComponentSelected(checkedViews[0].model);
                    break;
                default:
                    this.onSeveralComponentsSelected(checkedViews);
                    break;
            }

        },

        hideCheckGroup: function () {
            this.checkoutGroup.hide();
        },

        hideACLButton: function () {
            this.aclButton.hide();
        },

        onNoComponentSelected: function () {
            this.hideCheckGroup();
            this.aclButton.hide();
        },

        onOneComponentSelected: function (component) {
            if (!component.isReleased()) {
                this.checkoutGroup.show();
            }
            if (App.config.workspaceAdmin || component.getAuthorLogin() === App.config.login) {
                this.aclButton.show();
            }

            this.updateActionsButton(this.getPermission(component));

        },

        getPermission: function (component) {
            if (component.isCheckout()) {
                if (component.isCheckoutByConnectedUser()) {
                    return {
                        canCheckout: false,
                        canUndoAndCheckin: true
                    };
                } else {
                    return {
                        canCheckout: false,
                        canUndoAndCheckin: false
                    };
                }
            } else {
                return {
                    canCheckout: true,
                    canUndoAndCheckin: false
                };
            }
        },

        onSeveralComponentsSelected: function (listComponent) {
            var noneReleased = true;
            var permission = this.getPermission(listComponent[0].model);
            var samePermission = true;
            var that = this;
            _.each(listComponent, function (component) {
                var permComponent = that.getPermission(component.model);
                samePermission = samePermission && (permission.canCheckout === permComponent.canCheckout &&
                permComponent.canUndoAndCheckin === permComponent.canUndoAndCheckin);
                noneReleased = noneReleased && !component.model.isReleased();
            });
            if (noneReleased && samePermission) {
                this.checkoutGroup.show();
                this.updateActionsButton(permission);
            } else {
                this.hideCheckGroup();
            }

            this.hideACLButton();
        },

        updateActionsButton: function (permission) {
            this.checkoutButton.prop('disabled', !permission.canCheckout);
            this.undoCheckoutButton.prop('disabled', !permission.canUndoAndCheckin);
            this.checkinButton.prop('disabled', !permission.canUndoAndCheckin);
        }

    });

    return BomHeaderView;

});
