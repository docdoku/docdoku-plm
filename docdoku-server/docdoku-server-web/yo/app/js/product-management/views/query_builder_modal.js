/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/query_builder_modal.html',
    'query-builder',
    'selectize',
    '../../utils/query-builder-options'
], function (Backbone, Mustache, template, queryBuilder, selectize,querybuilderOptions) {
    'use strict';
    var QueryBuilderModal = Backbone.View.extend({

        events: {
            'hidden #query-builder-modal': 'onHidden'
        },

        initialize: function () {
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();


            this.$('#select').selectize({
                plugins: ['remove_button','drag_drop'],
                persist: false,
                maxItems: null,
                valueField: 'name',
                searchField: ['name'],
                options: [
                    {name: 'foo'},
                    {name: 'bar'},
                    {name: 'foofoo'},
                    {name: 'barbar'}
                ],
                render: {
                    item: function(item, escape) {
                        return '<div>' +
                            (item.name ? '<span class="name">' + escape(item.name) + '</span>' : '') +
                            '</div>';
                    },
                    option: function(item, escape) {
                        var label = item.name;
                        return '<div>' +
                            '<span class="label">' + escape(label) + '</span>' +
                            '</div>';
                    }
                },
                createFilter: function(input) {
                    var match, regex;

                    // email@address.com
                    regex = new RegExp('^' + REGEX_EMAIL + '$', 'i');
                    match = input.match(regex);
                    if (match) return !this.options.hasOwnProperty(match[0]);

                    return false;
                }

            });

            this.$('#builder').queryBuilder({
                plugins: [
                    'bt-tooltip-errors'
                ],

                filters: querybuilderOptions.filters
            });
            return this;
        },

        bindDomElements: function () {
            this.$modal = this.$('#query-builder-modal');
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }


    });



    return QueryBuilderModal
});
