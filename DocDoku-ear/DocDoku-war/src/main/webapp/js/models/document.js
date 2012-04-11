var Document = Backbone.Model.extend({
	initialize: function () {
		_.bindAll(this);
	},
	parse: function(data) {
		if (data.documentIterations) {
			this.iterations = new DocumentIterationList(data.documentIterations);
			this.iterations.document = this;
			data.documentIterations = this.iterations;
			// TODO update webservice to use lastIteration = lastIteration id
			// and have the iterations in "iterations"
			// and use iteration.id intead of iteration.iteration
			this.lastIteration = this.iterations.get(data.lastIteration.iteration);
			data.lastIteration = this.lastIteration;
		}
		return data;
	},
	url: function() {
		if (this.get("id")) {
			var baseUrl = "/api/workspaces/" + app.workspaceId + "/documents";
			return baseUrl + "/" + this.get("id");
		} else if (this.collection) {
			return this.collection.url;
		}
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
