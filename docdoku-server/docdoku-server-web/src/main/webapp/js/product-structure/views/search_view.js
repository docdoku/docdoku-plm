define([
    "text!templates/nav_list_action_bar.html",
    "collections/result_path_collection",
    "i18n!localization/nls/product-structure-strings"
], function(template, ResultPathCollection, i18n) {

    var SearchView = Backbone.View.extend({

        el: '#nav_list_action_bar',

        template: Mustache.compile(template),

        events: {
            "submit form#nav_list_search": "search",
            "click #nav_list_search_mini_icon i": "toggleHelp",
            "click .popover" : "toggleHelp"
        },

        initialize: function() {
            this.collection = new ResultPathCollection();
            this.oppened = false;
        },

        bindDomElements: function() {
            this.$helpLink = this.$("#nav_list_search_mini_icon i");
            this.$helpPopover = this.$("#nav_list_controls_help");
        },

        initHelpPopover: function() {
            var self = this;
            this.$helpLink.popover({
                html: true,
                placement: "bottom",
                title: i18n.SEARCH_OPTIONS,
                trigger: "manual",
                content: function() {
                    return self.$helpPopover.html();
                }
            });
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            this.initHelpPopover();
            return this;
        },

        search: function(e) {

            var searchString = e.target.children[0].value.trim();

            if (searchString.length > 0) {
                this.collection.searchString = searchString;
                this.collection.fetch();
            } else {
                this.collection.reset();
            }

            return false;
        },

        toggleHelp: function() {
            this.oppened ? this.$helpLink.popover('hide') : this.$helpLink.popover('show');
            this.oppened = !this.oppened;
        }

    });

    return SearchView;

});