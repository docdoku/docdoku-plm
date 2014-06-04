define([
    "text!templates/baseline/baseline_duplicate.html",
    "i18n!localization/nls/baseline-strings"
],function(
    template,
    i18n
){
    var BaselineDuplicateView = Backbone.View.extend({
        events: {
            "submit #baseline_duplicate_form" : "onSubmit",
            "hidden #baseline_duplicate_modal": "onHidden"
        },

        template:Mustache.compile(template),

        initialize:function(){
            _.bindAll(this);
        },

        render:function(){
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            this.initValue();
            return this;
        },

        bindDomElements:function(){
            this.$modal = this.$("#baseline_duplicate_modal");
            this.$inputBaselineName = this.$('#inputBaselineName');
            this.$inputBaselineType = this.$('#inputBaselineType');
            this.$inputBaselineDescription = this.$('#inputBaselineDescription');
        },

        initValue:function(){
            this.$inputBaselineType.val(this.model.getType());
            this.$inputBaselineDescription.val(this.model.getDescription());
        },

        onSubmit:function(e){
            var that = this ;
            var data = {
                name : this.$inputBaselineName.val(),
                type : this.model.getType(),
                description: this.$inputBaselineDescription.val()
            };

            this.model.duplicate({
                data:data,
                success:function(baseline){
                    that.closeModal();
                    that.model = baseline;
                },
                error:function(status,err){
                    that.$("#baseline_duplicate_form").after(err.responseText);
                }
            });

            e.preventDefault();
            e.stopPropagation();
            return false;

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

    return BaselineDuplicateView;
});