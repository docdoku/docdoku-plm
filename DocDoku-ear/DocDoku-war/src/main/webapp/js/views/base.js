var BaseView = Backbone.View.extend({
	modelEvents: {
		"change":	"modelChange",
		"sync":		"modelSync",
		"destroy":	"modelDestroy",
	},
	collectionEvents: {
		"reset":	"collectionReset",
		"add":		"collectionAdd",
		"remove":	"collectionRemove",
	},
	initialize: function (options) {
		// Owned events
		this.events = {};

		// Owned child views
		this.subViews = {};

		// Collection creation from factory
		if (_.isFunction(this.collection)) this.collection = this.collection();

		// Bindings
		_.bindAll(this);
		this.bindModel();
		this.bindCollection();
	},
	destroy: function () {
		this.deleteSubViews();
		this.undelegateEvents();
		this.unbindCollection();
		this.unbindModel();
		this.unbind();
		if (this.parentView) delete this.parentView.subViews[this.cid];
		if (_.isFunction(this.destroyed)) this.destroyed();
	},
	destroyed: function () {
		this.remove();
	},
	clear: function () {
		this.$el.html("");
	},
	addSubView: function (view) {
		view.parentView = this;
		this.subViews[view.cid] = view;
		if (_.isFunction(this.viewAdded)) this.viewAdded(view);
		return view;
	},
	deleteSubViews: function () {
		_.each(_.values(this.subViews), function (view) {
			view.destroy();
		});
	},
	_eventsBindings: function (options) {
		var target = options.target;
		var events = options.events;
		var action = options.action;
		for (evt in events) {
			var key = events[evt];
			if (key in this && this[key]) {
				target[action](evt, this[key]);
			};
		}
	},
	bindModel: function () {
		if (this.model) {
			this._eventsBindings({
				target: this.model,
				events: this.modelEvents,
				action: "bind",
			});
		};
	},
	bindCollection: function () {
		if (this.collection) {
			this._eventsBindings({
				target: this.collection,
				events: this.collectionEvents,
				action: "bind",
			});
		};
	},
	unbindModel: function () {
		if (this.model) {
			this._eventsBindings({
				target: this.model,
				events: this.modelEvents,
				action: "unbind",
			});
		};
	},
	unbindCollection: function () {
		if (this.collection) {
			this._eventsBindings({
				target: this.collection,
				events: this.collectionEvents,
				action: "unbind",
			});
		};
	},
	render: function () {
		this.deleteSubViews();
		var html = "";
		if (this.template) {
			var templateId = _.isFunction(this.template) ? this.template() : this.template;
			html = app.templates[templateId](this.renderData(), app.partials);
		}
		this.$el.html(html);
		if (_.isFunction(this.rendered)) this.rendered();
	},
	renderData: function () {
		var data = {};
		data._ = this.i18nRenderData();
		data.view = this.viewToJSON();
		if (this.model) data.model = this.modelToJSON();
		if (this.collection) data.collection = this.collectionToJSON();
		return data;
	},
	viewToJSON: function () {
		var data = {
			cid: this.cid
		};
		return data;
	},
	i18nRenderData: function () {
		return app.i18n;
	},
	modelToJSON: function () {
		return this.model.toJSON ?
			this.model.toJSON() :
			this.model;
	},
	collectionToJSON: function () {
		return this.collection.toJSON ?
			this.collection.toJSON() :
			this.collection;
	},
	confirm: function (options) {
		var view = this.addSubView(new ConfirmView({
			model: {
				message: options.message
			}
		}));
		view.render();
	},
	alert: function (options) {
		var titles = {
			"error": app.i18n.ERROR
		}
		options.title = options.title ? options.title : titles[options.type];
		var view = this.addSubView(new AlertView({
			el: "#alerts-" + this.cid,
			model: {
				type: options.type,
				title: options.title,
				message: options.message
			}
		}));
		view.render();
	},
});
