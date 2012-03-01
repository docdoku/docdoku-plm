var BaseView = Backbone.View.extend({
	baseViewBindings: function () {
		this.subViews = [];
		_.bindAll(this, "template", "render");
		if (this.model) {
			if (this.onModelChange) {
				_.bindAll(this, "onModelChange");
				this.model.bind("change", this.onModelChange);
			}
			if (this.onModelSync) {
				_.bindAll(this, "onModelSync");
				this.model.bind("sync", this.onModelSync);
			}
			if (this.onModelDestroy) {
				_.bindAll(this, "onModelDestroy");
				this.model.bind("sync", this.onModelDestroy);
			}
		}
		if (this.collection) {
			if (this.onCollectionReset) {
				_.bindAll(this, "onCollectionReset");
				this.collection.bind("reset", this.onCollectionReset);
			}
			if (this.onCollectionRemove) {
				_.bindAll(this, "onCollectionRemove");
				this.collection.bind("remove", this.onCollectionRemove);
			}
		}
	},
	template: function(data) {
		data.view_cid = this.cid;
		data._ = app.i18n;
		var template = $(this.template_el).html();
		return Mustache.render(template, data);
	},
	renderData: function () {
		var data = {};
		if (this.model) {
			data.model = this.modelToJSON ?
				this.modelToJSON() :
				this.model.toJSON();
		}
		if (this.collection) {
			data.collection = this.collectionToJSON ?
				this.collectionToJSON() :
				this.collection.toJSON();
		}
		return data;
	},
	removeSubViews: function () {
		_.each(this.subViews, function (view) {
			view.remove();
			delete view;
		});
	},
	render: function () {
		this.removeSubViews();
		var data = this.renderData ? this.renderData() : {};
		$(this.el).html(this.template(data));
		if (this.renderAfter) { this.renderAfter(); }
	},
	onModelDestroy: function () {
		this.removeSubViews();
		this.remove();
		delete this;
	},
	alert: function (model) {
		var view = new AlertView({
			el: $(this.el).find(".alerts").first(),
			model: model
		});
		this.subViews.push(view);
		view.render();
	},
});
