var Tag = Backbone.Model.extend({
	initialize: function () {
		this.documents = new DocumentTagList();
		this.documents.parent = this;
	}
});
