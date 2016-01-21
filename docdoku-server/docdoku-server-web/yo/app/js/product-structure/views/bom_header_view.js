/*global define,App,_*/
define([
    'backbone',
    'mustache',
    'text!templates/bom_header.html',
    'common-objects/views/prompt'
], function (Backbone, Mustache, template, PromptView) {
    'use strict';
    var BomHeaderView = Backbone.View.extend({

        events: {
            'click .checkout-group .checkout': 'actionCheckout',
            'click .checkout-group .undocheckout': 'actionUndocheckout',
            'click .checkout-group .checkin': 'actionCheckin',
            'click .cascade-checkout-group button.checkout': 'actionPBSCheckout',
            'click .cascade-checkout-group button.undocheckout': 'actionPBSUndocheckout',
            'click .cascade-checkout-group button.checkin': 'actioPBSnCheckin',
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

        cascadeSuccess: function(params) {
            App.partsTreeView.refreshAll();
            this.trigger('alert',{type: 'info', message: this.formatResponse(params)});
        },

        formatResponse: function(params) {
            //TODO: might be better to format the response on the server
            return App.config.i18n.CASCADE_RESULT + ' : '+params.succeedAttempts + ' / ' + (params.failedAttempts + params.succeedAttempts) + ' ' + App.config.i18n.DONE;
        },

        onSuccess: function() {
            Backbone.Events.trigger('part:saved');
        },

        actionPBSCheckout: function () {
            var ajaxes = [];
            _(App.partsTreeView.checkedPath).each(function (component) {
                ajaxes.push(component.checkout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actionPBSUndocheckout: function () {
            var ajaxes = [];
            _(App.partsTreeView.checkedPath).each(function (component) {
                ajaxes.push(component.undocheckout());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actioPBSnCheckin: function () {
            var ajaxes = [];
            _(App.partsTreeView.checkedPath).each(function (component) {
                ajaxes.push(component.checkin());
            });
            $.when.apply($, ajaxes).then(this.onSuccess);
        },

        actionCascadeCheckout: function() {
            App.partsTreeView.checkedPath[0].cascadeCheckout(this.cascadeSuccess);
        },

        actionCascadeUndocheckout: function() {
            App.partsTreeView.checkedPath[0].cascadeUndoCheckout(this.cascadeSuccess);

        },

        actionCascadeCheckin: function() {

            var _this = this;
            var promptView = new PromptView();

            promptView.specifyInput('textarea');
            promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.PART_REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
            window.document.body.appendChild(promptView.render().el);
            promptView.openModal();

            this.listenTo(promptView, 'prompt-ok', function (args) {
                var iterationNote = args[0];
                if (_.isEqual(iterationNote, '')) {
                    iterationNote = null;
                }
                App.partsTreeView.checkedPath[0].cascadeCheckin(_this.cascadeSuccess,iterationNote);
            });

            this.listenTo(promptView, 'prompt-cancel', function () {
                App.partsTreeView.checkedPath[0].cascadeCheckin(_this.cascadeSuccess,null);
            });

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
            this.cascadeCheckout = this.$('.cascade-checkout-group .cascade-button-checkout');
            this.cascadeUndocheckout = this.$('.cascade-checkout-group .cascade-button-undocheckout');
            this.cascadeCheckin = this.$('.cascade-checkout-group .cascade-button-checkin');
            this.pbsCheckout = this.$('.cascade-checkout-group .checkout');
            this.pbsUndocheckout = this.$('.cascade-checkout-group .undocheckout');
            this.pbsCheckin = this.$('.cascade-checkout-group .checkin');
            this.aclButton = this.$('.edit-acl');
            return this;
        },

        onPathChange: function(list) {
            if(App.config.productConfigSpec === 'wip') {
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
            } else {
                this.cascadeCheckoutGroup.hide();
            }

        },
        onNoPathSelected: function() {
            this.cascadeCheckoutGroup.hide();
        },

        onOnePathSelected: function(component) {
            if(!component.isReleased() && !component.isObsolete())  {
                this.cascadeCheckoutGroup.css('display', 'inline-block');
            }
            this.updatePathActionsButton(this.getPermission(component),true);
        },

        onSeveralPathSelected: function(list) {
            this.cascadeCheckoutGroup.css('display', 'inline-block');
            var perm = this.getPermissionFromSeveral(list);
            if(perm) {
                this.updatePathActionsButton(perm,false);
            } else {
                this.cascadeCheckoutGroup.hide();
            }
        },

        updatePathActionsButton: function (permission,dropdownStatus) {
            this.pbsCheckout.prop('disabled', !permission.canCheckout);
            this.cascadeCheckout.prop('disabled', (!permission.canCheckout || !dropdownStatus));
            this.pbsUndocheckout.prop('disabled', !permission.canUndoAndCheckin);
            this.cascadeUndocheckout.prop('disabled', (!permission.canUndoAndCheckin || !dropdownStatus));
            this.pbsCheckin.prop('disabled', !permission.canUndoAndCheckin);
            this.cascadeCheckin.prop('disabled', (!permission.canUndoAndCheckin || !dropdownStatus));
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
            if (!component.isReleased() && !component.isObsolete()) {
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

        getPermissionFromSeveral: function(list) {
            var noneReleased = true;
            var noneObsolete = true;
            var permission = this.getPermission(list[0]);
            var samePermission = true;
            var that = this;
            _.each(list, function (component) {
                var permComponent = that.getPermission(component);
                samePermission = samePermission && (permission.canCheckout === permComponent.canCheckout &&
                    permComponent.canUndoAndCheckin === permComponent.canUndoAndCheckin);
                noneReleased = noneReleased && !component.isReleased();
                noneObsolete = noneObsolete && !component.isObsolete();
            });
            return (noneReleased && noneObsolete && samePermission) ? permission : null;
        },

        onSeveralComponentsSelected: function (listComponent) {
            var permission = this.getPermissionFromSeveral(_.pluck(listComponent,'model'));
            if (permission !== null) {
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
