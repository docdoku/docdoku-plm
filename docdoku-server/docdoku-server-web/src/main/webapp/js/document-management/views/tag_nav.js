define([
	"common-objects/common/singleton_decorator",
	"common-objects/views/base",
	"views/tag_list",
	"text!templates/tag_nav.html"
], function (
	singletonDecorator,
	BaseView,
	TagListView,
	template
) {

	var TagNavView = BaseView.extend({
		template: Mustache.compile(template),
		el: "#tag-nav",
		initialize: function () {
			BaseView.prototype.initialize.apply(this, arguments);
			var toggleTarget = '[data-target="#items-' + this.cid + '"]';
			this.events['click ' + toggleTarget] = "toggle";

            var that = this ;

            Backbone.Events.on("refreshTagNavViewCollection",function(){
                that.tagsView.collection.fetch();
            });

			this.render();
		},
		rendered: function () {
			this.tagsView = this.addSubView(
				new TagListView({
					el: "#items-" + this.cid,
				})
			);
			this.tagsView.bind("shown", this.onTagsViewShown);
			this.bind("shown", this.shown);
			this.bind("hidden", this.hidden);
		},
		show: function (tag) {
			this.tag = tag;
			this.isOpen = true;
			this.tagsView.showTag(this.tag);
			if (!this.tag) {
				this.router.navigate("tags", {trigger: false});
			}
			this.trigger("shown");
		},
		hide: function () {
			this.isOpen = false;
			this.tagsView.hide();
			this.router.navigate("tags", {trigger: false});
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
			$("#document-menu .active").removeClass("active");
			this.$el.find(".header").first().addClass("active");
			return false;
		},
	});
	TagNavView = singletonDecorator(TagNavView);
	return TagNavView;
});
