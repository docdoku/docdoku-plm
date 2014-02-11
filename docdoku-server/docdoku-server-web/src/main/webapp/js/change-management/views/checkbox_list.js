define([
	"common-objects/views/components/list"
], function (
	ListView
) {
	var CheckboxListView = ListView.extend({
		initialize: function () {
			ListView.prototype.initialize.apply(this, arguments);
			this.checkToggle = "#check-toggle-" + this.cid;
			this.events["click " + this.checkToggle] = "toggle";
		},
		toggle: function () {
			if ($(this.checkToggle).is(":checked")) {
				_.each(_.values(this.subViews), function (view) {
					view.check();
				});
			} else {
				_.each(_.values(this.subViews), function (view) {
					view.uncheck();
				});
			}
		},
		viewAdded: function (view) {
			var that = this;
			view.on("checked unchecked", function () {
				that.trigger("selectionChange");
			});
		},
		checkedViews: function () {
			return _.filter(_.values(this.subViews),
				function (view) {
					return view.isChecked;
				});
		},
		eachChecked: function (callback) {
			_.each(this.checkedViews(), callback);
		}
	});
	return CheckboxListView;
});
