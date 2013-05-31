define(
    [
        "text!templates/product_details.html",
        "i18n!localization/nls/product-management-strings",
        "views/baseline_list"
    ],function(
        template,
        i18n,
        BaselineListView
        ){

    var ProductDetailsView = Backbone.View.extend({

        events: {
            "submit #product_details_form" : "onSubmitForm",
            "hidden #product_details_modal": "onHidden"
        },

        template:Mustache.compile(template),

        initialize:function(){

        },

        render:function(){
            this.$el.html(this.template({i18n: i18n, model: this.model}));
            this.bindDomElements();
            this.initBaselinesView();
            return this ;
        },

        bindDomElements:function(){
            this.$modal = this.$("#product_details_modal");
            this.$tabBaselines = this.$("#tab-baselines");
        },

        onSubmitForm: function(e) {

            var baselines = this.baselineListView.getCheckedBaselines();

            if(baselines.length){
                var errors = this.model.deleteBaselines(baselines);
                if(errors.length){
                    alert("Error on baseline deletion");
                }
            }

            this.closeModal();
            e.preventDefault();
            e.stopPropagation();
            return false ;
        },

        initBaselinesView: function() {
            this.baselineListView = new BaselineListView({},{productId:this.model.getId()}).render();
            this.$tabBaselines.append(this.baselineListView.$el);
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

    return ProductDetailsView;

});