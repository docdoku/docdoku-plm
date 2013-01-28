define(["text!templates/product_creation_view.html", "i18n!localization/nls/product-creation-strings"], function (template, i18n) {

    var ProductCreationView = Backbone.View.extend({

        el: 'div',

        template: Mustache.compile(template),

        initialize: function() {
            this.$modal = this.$('#product_creation_modal');
        },

        render: function() {
            $('body').append(this.template({i18n: i18n}));
            this.openPopup();
            return this;
        },

        openPopup: function() {
            this.$modal.modal('show');
        }

    });

    return ProductCreationView;

});