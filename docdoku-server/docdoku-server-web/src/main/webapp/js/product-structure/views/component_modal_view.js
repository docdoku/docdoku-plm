define(['text!templates/component_modal.html', 'i18n!localization/nls/product-structure-strings'], function(template, i18n) {

    var ComponentModalView = Backbone.View.extend({

        template: Mustache.compile(template),

        events: {
            "hidden #component-modal": "onHidden"
        },

        render: function() {
            this.$el.html(this.template({
                component: this.model,
                i18n: i18n
            }));
            this.$modal = this.$('.modal');
            this.$authorLink = this.$('.author-popover');
            this.bindUserPopover();
            return this;
        },

        show: function() {
            this.$modal.modal("show");
        },

        onHidden: function() {
            this.remove();
        },

        bindUserPopover: function() {
            this.$authorLink.userPopover(this.model.getAuthorLogin(), this.model.getNumber(), "right");
        }

    });

    return ComponentModalView;

});