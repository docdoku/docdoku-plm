/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/substitute_part_view.html',
    'common-objects/views/part/cad_instance_view'
], function (Backbone, Mustache, template, CadInstanceView) {
    'use strict';
    var SubstituteView = Backbone.View.extend({

        events: {
            'click a.removeSub': 'onRemove',
            'change input[name=substitute-amount]': 'changeSubstituteAmount',
            'change input[name=substitute-comment-edit]': 'changeSubstituteComment',
            'change input[name=substitute-name]': 'changeSubstituteName',
            'change input[name=substitute-number]': 'changeSubstituteNumber',
            'input input[name=substitute-newUnit]': 'changeSubstituteMeasureUnit',
            'click datalist[name=substitute-unitMeasure]': 'changeSubstituteMeasureUnit',
            'click .add-substitute-cadInstance': 'addCadInstance',
            'click .collapse-substitute-cadInstance': 'collapseSubstituteTransformations'
        },


        initialize: function () {

        },


        render: function () {
            this.$el.html(Mustache.render(template, {

                model: this.model,
                i18n: App.config.i18n,
                editMode: this.options.editMode
            }));


            this.bindDomElements();
            this.initSubstituteUnitAmount();
            //init the amount first then instanciate the CAD instances
            this.initSubstituteCadInstanceViews();

            return this;
        },

        bindDomElements: function () {
            this.$cadInstances = this.$('.cadInstances');
            this.$amount = this.$('input[name=substitute-amount]');
            this.$comment = this.$('input[name=substitute-comment]');
            this.$unitText = this.$('input[name=substitute-newUnit]');
            this.$defaultUnity = this.$unitText.attr('default-substitute-unity');
            this.$collapseButton = this.$('.collapse-substitute-cadInstance');

        },

        initSubstituteCadInstanceViews: function () {
            var self = this;
            _(this.model.cadInstances).each(function (instance) {
                self.addCadInstanceView(instance);
//                self.$cadInstances.hide();
            });
        },

        initSubstituteUnitAmount: function () {
            var unit = this.model.unit;
            this.$unitText.val(unit ? unit : this.$defaultUnity);
            this.$amount.val(this.model.amount);
            this.disableEnableAmount(unit);

        },

        addCadInstanceView: function (instance) {
            var self = this;
            var substituteCADInstanceView = new CadInstanceView({editMode: this.options.editMode});
            substituteCADInstanceView.setInstance(instance).render();
            self.$cadInstances.append(substituteCADInstanceView.$el);
            substituteCADInstanceView.on('instance:remove', function () {
                self.onRemoveCadInstance(instance);
            });
        },

        onRemove: function () {
            if (this.options.removeSubHandler && this.options.editMode) {
                this.options.removeSubHandler();
            }
        },
        onRemoveCadInstance: function (instance) {
            this.model.cadInstances = _(this.model.cadInstances).without(instance);
            this.$amount.val(parseInt(this.$amount.val(), 10) - 1);
            this.model.amount = this.$amount.val();
        },

        addCadInstance: function () {
            var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
            this.model.cadInstances.push(instance);
            this.addCadInstanceView(instance);
            this.$amount.val(parseInt(this.$amount.val(), 10) + 1);
            this.model.amount = this.$amount.val();
        },

        collapseSubstituteTransformations: function () {
            var isVisible = this.$(".data-sub-part").is(':visible');
            this.$(".data-sub-part").toggle(!isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-down', isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-up', !isVisible);
        },
        changeSubstituteAmount: function (e) {
            this.model.amount = e.target.value;
        },
        changeSubstituteComment: function (e) {
            this.model.referenceDescription = e.target.value;
        },
        changeSubstituteName: function (e) {
            this.model.substitute.name = e.target.value;
        },
        changeSubstituteNumber: function (e) {
            this.model.substitute.number = e.target.value;
        },
        changeSubstituteMeasureUnit: function (e) {
            this.model.unit = (e.target.value == this.$defaultUnity ? '' : e.target.value);
            this.$unitText.val(e.target.value);
            if (e.target.value == this.$defaultUnity) {
                this.model.amount = 1;
                this.$amount.val(1);
            }

            this.disableEnableAmount(e.target.value);
            this.checkIntegrity(e.target.value);
        },
        checkIntegrity: function (unit) {

            if (!unit || unit == this.$defaultUnity) {

                var amount = parseInt(this.model.amount, 10);

                if (amount > this.$('.substitute .cadInstance').length) {
                    var totalUnitToAdd = amount - this.$('.substitute .cadInstance').length;
                    for (var i = 0; i < totalUnitToAdd; i++) {
                        var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
                        this.addCadInstanceView(instance);
                    }
                }

                if (amount < this.$('.substitute .cadInstance').length) {
                    var totalToDelete = this.$('.substitute .cadInstance').length - amount;
                    this.$('.substitute .cadInstance').slice(-totalToDelete).remove();
                }

            } else {
                this.$('.substitute .cadInstance:not(:first)').remove();

                if (this.model.cadInstances.length > 1) {
                    var firstElt = _.first(this.model.cadInstances);
                    var self = this;
                    _.each(this.model.cadInstances, function () {
                        self.model.cadInstances.pop();
                    });

                }

            }


        },
        disableEnableAmount: function (unit) {

            if (!unit || unit == this.$defaultUnity) {
                var amount = parseInt(this.$amount.val(), 10);
                this.$amount.attr('disabled', 'disabled');
                this.$('.add-substitute-cadInstance').show();
                this.$unitText.val(this.$defaultUnity);
            }
            else {
                this.$amount.removeAttr('disabled');
                this.$('.add-substitute-cadInstance').hide();
            }

        }


    });

    return SubstituteView;
});
