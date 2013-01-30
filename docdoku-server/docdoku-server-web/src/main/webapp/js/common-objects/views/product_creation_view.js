define(["text!common-objects/templates/product_creation_view.html", "i18n!localization/nls/product-creation-strings"], function (template, i18n) {

    var ProductCreationView = Backbone.View.extend({

        tagName: 'div',

        events: {
            "hidden #product_creation_modal": "onHidden",
            "click .btn-primary" : "onCreateProduct"
        },

        template: Mustache.compile(template),

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.$modal = this.$('#product_creation_modal');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputProductId = this.$('#inputProductId');
            this.$inputDescription = this.$('#inputDescription');
            this.bindTypeahead();
            return this;
        },

        openPopup: function() {
            this.$modal.modal('show');
        },

        onHidden: function() {
            this.remove();
        },

        bindTypeahead: function() {
            this.$inputPartNumber.typeahead({
                source: function(query, process) {
                    $.getJSON('/api/workspaces/' + APP_CONFIG.workspaceId + '/parts?q=' + query, function(data) {
                        process(data);
                    })
                }
            });
        },

        onCreateProduct: function() {
            this.$modal.modal('hide');
            return false;
        }

    });

    return ProductCreationView;

});