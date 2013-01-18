define([
	"i18n",
	"collections/template",
	"views/content",
	"views/template_list",
	"views/template_new",
	"text!templates/template_content_list.html"
], function (
	i18n,
	TemplateList,
	ContentView,
	TemplateListView,
	TemplateNewView,
	template
) {
	var TemplateContentListView = ContentView.extend({
		template: Mustache.compile(template),
		collection: function () {
			return TemplateList.getInstance();
		},
		initialize: function () {
			ContentView.prototype.initialize.apply(this, arguments);
			this.events["click .actions .new-template"] = "actionNew";
			this.events["click .actions .delete"] = "actionDelete";
		},
		rendered: function () {
			this.listView = this.addSubView(new TemplateListView({
				el: "#list-" + this.cid,
				collection: this.collection
			}));
			this.listView.collection.fetch();
			this.listView.on("selectionChange", this.selectionChanged);
			this.selectionChanged();
		},
		selectionChanged: function () {
			var showOrHide = this.listView.checkedViews().length > 0;
			var action = showOrHide ? "show" : "hide";
			this.$el.find(".actions .delete")[action]();
		},
		actionNew : function () {
			var view = this.addSubView(
				new TemplateNewView({
					collection: this.collection
				})
			).show();
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
	return TemplateContentListView;
});
