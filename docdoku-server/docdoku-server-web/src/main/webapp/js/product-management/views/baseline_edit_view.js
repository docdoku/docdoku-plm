define(
    [
        "text!templates/baseline_edit.html",
        "i18n!localization/nls/baseline-strings"
    ],function(
        template,
        i18n
        ){

    var BaselineEditView = Backbone.View.extend({

        events: {
            "submit #baseline_edit_form" : "onSubmitForm",
            "hidden #baseline_edit_modal": "onHidden"
        },

        template:Mustache.compile(template),

        initialize:function(){

        },

        render:function(){
            this.$el.html(this.template({i18n: i18n, model: this.model}));
            this.bindDomElements();
            return this ;
        },

        bindDomElements:function(){
            this.$modal = this.$("#baseline_edit_modal");
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

    return BaselineEditView;

});