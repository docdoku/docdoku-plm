/*global define*/
define([
    "backbone",
    "mustache",
    "text!templates/nav_list_action_bar.html",
    "collections/result_path_collection"
], function (Backbone, Mustache, template, ResultPathCollection) {

    var SearchView = Backbone.View.extend({

        el: '#nav_list_action_bar',

        events: {
            "submit form#nav_list_search": "onSearchSubmit",
            "click #nav_list_search_mini_icon i": "toggleHelp",
            "click .popover": "toggleHelp"
        },

        initialize: function () {
            this.collection = new ResultPathCollection();
            this.oppened = false;
            this.on("instance:selected", this.onInstanceSelected);
            this.on("selection:reset", this.onResetSelection);
        },

        bindDomElements: function () {
            this.$helpLink = this.$("#nav_list_search_mini_icon i");
            this.$helpPopover = this.$("#nav_list_controls_help");
        },

        initHelpPopover: function () {
            var self = this;
            this.$helpLink.popover({
                html: true,
                placement: "bottom",
                title: APP_CONFIG.i18n.SEARCH_OPTIONS,
                trigger: "manual",
                content: function () {
                    return self.$helpPopover.html();
                }
            });
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
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
                this.collection.searchString = partNumber;
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
        },

        toggleHelp: function () {
            if (this.oppened) {
                this.$helpLink.popover('hide');
            } else {
                this.$helpLink.popover('show');
            }
            this.oppened = !this.oppened;
        }
    });

    return SearchView;

});