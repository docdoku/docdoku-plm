/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/query_builder_modal.html',
    'query-builder',
    'selectize'
], function (Backbone, Mustache, template, queryBuilder, selectize) {
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

                filters: [{
                    id: 'name',
                    label: 'Name',
                    type: 'string'
                }, {
                    id: 'category',
                    label: 'Category',
                    type: 'integer',
                    input: 'select',
                    values: {
                        1: 'Books',
                        2: 'Movies',
                        3: 'Music',
                        4: 'Tools',
                        5: 'Goodies',
                        6: 'Clothes'
                    },
                    operators: ['equal', 'not_equal', 'in', 'not_in', 'is_null', 'is_not_null']
                }, {
                    id: 'in_stock',
                    label: 'In stock',
                    type: 'integer',
                    input: 'radio',
                    values: {
                        1: 'Yes',
                        0: 'No'
                    },
                    operators: ['equal']
                }, {
                    id: 'price',
                    label: 'Price',
                    type: 'double',
                    validation: {
                        min: 0,
                        step: 0.01
                    }
                }, {
                    id: 'id',
                    label: 'Identifier',
                    type: 'string',
                    placeholder: '____-____-____',
                    operators: ['equal', 'not_equal'],
                    validation: {
                        format: /^.{4}-.{4}-.{4}$/
                    }
                }]
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
        },


    });



    return QueryBuilderModal
});
