define(
    ["text!templates/part_creation_view.html",
        "i18n!localization/nls/product-creation-strings",
        "common-objects/models/part"],
    function (template, i18n, Part) {

    var PartCreationView = Backbone.View.extend({

        events: {
            "submit #part_creation_form" : "onSubmitForm",
            "hidden #part_creation_modal": "onHidden"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$('#part_creation_modal');
            this.$inputPartNumber = this.$('#inputPartNumber');
            this.$inputPartName = this.$('#inputPartName');
            this.$inputPartDescription = this.$('#inputPartDescription');
        },

        onSubmitForm: function(e) {
            this.model = new Part({
                number: this.$inputPartNumber.val(),
                workspaceId: APP_CONFIG.workspaceId,
                description: this.$inputPartDescription.val(),
                name:  this.$inputPartName.val()
            });

            this.model.save({}, {
                wait: true,
                success: this.onPartCreated,
                error: this.onError
            });

            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        onPartCreated: function() {
            this.trigger('part:created', this.model);
            this.closeModal();
        },

        onError: function(model, error) {
            alert(i18n.CREATION_ERROR + " : " + error.responseText);
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        }

    });

    return PartCreationView;

});