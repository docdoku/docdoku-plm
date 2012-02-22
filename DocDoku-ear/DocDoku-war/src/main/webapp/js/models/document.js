var Document = Backbone.Model.extend({
	url: function() {
		if (this.get("id")) {
			baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
			return baseUrl + "/" + this.get("id");
		} else if (this.collection) {
			return this.collection.url;
		}
	},
	initialize: function () {
		_.bindAll(this,
			"checkout", "undocheckout", "checkin"
			);
	},
	checkout: function () {
		var that = this;
		$.ajax({
			type: "PUT",
			url: this.url() + "/checkout",
			success: function () {
				that.fetch();
			}
		});
	},
	undocheckout: function () {
		var that = this;
		$.ajax({
			type: "PUT",
			url: this.url() + "/undocheckout",
			success: function () {
				that.fetch();
			}
		});
	},
	checkin: function () {
		var that = this;
		$.ajax({
			type: "PUT",
			url: this.url() + "/checkin",
			success: function () {
				that.fetch();
			}
		});
	}
});
