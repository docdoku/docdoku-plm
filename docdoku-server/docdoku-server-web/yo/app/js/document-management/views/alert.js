/*global define*/
define([
    'common-objects/views/base',
    'text!templates/alert.html'
], function (BaseView, template) {
    'use strict';
    var AlertView = BaseView.extend({
        template: template
    });
    return AlertView;
});
