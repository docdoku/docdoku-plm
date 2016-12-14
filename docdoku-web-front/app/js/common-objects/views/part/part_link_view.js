/*global define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/part_link.html',
    'common-objects/views/part/cad_instance_view'
], function (Backbone, Mustache, template, CadInstanceView) {

    'use strict';

    var PartLinkView = Backbone.View.extend({

        events: {
            'change >input[name=amount]': 'changeAmount',
            'change >input[name=reference-description]': 'changeReferenceDescription',
            'change >input[name=number]': 'changeNumber',
            'change >input[name=name]': 'changeName',
            'change >label>input[name=optional]': 'changeIsOptional',
            'change >input[name=unit]': 'changeUnit',
            'click >.add-cad-instance': 'onAddCadInstance',
            'click >.remove-cad-instance': 'onRemoveCadInstance',
            'click >.toggle-cad-instances': 'toggleCadInstances',
            'click >.remove': 'onRemove',
            'click': 'toggleSelected'
        },

        initialize: function () {
            this.cadInstances = new Backbone.Collection(this.model.get('cadInstances'));
            this.cadInstances.bind('add', this.addCadInstance.bind(this));
            this.cadInstances.bind('remove', this.removeCadInstance.bind(this));
            this.cadInstances.bind('reset', this.removeAllCadInstances.bind(this));
            this.model.on('change', this.onModelChanged.bind(this));
        },

        render: function () {

            this.component = this.getComponent();

            var disabled = this.options.editMode ? '' : 'disabled';
            var optionalChecked = this.model.get('optional') ? 'checked' : '';
            var componentDisabled = this.component.number ? 'disabled' : '';

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                editMode: this.options.editMode,
                disabled: disabled,
                optionalChecked: optionalChecked,
                handleSubstitutes: this.handleSubstitutes,
                model: this.model.attributes,
                component: this.component,
                componentDisabled: componentDisabled
            }));

            this.$cadInstances = this.$('.cad-instances');
            this.cadInstances.each(this.addCadInstanceView.bind(this));

            this.$('>[name=unit]').selectize({
                create: true,
                onChange: this.changeUnit.bind(this)
            });

            var unit = this.model.get('unit');

            if (unit) {
                var unitSelectize = this.$('>[name=unit]')[0].selectize;
                unitSelectize.addOption({value: unit, text: unit});
                unitSelectize.setValue(unit);
            }

            this.updateCadInstancesCount();
            this.updateAmountField();

            return this;
        },

        hasUnit: function () {
            return this.model.get('unit');
        },

        onModelChanged: function () {
            this.model.set('id', 0, {silent: true});
            this.model.set('ROTATIONTYPE', 'ANGLE', {silent: true});
        },

        changeAmount: function (e) {
            this.model.set('amount', parseFloat(e.target.value));
        },

        changeReferenceDescription: function (e) {
            this.model.set('referenceDescription', e.target.value);
        },

        changeNumber: function (e) {
            this.component.number = e.target.value;
        },

        changeName: function (e) {
            this.component.name = e.target.value;
        },

        changeIsOptional: function (e) {
            this.model.set('optional', e.target.checked);
        },

        changeUnit: function (unit) {
            this.model.set('unit', unit);
            this.model.trigger('change');
            this.updateAmountField();
            if (this.hasUnit()) {
                this.cadInstances.reset();
            } else {
                this.onAddCadInstance();
            }
        },

        updateAmountField: function () {
            if (this.hasUnit()) {
                this.$('>[name=amount]').removeProp('disabled');
                this.$('>.change-cad-instances-amount').hide();
            } else {
                this.$('>[name=amount]').prop('disabled', 'disabled');
                this.updateAmountChangeIcons();
            }
        },

        onAddCadInstance: function () {
            this.cadInstances.add({tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0});
        },

        addCadInstance: function (cadInstance) {
            this.addCadInstanceView(cadInstance);
            this.updateCadInstances();
            this.onModelChanged();
        },

        updateCadInstances: function () {
            this.model.set('cadInstances', this.cadInstances.models);
            this.updateCadInstancesCount();
        },

        updateCadInstancesCount: function () {
            if (!this.hasUnit()) {
                this.model.set('amount', this.cadInstances.size());
                this.$('>[name=amount]').val(this.cadInstances.size());
                this.updateAmountChangeIcons();
            }
        },

        updateAmountChangeIcons: function () {
            this.$('.add-cad-instance').show();
            if (this.cadInstances.size() <= 1) {
                this.$('.remove-cad-instance').hide();
            } else {
                this.$('.remove-cad-instance').show();
            }
        },

        addCadInstanceView: function (cadInstance) {
            var instanceView = new CadInstanceView({
                editMode: this.options.editMode,
                model: cadInstance
            }).render();
            this.$cadInstances.append(instanceView.$el);
            this.listenTo(cadInstance, 'change', this.updateCadInstances.bind(this));
            this.listenTo(cadInstance, 'change', this.onModelChanged.bind(this));
        },

        removeCadInstance: function () {
            this.updateCadInstances();
        },

        removeAllCadInstances: function () {
            this.model.set('cadInstances', []);
            this.$('>.cad-instances .cad-instance').remove();
        },

        onRemoveCadInstance: function () {
            this.cadInstances.remove(this.cadInstances.at(this.cadInstances.size() - 1));
            this.$('>.cad-instances .cad-instance:last').remove();
            this.updateCadInstances();
        },

        toggleCadInstances: function () {
            this.$el.toggleClass('cad-instances-opened');
        },

        onRemove: function () {
            // Remove by cid, instead of default "remove by id" (our ids are set to 0 on model changed)
            this.model.collection.remove({cid: this.model.cid});
            this.remove();
        },

        toggleSelected: function (e) {
            if (e.target.className.indexOf('component') !== -1 || e.target.parentNode.className === 'cad-instance' || e.target.parentNode.className === 'cad-instances') {
                this.trigger('part-link:toggle-selected', this);
            }
        }

    });

    return PartLinkView;
});
