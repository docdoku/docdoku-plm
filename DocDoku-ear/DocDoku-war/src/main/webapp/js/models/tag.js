var Tag = Backbone.Model.extend({
	initialize: function () {
		this.documents = new DocumentList();
	}
});
