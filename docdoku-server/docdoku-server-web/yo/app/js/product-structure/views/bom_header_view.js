/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/bom_header.html'
], function (Backbone, Mustache, template) {
    'use strict';
    var BomHeaderView = Backbone.View.extend({

        el: '#top_controls_container',

        events: {
            "click .checkout": "actionCheckout",
            "click .undocheckout": "actionUndocheckout",
            "click .checkin": "actionCheckin",
            "click .edit-acl": "actionUpdateACL"
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
            this.checkoutGroup = this.$(".checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.checkinButton = this.$(".checkin");
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
                    this.onSeveralComponentsSelected();
                    break;
            }

        },

        hideButtons:function(){
            this.hideCheckGroup();
            this.hideACLButton();
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
            if(!component.isReleased()){
                this.checkoutGroup.show();
            }
            if(App.config.workspaceAdmin || component.getAuthorLogin() === App.config.login){
                this.aclButton.show();
            }

            if (component.isCheckout()) {
                if (component.isCheckoutByConnectedUser()) {
                    this.updateActionsButton(false, true);
                } else {
                    this.updateActionsButton(false, false);
                }
            } else {
                this.updateActionsButton(true, false);
            }

        },

        onSeveralComponentsSelected: function () {
            this.hideCheckGroup();
            this.hideACLButton();
        },

        updateActionsButton: function (canCheckout, canUndoAndCheckin) {
            this.checkoutButton.prop('disabled', !canCheckout);
            this.undoCheckoutButton.prop('disabled', !canUndoAndCheckin);
            this.checkinButton.prop('disabled', !canUndoAndCheckin);
        }

    });

    return BomHeaderView;

});
