define([
	"i18n",
	"views/components/list_item",
	"views/tag_document_list",
	"text!templates/tag_list_item.html"
], function (
	i18n,
	ListItemView,
	TagDocumentListView,
	template
) {
	var TagListItemView = ListItemView.extend({
		template: Mustache.compile(template),
		tagName: "li",
		className: "tag",
		initialize: function () {
			ListItemView.prototype.initialize.apply(this, arguments);
			this.events = _.extend(this.events, {
				"click .actions .edit": "actionEdit",
				"click .actions .delete": "actionDelete",
				"mouseleave .header": "hideActions"
			});
		},
		hideActions: function () {
			// Prevents the actions menu to stay opened all the time
			this.$el.find(".header .btn-group").first().removeClass("open");
		},
		setActive: function () {
			$("#nav .active").removeClass("active");
			this.$el.find(".nav-list-entry").first().addClass("active");
		},
		showContent: function () {
			this.setActive();
			this.addSubView(
				new TagDocumentListView({
					model: this.model
				})
			).render();
		},
		actionEdit: function () {
			this.hideActions();
			var view = this.addSubView(
				new TagEditView({
					model: this.model
				})
			).render();
			return false;
		},
		actionDelete: function () {
			this.hideActions();
			if (confirm(i18n["DELETE_TAG_?"])) {
				this.model.destroy();
			}
			return false;
		},
	});
	return TagListItemView;
});
