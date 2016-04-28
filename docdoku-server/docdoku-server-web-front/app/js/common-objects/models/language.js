/*global $,define,App*/
define(['backbone'], function (Backbone) {
    'use strict';
    var Language = Backbone.Model.extend({
        initialize: function () {
            this.className = 'Language';
        }
    });

    Language.getLanguages = function () {
        return $.getJSON(App.config.contextPath + '/api/languages');
    };

    return Language;
});
