/*global define*/
define([
    'backbone',
    "mustache",
    "text!templates/product-instances/product_instance_modal.html",
    "views/baseline/baselined_part_list",
    "common-objects/utils/date"
], function (Backbone, Mustache, template, BaselinedPartListView, date) {
    var ProductInstancesModalView = Backbone.View.extend({
        events: {
            "submit #product_instance_edit_form": "onSubmitForm",
            "hidden #product_instance_modal": "onHidden",
            "click a#previous-iteration": "onPreviousIteration",
            "click a#next-iteration": "onNextIteration"
        },

        template: Mustache.parse(template),

        initialize: function () {
            this.productId = this.options.productId;
            this.iteration = this.model.getLastIteration();
            this.iterations = this.model.getIterations();
        },

        render: function () {
            this.editMode = this.iterations.isLast(this.iteration);
            var data = {
                i18n: App.config.i18n,
                model: this.iteration,
                editMode: this.editMode
            };
            if (this.model.hasIterations()) {
                var hasNextIteration = this.iterations.hasNextIteration(this.iteration);
                var hasPreviousIteration = this.iterations.hasPreviousIteration(this.iteration);
                data.iteration = this.iteration.toJSON();
                data.iteration.hasNextIteration = hasNextIteration;
                data.iteration.hasPreviousIteration = hasPreviousIteration;
                data.iteration.updateDate = date.formatTimestamp(
                    App.config.i18n._DATE_FORMAT,
                    data.iteration.updateDate
                );
            }

            this.$el.html(Mustache.render(template, data));
            this.bindDomElements();
            this.bindUserPopover();
            var that = this;
            this.iteration.initBaselinedParts(that, {success: that.initBaselinedPartListView});
            this.openModal();
            return this;
        },

        onPreviousIteration: function () {
            if (this.iterations.hasPreviousIteration(this.iteration)) {
                this.switchIteration(this.iterations.previous(this.iteration));
            }
            return false;
        },

        onNextIteration: function () {
            if (this.iterations.hasNextIteration(this.iteration)) {
                this.switchIteration(this.iterations.next(this.iteration));
            }
            return false;
        },

        switchIteration: function (iteration) {
            this.iteration = iteration;
            this.undelegateEvents();
            this.closeModal();
            this.delegateEvents();
            this.render();
        },

        bindDomElements: function () {
            this.$modal = this.$("#product_instance_modal");
            this.$inputIterationNote = this.$("#inputIterationNote");
            this.$baselinedPartListArea = this.$("#baselinedPartListArea");
            this.$authorLink = this.$('.author-popover');
        },

        initBaselinedPartListView: function (view) {
            view.baselinePartListView = new BaselinedPartListView({model: view.iteration, isLocked: !view.editMode}).render();
            view.$baselinedPartListArea.html(view.baselinePartListView.$el);
        },

        bindUserPopover: function () {
            this.$authorLink.userPopover(this.model.getUpdateAuthor(), this.model.getSerialNumber(), "right");
        },

        onSubmitForm: function (e) {
            var that = this;
            this.iteration = this.iteration.clone();
            this.iteration.unset("iteration");
            this.iteration.setIterationNote(this.$inputIterationNote.val());
            this.iteration.setBaselinedParts(this.baselinePartListView.getBaselinedParts());
            this.iteration.save(JSON.stringify(this.iteration), "", {
                success: function () {
                    that.model.fetch();
                    that.closeModal();
                },
                error: function (status, err) {
                    alert(err.responseText);
                }
            });

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        onError: function (model, error) {
            alert(App.config.i18n.CREATION_ERROR + " : " + error.responseText);
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
    return ProductInstancesModalView;
});