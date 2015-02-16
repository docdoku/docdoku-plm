/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/component_view.html',
    'common-objects/views/part/substitute_part_view',
    'common-objects/views/part/cad_instance_view'
], function (Backbone, Mustache, template, SubstitutePartView, CadInstanceView) {
    'use strict';
    var ComponentView = Backbone.View.extend({

        events: {
            'click a.remove': 'onRemove',
            'change input[name=amount]': 'changeAmount',
            'change input[name=comment]': 'changeComment',
            'change input[name=number]': 'changeNumber',
            'change input[name=name]': 'changeName',
            'input input[name=newUnit]': 'changeMeasureUnit',
            'click datalist[name=unitMeasure]': 'changeMeasureUnit',
            'click .add-cadInstance': 'addCadInstance',
            'click .collapse-subParts-cadInstances': 'collapseTransformations',
            'click .component': 'selectPart'
        },


        initialize: function () {
            this.$selectPart = false;
            this.collection.bind('add', this.createSubPart, this);
            this.collection.bind('remove', this.removeSubPart, this);
        },


        render: function () {
            var that = this;
            this.substitutePartViews = [];

            this.collection.each(function (model) {
                that.addSubstitutePartsView(model);
            });

            this.$el.html(Mustache.render(template, {
                model: this.model.attributes,
                i18n: App.config.i18n,
                editMode: this.options.editMode
            }));

            this.bindDomElements();
            this.initCadInstanceViews();
            this.initSubstitutePartView();
            this.initUnit();
            return this;
        },

        bindDomElements: function () {
            this.$extraInformation = this.$('.subParts-CADInstance');
            this.$cadInstances = this.$('.cadInstances');
            this.$amount = this.$('input[name=amount]');
            this.$comment = this.$('input[name=comment]');
            this.$unitText = this.$('input[name=newUnit]');
            this.$defaultUnity = this.$unitText.attr('default-unity');
            this.$collapseButton = this.$('.collapse-subParts-cadInstances');

        },

        initCadInstanceViews: function () {
            var self = this;
            _(this.model.get('cadInstances')).each(function (instance) {
                self.addCadInstanceView(instance);
            });
        },

        initSubstitutePartView: function () {
            var self = this;
            _(this.model.get('substitutes')).each(function (instance) {
                self.addSubstitutePartsView(instance);
            });
        },
        initUnit: function () {
            var unit = this.model.get('unit');
            this.$unitText.val(unit ? unit : this.$unitText.attr('default-unity'));
            this.disableEnableAmount(unit);
        },

        addCadInstanceView: function (instance) {
            var self = this;
            var instanceView = new CadInstanceView({editMode: this.options.editMode});
            instanceView.setInstance(instance).render();
            self.$cadInstances.append(instanceView.$el);
            instanceView.on('instance:remove', function () {
                self.onRemoveCadInstance(instance);
            });
        },
        addSubstitutePartsView: function (model) {
            var self = this;
            var substitutePartView = new SubstitutePartView({model: model, editMode: this.options.editMode, removeSubHandler: function () {
                self.collection.remove(model);
                self.model.attributes.substitutes = _(self.model.attributes.substitutes).without(model);
                self.removeSubPart(model);
            }}).render();
            self.substitutePartViews.push(substitutePartView);
            self.$(".substitute-parts").append(substitutePartView.$el);
        },

        onRemove: function () {
            if (this.options.removeHandler && this.options.editMode) {
                this.options.removeHandler();
            }
        },

        onRemoveCadInstance: function (instance) {
            this.model.set('cadInstances', _(this.model.get('cadInstances')).without(instance));
            this.$amount.val(parseInt(this.$amount.val(), 10) - 1);
            this.model.set('amount', this.$amount.val());
            this.model.get('component').amount = this.$amount.val();
        },

        addCadInstance: function () {
            var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
            this.model.get('cadInstances').push(instance);
            this.addCadInstanceView(instance);
            this.$amount.val(parseInt(this.$amount.val(), 10) + 1);
            this.model.set('amount', this.$amount.val());
        },

        createSubPart: function (model) {

            var substitutePart = {
                unit: this.model.get('unit'),
                amount: this.model.get('amount')
            };
            if (!substitutePart.unit || (substitutePart.unit == this.$defaultUnity) && substitutePart.amount > 1) {
                substitutePart.cadInstances = [];
                for (var i = 0; i < substitutePart.amount; i++) {
                    substitutePart.cadInstances.push({tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0});
                }
            } else {
                substitutePart.cadInstances.push({tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0});
            }
            substitutePart.amount = this.model.get('amount');
            substitutePart.unit = this.model.get('unit');

            model.set('amount', this.model.get('amount'));
            model.set('unit', this.model.get('unit'));
            model.set('cadInstances', substitutePart.cadInstances);
            this.$extraInformation.toggle(true);
            this.addSubstitutePartsView(model.attributes);
        },

        removeSubPart: function (modelToRemove) {
            var viewToRemove = _(this.substitutePartViews).select(function (view) {
                return view.model === modelToRemove;
            })[0];

            if (viewToRemove !== null) {
                this.substitutePartViews = _(this.substitutePartViews).without(viewToRemove);
                viewToRemove.remove();
            }

        },

        collapseTransformations: function () {
            var isVisible = this.$extraInformation.is(':visible');
            this.$extraInformation.toggle(!isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-down', isVisible);
            this.$collapseButton.toggleClass('fa-angle-double-up', !isVisible);
        },

        changeAmount: function (e) {
            this.model.set('amount', e.target.value);
            this.model.get('component').amount = e.target.value;
        },
        changeComment: function (e) {
            this.model.set('comment', e.target.value);
        },
        changeNumber: function (e) {
            this.model.get('component').number = e.target.value;
        },
        changeName: function (e) {
            this.model.get('component').name = e.target.value;
        },
        changeMeasureUnit: function (e) {
            this.model.set('unit', (e.target.value == this.$defaultUnity ? '' : e.target.value));
            this.$unitText.val(e.target.value);
            this.disableEnableAmount(e.target.value);
        },
        checkIntegrity: function (unit) {

            if (!unit || unit == this.$defaultUnity) {
                if (parseInt(this.$amount.val(), 10) > this.$('.subParts-CADInstance>.cadInstances >.cadInstance').length) {
                    var totalUnitToAdd = parseInt(this.$amount.val(), 10) - this.$('.subParts-CADInstance>.cadInstances >.cadInstance').length;
                    for (var i = 0; i < totalUnitToAdd; i++) {
                        var instance = {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0};
                        this.model.get('cadInstances').push(instance);
                        this.addCadInstanceView(instance);
                    }
                }
                if (parseInt(this.$amount.val(), 10) < this.$('.subParts-CADInstance>.cadInstances >.cadInstance').length) {
                    var totalToDelete = this.$('.subParts-CADInstance>.cadInstances >.cadInstance').length - parseInt(this.$amount.val(), 10);
                    this.$(".subParts-CADInstance>.cadInstances >.cadInstance").slice(-totalToDelete).remove();
                }
            } else {
                if (this.$('.subParts-CADInstance>.cadInstances >.cadInstance').length > 1) {
                    this.$(".subParts-CADInstance>.cadInstances >.cadInstance:not(:first)").remove();
                    var self = this;
                    _.each(self.model.get('cadInstances'), function () {
                        self.model.get('cadInstances').pop();
                    });

                }
            }
        },
        disableEnableAmount: function (unit) {

            if (unit == "null" || unit == "" || unit == undefined || unit == this.$defaultUnity) {
                this.$amount.val(parseInt(this.$amount.val(), 10) == 0 ? 1 : parseInt(this.$amount.val(), 10));
                this.$amount.attr('disabled', 'disabled');
                this.$('.add-cadInstance').show();
                this.$unitText.val(this.$unitText.attr('default-unity'));
            }
            else {
                this.$amount.removeAttr('disabled');
                this.$('.add-cadInstance').hide();
            }
            this.checkIntegrity(unit);

        },


        selectPart: function () {
            $('.component').toggleClass("selected-part", false);
            this.$selectPart = !this.$selectPart;
            this.$('.component').toggleClass("selected-part", this.$selectPart);
            $("#createPartMenu").toggleClass('hidden', this.$selectPart);
            $("#createSubPartMenu").toggleClass('hidden', !this.$selectPart);
        },
        isSelected: function () {
            return this.$selectPart;
        }


    });

    return ComponentView;
});
