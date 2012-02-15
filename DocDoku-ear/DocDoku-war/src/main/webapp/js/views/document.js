var DocumentListView = Backbone.View.extend({
	initialize: function () {
		_.bindAll(this, "template", "render");
		this.el = this.make("table", {"class": "table table-striped table-condensed"});
		this.render();
	},
	template: function(data) {
		return Mustache.render(
			$("#document-list-tpl").html(),
			data
		);
	},
	render: function () {
		data = {
			items: this.collection.toJSON()
		}
		// Format date
		_.each(data.items, function (item) {
			if (item.lastIterationDate) {
				item.lastIterationDate = new Date(item.lastIterationDate).toLocaleDateString();
			}
		});
		$(this.el).html(this.template(data));
	}
});
