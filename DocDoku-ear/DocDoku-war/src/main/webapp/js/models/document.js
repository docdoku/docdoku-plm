var Document = Backbone.Model.extend({
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
