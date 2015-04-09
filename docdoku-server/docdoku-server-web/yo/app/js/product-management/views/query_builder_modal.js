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
            this.selectizeOptions = {
                plugins: ['remove_button','drag_drop'],
                persist: false,
                valueField: 'name',
                searchField: ['name'],
                options: querybuilderOptions.fields,
                render: {
                    item: function(item, escape) {
                        return '<div><span class="name">' + escape(item.name) + '</span></div>';
                    },
                    option: function(item, escape) {
                        return '<div><span class="label">' + escape(item.name) + '</span></div>';
                    }
                }
            };
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.fillSelectizes();

            this.$('#builder').queryBuilder({
                plugins: [
                    'bt-tooltip-errors'
                ],
                filters: querybuilderOptions.filters

            });
            return this;
        },

        fillSelectizes: function(){
            this.$select.selectize(this.selectizeOptions);
            this.$orderBy.selectize(this.selectizeOptions);
            this.$groupBy.selectize(this.selectizeOptions);
        },

        bindDomElements: function () {
            this.$modal = this.$('#query-builder-modal');
            this.$select = this.$('#select');
            this.$orderBy = this.$('#orderBy');
            this.$groupBy = this.$('#groupBy');
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
