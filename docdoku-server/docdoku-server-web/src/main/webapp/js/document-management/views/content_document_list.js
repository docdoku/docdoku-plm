define([
	"i18n!localization/nls/document-management-strings",
	"views/content",
	"views/document_list"
], function (
	i18n,
	ContentView,
	DocumentListView
) {
	var ContentDocumentListView = ContentView.extend({

		initialize: function () {
			ContentView.prototype.initialize.apply(this, arguments);
			this.events["click .actions .checkout"] = "actionCheckout";
			this.events["click .actions .undocheckout"] = "actionUndocheckout";
			this.events["click .actions .checkin"] = "actionCheckin";
			this.events["click .actions .delete"] = "actionDelete";
		},

		rendered: function () {
            this.checkoutGroup = this.$(".actions .checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.checkinButton = this.$(".checkin");
            this.deleteButton = this.$(".actions .delete");

			this.listView = this.addSubView(
				new DocumentListView({
					el: "#list-" + this.cid,
					collection: this.collection
				})
			);
			this.collection.fetch();

			this.listenTo(this.listView, "selectionChange", this.updateActionsButtons);
            this.listenTo(this.collection, "change", this.updateActionsButtons);
		},

		updateActionsButtons: function () {

            var checkedViews = this.listView.checkedViews();

            switch (checkedViews.length) {
                case 0:
                    this.onNoDocumentSelected();
                    break;
                case 1:
                    this.onOneDocumentSelected(checkedViews[0].model);
                    break;
                default:
                    this.onSeveralDocumentsSelected();
                    break;
            }

		},

        onNoDocumentSelected: function() {
            this.deleteButton.hide();
            this.checkoutGroup.hide();
        },

        onOneDocumentSelected: function(document) {
            this.deleteButton.show();
            this.checkoutGroup.css('display', 'inline-block');

            if (document.isCheckout()) {
                this.checkoutButton.prop('disabled', true);
                this.undoCheckoutButton.prop('disabled', false);
                this.checkinButton.prop('disabled', false);
            } else {
                this.checkoutButton.prop('disabled', false);
                this.undoCheckoutButton.prop('disabled', true);
                this.checkinButton.prop('disabled', true);
            }

        },

        onSeveralDocumentsSelected: function() {
            this.deleteButton.show();
            this.checkoutGroup.hide();
        },

		actionCheckout: function () {
			this.listView.eachChecked(function (view) {
				view.model.checkout();
			});
			return false;
		},

		actionUndocheckout: function () {
			this.listView.eachChecked(function (view) {
				view.model.undocheckout();
			});
			return false;
		},

		actionCheckin: function () {
			this.listView.eachChecked(function (view) {
				view.model.checkin();
			});
			return false;
		},

		actionDelete: function () {
			if (confirm(i18n["DELETE_SELECTION_?"])) {
				this.listView.eachChecked(function (view) {
					view.model.destroy();
				});
			}
			return false;
		}

	});
	return ContentDocumentListView;
});
