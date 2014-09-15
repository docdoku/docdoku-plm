/*global define*/
define(
    [
        'backbone',
        "mustache",
        "text!templates/product_details.html",
        "views/baseline/baseline_list",
        "views/baseline/baseline_edit_view"
    ], function (Backbone, Mustache, template, BaselineListView, BaselineEditView) {

        var ProductDetailsView = Backbone.View.extend({

            events: {
                "submit #product_details_form": "onSubmitForm",
                "hidden #product_details_modal": "onHidden"
            },

            template: Mustache.parse(template),

            initialize: function () {

            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n, model: this.model}));
                this.bindDomElements();
                this.initBaselinesView();
                return this;
            },

            bindDomElements: function () {
                this.$modal = this.$("#product_details_modal");
                this.$tabBaselines = this.$("#tab-baselines");
            },

            onSubmitForm: function (e) {

                var baselines = this.baselineListView.getCheckedBaselines();

                if (baselines.length) {
                    var errors = this.model.deleteBaselines(baselines);
                    if (errors.length) {
                        alert("Error on baseline deletion");
                    }
                }

                this.closeModal();
                e.preventDefault();
                e.stopPropagation();
                return false;
            },

            initBaselinesView: function () {
                var that = this;
                this.baselineListView = new BaselineListView({}, {productId: this.model.getId()}).render();
                this.$tabBaselines.append(this.baselineListView.$el);
                this.listenToOnce(this.baselineListView, "baseline:to-edit-modal", function (baseline) {
                    that.closeModal();
                    new BaselineEditView({model: baseline}, {productId: that.model.getId()}).render();
                });
            },

            onError: function (model, error) {
                alert(APP_CONFIG.i18n.CREATION_ERROR + " : " + error.responseText);
            },

            openModal: function () {
                this.$modal.modal('show');
            },

            closeModal: function () {
                this.$modal.modal('hide');
            },

            onHidden: function () {
                this.remove();
            }

        });

        return ProductDetailsView;

    });