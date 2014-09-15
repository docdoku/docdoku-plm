/*global define*/
define([
    "common-objects/views/base",
    "text!templates/alert.html"
], function (BaseView, template) {
    var AlertView = BaseView.extend({
        template: template
    });
    return AlertView;
});
