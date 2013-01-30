define(["text!modules/product-creation-module/templates/product_creation_view.html", "i18n!localization/nls/product-creation-strings", "modules/product-creation-module/models/configuration_item"], function (template, i18n, ConfigurationItem) {

    var ProductCreationView = Backbone.View.extend({

        tagName: 'div',

        events: {
            "hidden #product_creation_modal": "onHidden",
            "submit #product_creation_form" : "onSubmitForm"
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

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
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

        onSubmitForm: function(e) {
            e.preventDefault();
            e.stopPropagation();
            var configurationItem = new ConfigurationItem({
                id: this.$inputProductId.val(),
                workspaceId: APP_CONFIG.workspaceId,
                description: this.$inputDescription.val(),
                designItemNumber: this.$inputPartNumber.val()
            });
            configurationItem.save({}, {wait: true});
            this.trigger('product:created');
            this.closeModal();
        }

    });

    return ProductCreationView;

});