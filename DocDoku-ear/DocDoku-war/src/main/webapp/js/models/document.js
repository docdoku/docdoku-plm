var Document = Backbone.Model.extend({
	url: function() {
		if (this.get("id")) {
			var baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
			return baseUrl + "/" + this.get("id");
		} else if (this.collection) {
			return this.collection.url;
		}
	},
	initialize: function () {
		_.bindAll(this, "checkout", "undocheckout", "checkin");
		/*
		this.iterations = new DocumentIterationList();
		this.iterations.parent = this;
		*/
	},
	checkout: function () {
		$.ajax({
			context: this,
			type: "PUT",
			url: this.url() + "/checkout",
			success: function () {
				this.fetch();
			}
		});
	},
	undocheckout: function () {
		$.ajax({
			context: this,
			type: "PUT",
			url: this.url() + "/undocheckout",
			success: function () {
				this.fetch();
			}
		});
	},
	checkin: function () {
		$.ajax({
			context: this,
			type: "PUT",
			url: this.url() + "/checkin",
			success: function () {
				this.fetch();
			}
		});
	}
});
