var TagNavView = BaseView.extend({
	template: "tag-nav-tpl",
	el: "#tag-nav",
	initialize: function () {
		BaseView.prototype.initialize.apply(this, arguments);
		this.events['click [data-target="#items-' + this.cid + '"]'] = "toggle";
		this.render();
	},
	rendered: function () {
		this.tagsView = this.addSubView(new TagListView({
			el: "#items-" + this.cid,
		}));
		this.tagsView.bind("shown", this.onTagsViewShown);
		this.bind("shown", this.shown);
		this.bind("hidden", this.hidden);
	},
	show: function (tag) {
		this.tag = tag;
		this.isOpen = true;
		this.tagsView.showTag(this.tag);
		if (!this.tag) {
			app.router.navigate("tags", {trigger: false});
		}
		this.trigger("shown");
	},
	hide: function () {
		this.isOpen = false;
		this.tagsView.hide();
		app.router.navigate("tags", {trigger: false});
		this.trigger("hidden");
	},
	shown: function () {
		this.$el.addClass("open");
	},
	hidden: function () {
		this.$el.removeClass("open");
	},
	toggle: function () {
		this.isOpen ? this.hide() : this.show();
		$("#nav .active").removeClass("active");
		this.$el.find(".header").first().addClass("active");
		return false;
	},
});
TagNavView = singletonDecorator(TagNavView);
