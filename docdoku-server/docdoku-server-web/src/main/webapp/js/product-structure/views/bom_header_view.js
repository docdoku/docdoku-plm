define(["text!templates/bom_header.html"], function(template) {

    var BomHeaderView = Backbone.View.extend({

        el: $('#top_controls_container'),

        events: {
            "click .checkout": "actionCheckout",
            "click .undocheckout": "actionUndocheckout",
            "click .checkin": "actionCheckin"
        },

        actionCheckout: function() {
            this.trigger('actionCheckout');
        },

        actionUndocheckout: function() {
            this.trigger('actionUndocheckout');
        },

        actionCheckin: function() {
            this.trigger('actionCheckin');
        },

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.prepend(Mustache.render(template, {i18n : {}}));
            this.checkoutGroup = this.$(".checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.checkinButton = this.$(".checkin");
            return this;
        },

        onSelectionChange: function(checkedViews) {

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

        onNoComponentSelected: function() {
            this.checkoutGroup.hide();
        },

        onOneComponentSelected: function(component) {
            this.checkoutGroup.show();

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

        onSeveralComponentsSelected: function() {
            this.checkoutGroup.hide();
        },

        updateActionsButton: function(canCheckout, canUndoAndCheckin) {
            this.checkoutButton.prop('disabled', !canCheckout);
            this.undoCheckoutButton.prop('disabled', !canUndoAndCheckin);
            this.checkinButton.prop('disabled', !canUndoAndCheckin);
        }

    });

    return BomHeaderView;

});