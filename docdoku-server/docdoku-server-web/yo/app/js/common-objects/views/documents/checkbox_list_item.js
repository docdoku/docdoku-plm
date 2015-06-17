/*global define*/
define([
    'common-objects/views/components/list_item'
], function (ListItemView) {

    'use strict';

    var CheckboxListItemView = ListItemView.extend({
        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            this.checkToggle = '#check-toggle-' + this.cid;
            this.events['click ' + this.checkToggle] = 'toggle';
            this.isChecked = false;
        },
        rendered: function () {
            // Restore check state
            this.$(this.checkToggle).prop('checked', this.isChecked);
        },
        stateChanged: function () {
            this.trigger(this.isChecked ? 'checked' : 'unchecked');
        },
        toggle: function () {
            // Save check state to restore it after render
            this.isChecked = this.$(this.checkToggle).prop('checked');
            this.stateChanged();
        },
        setCheckState: function (value) {
            this.isChecked = value;
            this.$(this.checkToggle).prop('checked', value);
            this.stateChanged();
        },
        check: function () {
            this.setCheckState(true);
        },
        uncheck: function () {
            this.setCheckState(false);
        }
    });
    return CheckboxListItemView;
});
