var TemplateContentListView = ContentView.extend({
	template: "#template-content-list-tpl",
	initialize: function () {
		ContentView.prototype.initialize.apply(this, arguments);
		this.events = _.extend(this.events, {
			"click .actions .new": "actionNew",
			"click .actions .delete": "actionDelete"
		});
	},
	rendered: function () {
		this.listView = this.addSubView(new TemplateListView({
			el: "#list-" + this.cid,
		}));
		this.listView.collection.fetch();
		this.listView.on("selectionChange", this.selectionChanged);
		this.selectionChanged();
	},
	selectionChanged: function () {
		var action = this.listView.checkedViews().length > 0 ? "show" : "hide";
		this.$el.find(".actions .delete")[action]();
	},
	actionNew : function () {
		var view = this.addSubView(new TemplateNewView());
		view.render();
		return false;
	},
	actionDelete: function () {
		this.listView.eachChecked(function (view) {
			view.model.destroy();
		});
		return false;
	},
});
