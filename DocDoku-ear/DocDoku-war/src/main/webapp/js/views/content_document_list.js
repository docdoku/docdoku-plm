var ContentDocumentListView = ContentView.extend({
	template: "content-document-list-tpl",
	initialize: function () {
		ContentView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"click .actions .checkout": "actionCheckout",
			"click .actions .undocheckout": "actionUndocheckout",
			"click .actions .checkin": "actionCheckin",
			"click .actions .delete": "actionDelete"
		});
	},
	rendered: function () {
		this.listView = this.addSubView(new DocumentListView({
			el: "#list-" + this.cid,
			collection: this.collection
		}));
		this.collection.fetch();
		this.listView.on("selectionChange", this.selectionChanged);
		this.selectionChanged();
	},
	selectionChanged: function () {
		var action = this.listView.checkedViews().length > 0 ? "show" : "hide";
		this.$el.find(".actions .checkout-group")[action]();
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
		if (confirm(app.i18n["DELETE_SELECTION_?"])) {
			this.listView.eachChecked(function (view) {
				view.model.destroy();
			});
		}
		return false;
	},
});
