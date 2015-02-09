/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/substitute_part_view.html',
    'common-objects/views/part/cad_instance_view'
], function (Backbone, Mustache, template,CadInstanceView) {
    'use strict';
    var SubstituteView = Backbone.View.extend({

        events: {
            'click remove-substitute': 'removeSubstitutePart',
            'change input[name=substitute-amount]': 'changeSubstituteAmount',
            'change input[name=substitute-comment]': 'changeSubstituteComment',
            'change input[name=substitute-number]': 'changeSubstituteNumber',
            'change input[name=substitute-name]': 'changeSubstituteName',
            'input input[name=substitute-newUnit]': 'changeSubstituteMeasureUnit',
            'click datalist[name=substitute-unitMeasure]': 'changeSubstituteMeasureUnit',
            'click .add-substitute-cadInstance': 'addSubstituteCadInstance',
            'click .collapse-substitute-cadInstance': 'collapseSubstituteTransformations'
        },


        initialize: function () {

        },

        setInstance: function (instance) {
            this.instance = instance;
            return this;
        },

        bindTypeahead: function () {

            var that = this;

            this.$('#existingSubstituteParts').typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                        var partNumbers = [];
                        _(data).each(function (d) {
                            if ((!that.model.getNumber()) || (that.model.getNumber() !== d.partNumber)) {
                                partNumbers.push(d.partNumber);
                            }
                        });
                        process(partNumbers);
                    });
                },
                updater: function (partKey) {
                    var existingPart = {
                        amount: 1,
                        component: {
                            number: partKey
                        }
                    };

                    that.collection.push(existingPart);

                }
            });
        },



        render: function () {
            this.$el.html(Mustache.render(template, {
                model: this.model.attributes,
                i18n: App.config.i18n,
                editMode: this.options.editMode
            }));

            this.bindTypeahead();
            this.bindDomElements();
            this.initSubstituteCadInstanceViews();
            this.initSubstituteUnitAmount();
            return this;
        },

        bindDomElements: function () {
            this.$cadInstances = this.$('.substitute .cadInstances');
            this.$amount = this.$('input[name=substitute-amount]');
            this.$comment = this.$('input[name=substitute-comment]');
            this.$unitText = this.$('input[name=substitute-newUnit]');
            this.$collapseButton = this.$('.collapse-substitute-cadInstance');
        },

        initSubstituteCadInstanceViews: function () {
            var self = this;
            _(this.instance.cadInstances).each(function (instance) {
                self.addCadInstanceView(instance);
                self.$cadInstances.hide();
            });
        },

        initSubstituteUnitAmount: function () {
            var unit = this.instance.unit;
            this.$unitText.val(unit ? unit : this.$unitText.attr('substitute-default-unity'));
            this.disableEnableAmount(unit);
            this.$amount.val(this.instance.amount);
        },

        addCadInstanceView: function (instance) {
            var self = this;
            var instanceView = new CadInstanceView();
            instanceView.setInstance(instance).render();
            self.$cadInstances.append(instanceView.$el);
            instanceView.on('instance:remove', function () {
                self.onRemoveCadInstance(instance);
            });
        },



        removeSubstitutePart: function () {

        },

        onRemoveCadInstance: function (instance) {
            this.model.set('substitute-cadInstances', _(this.model.get('substitute-cadInstances')).without(instance));
            this.$amount.val(parseInt(this.$amount.val(), 10) - 1);
            this.instance.amount= this.$amount.val();
            this.model.get('component').amount = this.$amount.val();
        },

        addCadInstance: function () {
            var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
            this.model.get('substitute-cadInstances').push(instance);
            this.addCadInstanceView(instance);
            this.$amount.val(parseInt(this.$amount.val(), 10) + 1);
            this.instance.amount= this.$amount.val();
        },

        collapseSubstituteTransformations: function () {
            var isVisible = this.$cadInstances.is(':visible');
            this.$cadInstances.toggle(!isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-down', isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-up', !isVisible);
        },
        changeSubstituteAmount: function (e) {
            this.instance.amount= e.target.value;
        },
        changeSubstituteComment: function (e) {
            this.instance.comment= e.target.value;
        },
        changeSubstituteNumber: function (e) {
            this.instance.number = e.target.value;
        },
        changeSubstituteName: function (e) {
            this.instance.name = e.target.value;
        },
        changeSubstituteMeasureUnit: function (e) {
            this.instance.unit=e.target.value;
            this.$unitText.val(e.target.value);
            this.disableEnableAmount(e.target.value);
        },
        checkIntegrity: function (unit) {

            if (!unit || unit == this.$defaultUnity) {

                var amount = parseInt(this.$amount.val(), 10);

                if (amount > this.$('.cadInstance').length) {
                    var totalUnitToAdd = amount - this.$('.cadInstance').length;
                    for (var i = 0; i < totalUnitToAdd; i++) {
                        var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
                        this.model.get('cadInstances').push(instance);
                        this.addCadInstanceView(instance);
                    }
                }

                if (amount < this.$('.cadInstance').length) {
                    var totalToDelete = this.$('.cadInstance').length - amount;
                    this.$('.cadInstance').slice(-totalToDelete).remove();
                }

            } else  if (this.$('.cadInstance').length > 1) {
                this.$('.cadInstance:not(:first)').remove();
                _.each(this.model.get('cadInstances'), function () {
                    this.model.get('cadInstances').pop();
                },this);
            }

        },
        disableEnableAmount: function (unit) {

            if (!unit || unit == this.$defaultUnity) {
                var amount = parseInt(this.$amount.val(), 10);
                this.$amount.val(amount >= 0 ? 1 : amount);
                this.$amount.attr('disabled', 'disabled');
                this.$('.add-cadInstance').show();
                this.$unitText.val(this.$unitText.attr('default-unity'));
            }
            else {
                this.$amount.removeAttr('disabled');
                this.$('.add-cadInstance').hide();
            }

            this.checkIntegrity(unit);

        }


    });

    return SubstituteView;
});
