define([
	"common-objects/views/components/list_item"
], function (
	ListItemView
) {
	var CheckboxListItemView = ListItemView.extend({
		initialize: function () {
			ListItemView.prototype.initialize.apply(this, arguments);
			this.checkToggle = "#check-toggle-" + this.cid
			this.events["click " + this.checkToggle] = "toggle";
			this.isChecked = false;
		},
		rendered: function () {
			// Restore check state
			$(this.checkToggle).attr("checked", this.isChecked);
		},
		stateChanged: function () {
			this.trigger(this.isChecked ? "checked" : "unchecked");
		},
		toggle: function () {
			// Save check state to restore it after render
			this.isChecked = $(this.checkToggle).attr("checked");
			this.stateChanged();
		},
		setCheckState: function (value) {
			this.isChecked = value;
			$(this.checkToggle).attr("checked", value);
			this.stateChanged();
		},
		check: function () {
			this.setCheckState(true);
		},
		uncheck: function () {
			this.setCheckState(false);
		},
	});
	return CheckboxListItemView;
});
