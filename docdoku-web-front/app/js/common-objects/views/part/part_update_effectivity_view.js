/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'moment',
    'text!common-objects/templates/part/part_creation_effectivity_view.html',
    'text!common-objects/templates/part/part_effectivity_serial_number.html',
    'text!common-objects/templates/part/part_effectivity_date.html',
    'text!common-objects/templates/part/part_effectivity_lot.html',
    'common-objects/models/part',
    'common-objects/models/effectivity',
    'common-objects/views/alert'
], function (Backbone, Mustache, moment, template, effectivitySerialNumber, effectivityDate, effectivityLot, Part, Effectivity, AlertView) {
    'use strict';
    var PartCreationView = Backbone.View.extend({

        events: {
            'submit #part_creation_effectivity_form': 'onSubmitForm'
        },

        initialize: function () {
            this.model = this.options.model;
            this.Effectivity = new Effectivity();
            this.selectedPart = this.options.selectedPart;
            this.effectivityTypes = this.Effectivity.effectivityTypes;
            this.effectivity = this.options.effectivity;
            _.bindAll(this);
        },

        render: function () {
            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                isNewEffectivity: false,
                effectivity: this.effectivity,
                typeEffectivity: App.config.i18n[this.Effectivity.getEffectivityTypeById(this.effectivity.typeEffectivity).name]
            }));
            this.bindDomElements();
            this.setConfigurationRequiredState();

            this.showTypeEffectivityFields();

            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$inputEffectivityName = this.$('#inputEffectivityName');
            this.$inputEffectivityDescription = this.$('#inputEffectivityDescription');
            this.$specificFields = this.$('#specificFields');
            this.$inputProductId = this.$('#inputProductId');
        },

        bindDomFieldElements: function () {
            this.bindTypeahead();
            this.$inputStart = this.$('#inputStart');
            this.$inputEnd = this.$('#inputEnd');
        },

        bindTypeahead: function () {
            var self = this;
            var map = {};
            this.$inputProductId.typeahead({
                source: function (query, process) {
                    var productIds = [];
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/numbers?q=' + query, function (data) {
                        _(data).each(function (d) {
                            productIds.push(d.id);
                            map[d.id] = d.id;
                        });
                        process(productIds);
                    });
                },
                updater: function (item) {
                    self.$inputProductId.val(map[item].id);
                    return item;
                }
            });
        },

        showTypeEffectivityFields: function () {
            var currentType = this.effectivity.typeEffectivity;
            var fields = null;
            switch (currentType) {
                case this.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                    fields = effectivitySerialNumber;
                    break;
                case this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                    fields = effectivityDate;
                    this.effectivity.startDate = moment(this.effectivity.startDate).format('YYYY-MM-DD');
                    this.effectivity.endDate = this.effectivity.endDate ? moment(this.effectivity.endDate).format('YYYY-MM-DD') : null;
                    break;
                case this.Effectivity.getEffectivityTypeById('LOTBASEDEFFECTIVITY').id:
                    fields = effectivityLot;
                    break;
            }
            this.$specificFields.html(Mustache.render(fields, {
                i18n: App.config.i18n,
                effectivity: this.effectivity
            }));
            this.bindDomFieldElements();
        },

        setConfigurationRequiredState: function () {
            var notRequiredTypes = ['DATEBASEDEFFECTIVITY'];
            var isRequired = notRequiredTypes.indexOf(this.effectivity.typeEffectivity) === -1;
            this.$inputProductId.attr('required', isRequired);
        },

        onSubmitForm: function (e) {
            this.$notifications.empty();

            this.updatedEffectivity = {
                name: this.$inputEffectivityName.val(),
                description: this.$inputEffectivityDescription.val(),
                typeEffectivity: this.effectivity.typeEffectivity
            };

            var currentType = this.effectivity.typeEffectivity;
            var start, end;

            switch (currentType) {
                case this.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                    start = 'startNumber';
                    end = 'endNumber';
                    break;
                case this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                    start = 'startDate';
                    end = 'endDate';
                    break;
                case this.Effectivity.getEffectivityTypeById('LOTBASEDEFFECTIVITY').id:
                    start = 'startLotId';
                    end = 'endLotId';
                    break;
            }

            if (currentType === this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id) {
                var endInputValue = this.$inputEnd.val();
                this.updatedEffectivity[start] = moment(this.$inputStart.val()).format();
                this.updatedEffectivity[end] = endInputValue ? moment(endInputValue).format() : null;
            } else {
                this.updatedEffectivity[start] = this.$inputStart.val();
                this.updatedEffectivity[end] = this.$inputEnd.val();
            }

            var inputProductValue = this.$inputProductId.val();

            this.updatedEffectivity.configurationItemKey = inputProductValue ? {
                id: inputProductValue,
                workspace: App.config.workspaceId
            } : null;

            this.updateEffectivity();

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        updateEffectivity: function () {
            var self = this;
            this.Effectivity.updateEffectivity(this.effectivity.id, this.updatedEffectivity).then(function () {
                self.$notifications.append(new AlertView({
                    type: 'success',
                    message: App.config.i18n.UPDATE_EFFECTIVITY_SUCCESS
                }).render().$el);
                self.options.updateCallback();

            }, function (error) {
                self.$notifications.append(new AlertView({
                    type: 'error',
                    message: error ? error.responseText : App.config.UPDATE_EFFECTIVITY_ERROR
                }).render().$el);
            });
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },

        openModal: function () {
            this.$el.one('shown', this.render.bind(this));
            this.$el.one('hidden', this.onHidden.bind(this));
            this.$el.modal('show');
        },

        closeModal: function () {
            this.$el.modal('hide');
        },

        onHidden: function () {
            this.remove();
        }

    });

    return PartCreationView;

});
