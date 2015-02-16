/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/parts_management.html',
    'common-objects/views/part/component_view'
], function (Backbone, Mustache, template, ComponentView) {
    'use strict';
    var PartsManagementView = Backbone.View.extend({

        events: {
            'click #createPart': 'createPart',
            'click #createSubstitutePart': 'createSubstitute'
        },

        initialize: function () {
            this.collection.bind('add', this.addPart, this);
            this.collection.bind('remove', this.removePart, this);
            this.$selectedComponent =null;
        },

        bindTypeahead: function () {

            var that = this;

            this.$('#existingParts').typeahead({
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

            var that = this;
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, editMode: this.options.editMode}));

            this.componentViews = [];

            this.collection.each(function (model) {
                that.addView(model);
            });

            if (this.options.editMode) {
                this.bindTypeahead();
                this.bindTypeaheadSub();
            }

            return this;
        },

        addView: function (model) {
            var that = this;
            this.iteration = this.model.getLastIteration();
            var componentView = new ComponentView({
                model: model, editMode: this.options.editMode,
                collection: new Backbone.Collection(this.iteration.getSubstituteParts()),
                removeHandler: function () {
                    that.collection.remove(model);
                }}).render();
            this.componentViews.push(componentView);
            this.$el.append(componentView.$el);
        },

        removePart: function (modelToRemove) {

            var viewToRemove = _(this.componentViews).select(function (view) {
                return view.model === modelToRemove;
            })[0];

            if (viewToRemove !== null) {
                this.componentViews = _(this.componentViews).without(viewToRemove);
                viewToRemove.remove();
            }
        },

        addPart: function (model) {
            model.set('cadInstances', [
                {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
            ]);
            model.set('substitutes', []);
            this.addView(model);
        },

        createPart: function () {
            var newPart = {
                amount: 1,
                component: {
                    description: '',
                    standardPart: false
                },
                cadInstances: [
                    {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                ],
                unit: this.unit,
                substitutes: []

            };
            this.collection.push(newPart);
        },
        createSubstitute: function () {

            _(this.componentViews).select(function (view) {
                if (view.isSelected()) {
                    var substitutePart = {
                        comment: '',
                        amount: view.model.attributes.amount,
                        substitute: {
                            name: '',
                            number: ""
                        },
                        cadInstances: [
                            {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                        ],
                        unit: view.model.attributes.unit

                    };

                    view.model.get('substitutes').push(substitutePart);
                    view.collection.push(substitutePart);

                }
            })[0];
        },
        bindTypeaheadSub: function () {

            var that = this;
            that.$('#existingSubParts').typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                        var partNumbers = [];
                        that.getSelectedComponent();
                        _(data).each(function (d) {
                            if ((!that.model.get('number')) || (that.model.get('number') !== d.partNumber) && ((!that.$selectedComponent.model.get('component').number) || (that.$selectedComponent.model.get('component').number !== d.partNumber))) {
                                partNumbers.push(d.partNumber);
                            }
                        });
                        process(partNumbers)
                    });
                },
                updater: function (partKey) {
                    var existingPart = {
                        amount: 1,
                        unit: '',
                        substitute: {
                            number: partKey
                        }
                    };

                    that.getSelectedComponent();
                    that.$selectedComponent.model.get('substitutes').push(existingPart);
                    that.$selectedComponent.collection.push(existingPart);

                }
            });
        },

        getSelectedComponent: function () {
            var self = this ;
            _(this.componentViews).select(function (view) {
                if (view.isSelected()) {
                     self.$selectedComponent = view;
                }
            });
        }


    });

    return PartsManagementView;
});
