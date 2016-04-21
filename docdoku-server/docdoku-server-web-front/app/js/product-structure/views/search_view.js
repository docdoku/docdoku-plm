/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/nav_list_action_bar.html',
    'collections/result_path_collection'
], function (Backbone, Mustache, template, ResultPathCollection) {
	'use strict';
    var SearchView = Backbone.View.extend({
        el: '#nav_list_action_bar',

        events: {
            'submit form#nav_list_search': 'onSearchSubmit'
        },

        initialize: function () {
            this.collection = new ResultPathCollection();
            this.oppened = false;
            this.on('instance:selected', this.onInstanceSelected);
            this.on('selection:reset', this.onResetSelection);
        },

        bindDomElements: function () {
            this.$helpLink = this.$('#nav_list_search_mini_icon i');
            this.$helpPopover = this.$('#nav_list_controls_help');
        },

        initHelpPopover: function () {
            var self = this;
            var $link = this.$helpLink;
            $link.popover({
                html: true,
                placement: 'bottom',
                title: App.config.i18n.SEARCH_OPTIONS,
                trigger: 'manual',
                container:'body',
                content: function () {
                    return self.$helpPopover.html();
                }
            }).click(function (e) {
                $link.popover('show');
                e.stopPropagation();
                e.preventDefault();
                return false;
            });
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.initHelpPopover();
            return this;
        },

        onSearchSubmit: function (e) {
            var searchString = e.target.children[0].value.trim();
            this.search(searchString);
            e.preventDefault();
            return false;
        },

        search: function (partNumber) {
            if (partNumber.length > 0) {
                this.collection.searchString = partNumber.replace(/%/g, '.*');
                this.collection.fetch({reset: true});
            } else {
                this.collection.reset();
            }

            return false;
        },

        onInstanceSelected: function (partNumber) {
            this.search(partNumber);
        },

        onResetSelection: function () {
            this.collection.reset();
        }

    });
    return SearchView;
});
