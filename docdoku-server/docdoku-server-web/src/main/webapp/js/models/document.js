define([
	"i18n",
	"common/date",
	"collections/document_iteration"
], function (
	i18n,
	date,
	DocumentIterationList
) {
	var Document = Backbone.Model.extend({
		initialize: function () {
			_.bindAll(this);
		},
		parse: function(data) {
			this.iterations = new DocumentIterationList(data.documentIterations);
			this.iterations.document = this;
			if (data.documentIterations.length) {
				this.lastIteration = this.iterations.get(data.documentIterations[data.documentIterations.length - 1].iteration);
				data.lastIteration = this.lastIteration;
			}
			data.documentIterations = this.iterations;
			return data;
		},
		url: function() {
			if (this.get("id")) {
				var baseUrl = "/api/workspaces/" + APP_CONFIG.workspaceId + "/documents";
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
	return Document;
});
