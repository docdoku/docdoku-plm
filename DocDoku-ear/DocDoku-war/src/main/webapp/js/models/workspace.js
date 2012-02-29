var Workspace = Backbone.Model.extend({
	initialize: function () {
		this.templates = new TemplateList();
	}
});
