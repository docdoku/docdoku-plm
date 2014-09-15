/*global define*/
define(
    [
        'backbone',
        "mustache",
        "text!common-objects/templates/tags/tag.html"
    ],
    function (Backbone, Mustache, template) {

        var TagView = Backbone.View.extend({

            tagName: "li",
            className: "pull-left well",

            initialize: function () {

                if (this.options.isAdded) {
                    this.events = { "click a": "clicked" };
                }
                else if (this.options.isAvailable) {
                    this.events = { "click a": "cross_clicked",
                        "click": "clicked" };
                } else {
                    this.events = { "click": "clicked" };
                }

                this.isRemovable = this.options.isAdded || this.options.isAvailable;

                return this;
            },

            render: function () {
                this.$el.html(Mustache.render(template, {tag: this.model, isRemovable: this.isRemovable}));
                return this;
            },

            clicked: function () {
                if (this.options.clicked) {
                    this.options.clicked();
                }
            },

            cross_clicked: function (e) {
                if (this.options.cross_clicked) {
                    this.options.cross_clicked();
                }
                e.stopPropagation();
            }

        });

        return TagView;
    });