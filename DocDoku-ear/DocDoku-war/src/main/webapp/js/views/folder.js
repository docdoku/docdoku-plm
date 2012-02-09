var FolderView = Backbone.View.extend({
	initialize: function () {
		_.bindAll(this, "render");
	},
	render: function () {
		console.debug('rendering folder ' + this.model.id);
		$(this.el).html('<a href="#folders/' + this.model.id + '">' + this.model.id + '</a>');
		return this;
	}
});

var FoldersView = Backbone.View.extend({
	events: {
		"click #folders-header": "toggle",
	},
	initialize: function () {
		_.bindAll(this,
			"render",
			"onCollectionReset",
			"toggle");
		this.views = {};
		this.collection.bind("reset", this.onCollectionReset);
	},
	onCollectionReset: function () {
		console.debug('fetching collection ' + this.collection.url);
		var that = this;
		this.collection.each(function (folder) {
			if (that.views[folder.id] == undefined) {
				console.debug('creating view for folder ' + folder.id);
				view = new FolderView({
					el: $('li'),
					model: folder
				})
				that.views[folder.id] = view;
				$("#folders-content").append(view.el);
			}
		});
		this.render();
	},
	render: function () {
		_.each(this.views, function(view) {
			view.render();
		});
		return this;
	},
	toggle : function () {
		this.collection.fetch();
	}
});
