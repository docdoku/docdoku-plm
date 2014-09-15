/*global define*/
define([ 'backbone', "mustache", 'text!templates/bom_item.html', 'common-objects/views/part/part_modal_view'],
    function (Backbone, Mustache, template, PartModalView) {

        var BomItemView = Backbone.View.extend({

            tagName: 'tr',

            events: {
                "click .part_number": "onPartClicked"
            },

            initialize: function () {
                this.listenTo(this.model, "change", this.render);
            },

            render: function () {
                this.$el.html(Mustache.render(template, this.model));
                this.$input = this.$("input");
                this.$(".author-popover").userPopover(this.model.getAuthorLogin(), this.model.getName(), "left");
                if (this.model.isCheckout()) {
                    this.$(".checkout-user-popover").userPopover(this.model.getCheckOutUserLogin(), this.model.getNumber(), "left");
                }
                return this;
            },

            onPartClicked: function () {
                var self = this;
                self.model.fetch().success(function () {
                    new PartModalView({
                        model: self.model
                    }).show();
                });
            },

            isChecked: function () {
                return this.$input[0].checked;
            },

            setSelectionState: function (state) {
                this.$input[0].checked = state;
            }

        });

        return BomItemView;

    });
