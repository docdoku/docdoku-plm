/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/part_creation_effectivity_view.html',
    'text!common-objects/templates/part/part_effectivity_serial_number.html',
    'text!common-objects/templates/part/part_effectivity_date.html',
    'text!common-objects/templates/part/part_effectivity_lot.html',
    'common-objects/models/part',
    'common-objects/models/effectivity',
    'common-objects/views/alert'
], function (Backbone, Mustache, template, effectivitySerialNumber, effectivityDate, effectivityLot, Part, Effectivity, AlertView) {
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

            this.showTypeEffectivityFields();

            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.$inputEffectivityName = this.$('#inputEffectivityName');
            this.$inputEffectivityDescription = this.$('#inputEffectivityDescription');
            this.$specificFields = this.$('#specificFields');
        },

        bindDomFieldElements: function() {
            this.$inputProductId = this.$('#inputProductId');
            this.bindTypeahead();
            this.$inputStart = this.$('#inputStart');
            this.$inputEnd = this.$('#inputEnd');
        },

        bindTypeahead: function () {
            var context = this;
            var map = {};
            this.$inputProductId.typeahead({
                source: function (query, process) {
                    var partNumbers = [];

                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/products/numbers?q=' + query, function (data) {
                        _(data).each(function (d) {
                            partNumbers.push(d.id);
                            map[d.id] = d.id;
                        });
                        process(partNumbers);
                    });
                },
                updater: function(item) {
                    context.$inputProductId.val(map[item].id);
                    return item;
                }
            });
        },

        showTypeEffectivityFields: function() {
            var currentType = this.effectivity.typeEffectivity;
            var fields = null;
            switch(currentType) {
              case this.Effectivity.getEffectivityTypeById('SERIALNUMBERBASEDEFFECTIVITY').id:
                fields = effectivitySerialNumber;
                break;
                case this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id:
                fields = effectivityDate;
                this.effectivity.startDate = moment(this.effectivity.startDate).format('YYYY-MM-DD');
                this.effectivity.endDate = moment(this.effectivity.endDate).format('YYYY-MM-DD');
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

        onSubmitForm: function (e) {
            this.$notifications.empty();

            this.updatedEffectivity = {
                name: this.$inputEffectivityName.val(),
                description: this.$inputEffectivityDescription.val(),
                typeEffectivity: this.effectivity.typeEffectivity
            };

            var currentType = this.effectivity.typeEffectivity;
            var start, end;

            switch(currentType) {
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

            if(currentType !== this.Effectivity.getEffectivityTypeById('DATEBASEDEFFECTIVITY').id) {
                this.updatedEffectivity.configurationItemKey = {
                    id: this.$inputProductId.val(),
                    workspace: App.config.workspaceId
                };
                this.updatedEffectivity[start] = this.$inputStart.val();
                this.updatedEffectivity[end] = this.$inputEnd.val();
            } else {
                this.updatedEffectivity[start] = moment(this.$inputStart.val()).format();
                this.updatedEffectivity[end] = moment(this.$inputEnd.val()).format();
            }

            this.updateEffectivity();

            e.preventDefault();
            e.stopPropagation();
            return false;
        },

        updateEffectivity: function() {
            var context = this;
            this.Effectivity.updateEffectivity(this.effectivity.id, this.updatedEffectivity).then(function(data) {
                context.$notifications.append(new AlertView({
                    type: 'success',
                    message: App.config.i18n.UPDATE_EFFECTIVITY_SUCCESS
                }).render().$el);
                context.options.updateCallback();

            }, function(error) {
                context.$notifications.append(new AlertView({
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
