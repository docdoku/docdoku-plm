define([
	"i18n",
	"views/content",
	"views/document_list",
	"text!templates/content_document_list_checkout_button_group.html",
	"text!templates/content_document_list.html"
], function (
	i18n,
	ContentView,
	DocumentListView,
	checkout_button_group,
	template
) {
	var ContentDocumentListView = ContentView.extend({
		template: Mustache.compile(template),
		partials: {
			checkout_button_group: checkout_button_group
		},
		initialize: function () {
			ContentView.prototype.initialize.apply(this, arguments);
			this.events["click .actions .checkout"] = "actionCheckout";
			this.events["click .actions .undocheckout"] = "actionUndocheckout";
			this.events["click .actions .checkin"] = "actionCheckin";
			this.events["click .actions .delete"] = "actionDelete";
		},
		rendered: function () {
			this.listView = this.addSubView(
				new DocumentListView({
					el: "#list-" + this.cid,
					collection: this.collection
				})
			);
			this.collection.fetch();
			this.listView.on("selectionChange", this.selectionChanged);
			this.selectionChanged();
		},
		selectionChanged: function () {
			var action = this.listView.checkedViews().length > 0 ? "show" : "hide";
            if (action == "show") {
                this.$el.find(".actions .checkout-group").css('display', 'inline-block');
            } else {
                this.$el.find(".actions .checkout-group").hide();
            }
			this.$el.find(".actions .delete")[action]();
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
		},
	});
	return ContentDocumentListView;
});
