/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/bom_header.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var BomHeaderView = Backbone.View.extend({

        events: {
            'click .checkout-group .checkout': 'actionCheckout',
            'click .checkout-group .undocheckout': 'actionUndocheckout',
            'click .checkout-group .checkin': 'actionCheckin',
            'click .cascade-checkout-group .selected a.checkout': 'actionPBSCheckout',
            'click .cascade-checkout-group .selected a.undocheckout': 'actionPBSUndocheckout',
            'click .cascade-checkout-group .selected a.checkin': 'actioPBSnCheckin',
            'click .cascade-checkout-group .cascade a.checkout': 'actionCascadeCheckout',
            'click .cascade-checkout-group .cascade a.undocheckout': 'actionCascadeUndocheckout',
            'click .cascade-checkout-group .cascade a.checkin': 'actionCascadeCheckin',
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

        onSuccess: function() {
            Backbone.Events.trigger('part:saved');
        },

        actionPBSCheckout: function () {
            var ajaxes = [];
            _(App.partsTreeView.componentViews.componentViews).each(function (component) {
                ajaxes.push(component.checkout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actionPBSUndocheckout: function () {
            var ajaxes = [];
            _(App.partsTreeView.componentViews.componentViews).each(function (component) {
                ajaxes.push(component.undocheckout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actioPBSnCheckin: function () {
            var ajaxes = [];
            _(App.partsTreeView.componentViews.componentViews).each(function (component) {
                ajaxes.push(component.checkin());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actionCascadeCheckout: function() {

        },

        actionCascadeUndocheckout: function() {

        },

        actionCascadeCheckin: function() {

        },

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {
            this.$el.append(Mustache.render(template, {i18n: App.config.i18n}));
            this.checkoutGroup = this.$('.checkout-group');
            this.checkoutButton = this.$('.checkout-group .checkout');
            this.undoCheckoutButton = this.$('.checkout-group .undocheckout');
            this.checkinButton = this.$('.checkout-group .checkin');
            this.cascadeCheckoutGroup = this.$('.cascade-checkout-group');
            this.cascadeCheckout = this.$('.cascade-checkout-group .checkout');
            this.cascadeUndocheckout = this.$('.cascade-checkout-group .undocheckout');
            this.cascadeCheckin = this.$('.cascade-checkout-group .checkin');
            this.aclButton = this.$('.edit-acl');
            return this;
        },

        onPathChange: function(list) {
            switch (list.length) {
                case 0:
                    this.onNoPathSelected();
                    break;
                case 1:
                    this.onOnePathSelected(list[0]);
                    break;
                default:
                    this.onSeveralPathSelected(list);
                    break;
            }

        },
        onNoPathSelected: function() {
            this.cascadeCheckoutGroup.hide();
        },

        onOnePathSelected: function(component) {
            this.cascadeCheckoutGroup.show();
            this.cascadeCheckoutGroup.find('.cascade').removeClass('disabled');
            this.updatePathActionsButton(this.getPermission(component));
        },

        onSeveralPathSelected: function(list) {
            this.cascadeCheckoutGroup.show();
            this.cascadeCheckoutGroup.find('.cascade').addClass('disabled');
        },

        updatePathActionsButton: function (permission) {
            this.cascadeCheckout.prop('disabled', !permission.canCheckout);
            this.cascadeUndocheckout.prop('disabled', !permission.canUndoAndCheckin);
            this.cascadeCheckin.prop('disabled', !permission.canUndoAndCheckin);
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
