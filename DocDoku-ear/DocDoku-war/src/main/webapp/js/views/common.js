BaseView = Backbone.View.extend({
	template: function(data) {
		data.view_cid = this.cid;
		data._ = app.i18n;
		return Mustache.render(
			$(this.template_el).html(),
			data
		);
	},
	renderData: function () {
		data = {};
		if (this.model) {
			data.model = this.modelDataFormater ?
				this.modelDataFormater(this.model.toJSON()) :
				this.model.toJSON();
		}
		if (this.collection) {
			data.collection = this.collectionDataFormater ?
				this.collectionDataFormater(this.collection.toJSON()) :
				this.collection.toJSON();
		}
		return data;
	},
	render: function () {
		var data = this.renderData ? this.renderData() : {};
		$(this.el).html(this.template(data));
		if (this.renderAfter) { this.renderAfter(); }
	},
	alert: function (model) {
		alertView = new AlertView({
			el: $(this.el).find(".alerts").first(),
			model: model
		});
		alertView.render();
	},
});
ModalView = BaseView.extend({
	events: {
		"click .cancel": "cancel",
	},
	cancel: function () {
		$(this.el).modal("hide");
		this.remove();
		return false;
	}
});
