define(
    [
        "text!templates/baseline_creation_view.html",
        "i18n!localization/nls/baseline-strings"
    ],
    function (template, i18n) {

        var BaselineCreationView = Backbone.View.extend({

            events: {
                "submit #baseline_creation_form" : "onSubmitForm",
                "hidden #baseline_creation_modal": "onHidden"
            },

            template: Mustache.compile(template),

            initialize: function() {
                _.bindAll(this);
            },

            render: function() {
                this.$el.html(this.template({i18n: i18n, model: this.model}));
                this.bindDomElements();
                return this;
            },

            bindDomElements:function(){
                this.$modal = this.$('#baseline_creation_modal');
                this.$inputBaselineName = this.$('#inputBaselineName');
                this.$inputBaselineDescription = this.$('#inputBaselineDescription');
            },

            onSubmitForm: function(e) {
                this.model.createBaseline(
                    {
                        name:this.$inputBaselineName.val(),
                        description:this.$inputBaselineDescription.val()
                    },
                    {
                        success: this.onBaselineCreated,
                        error: this.onError
                    }
                );
                e.preventDefault();
                e.stopPropagation();
                return false ;
            },

            onBaselineCreated: function() {
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

        return BaselineCreationView;

    });