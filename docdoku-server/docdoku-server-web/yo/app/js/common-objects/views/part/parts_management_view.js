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
            'click #createSubstitutePart': 'createSubstitutePart',
            'click  #component': 'undoSelectPart'
        },

        initialize: function () {
            this.collection.bind('add', this.addPart, this);
            this.collection.bind('remove', this.removePart, this);
            this.$selectedComponent = null;
        },

        bindTypeahead: function () {

            var that = this;

            this.$('#existingParts').typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                        var partNumbers = [];
                        _(data).each(function (d) {
                            if ((!that.model.getNumber()) || (that.model.getNumber() !== d.partNumber)) {
                                partNumbers.push(d.partNumber+", "+d.partName);
                            }
                        });
                        process(partNumbers);
                    });
                },
                updater: function (part) {
                    var existingPart = {
                        amount: 1,
                        component: {
                            number: part.split(",")[0],
                            name: part.split(",")[1].trim()
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
                model: model, editMode: this.options.editMode,isReleased:this.options.isReleased,
                otherComponentViews: new Backbone.Collection(this.componentViews),
                collection: new Backbone.Collection(this.iteration.getSubstituteParts()),
                removeHandler: function () {
                    that.collection.remove(model);
                },undoSelect: function(viewToSelect){
                    _(that.componentViews).each(function (view) {
                        if (viewToSelect != view){
                            view.$selectPart = false;
                        }
                    });
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
                viewToRemove.undoSelectPart();
            }
        },

        addPart: function (model) {
            model.set('cadInstances', [
                {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
            ]);
            model.set('substitutes', []);
            model.set('amount',1);
            this.addView(model);
        },

        createPart: function () {
            var newPart = {
                amount: 1,
                component: {
                    standardPart: false
                },
                cadInstances: [
                    {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                ],
                unit: this.unit,
                optional: false,
                substitutes: []

            };
            this.collection.push(newPart);
        },
        createSubstitutePart: function () {
            var that = this;
            that.getSelectedComponent();
            var substitutePart = {
                amount: that.$selectedComponent.model.attributes.amount,
                substitute: {
                    standardPart: false
                },
                cadInstances: [
                    {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                ],
                unit: that.$selectedComponent.model.attributes.unit

            };
            that.$selectedComponent.collection.push(substitutePart);


        },
        bindTypeaheadSub: function () {
            var that = this;
            that.$('#existingSubParts').typeahead({
                source: function (query, process) {
                    $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                        var partNumbers = [];
                        that.getSelectedComponent();
                        _(data).each(function (d) {
                            if ((!that.model.get('number')) || (that.model.get('number') !== d.partNumber)) {
                                partNumbers.push(d.partNumber+", "+d.partName);
                            }
                        });
                        process(partNumbers)
                    });
                },
                updater: function (part) {
                    var existingPart = {
                        amount: 1,
                        unit: '',
                        substitute: {
                            number: part.split(",")[0],
                            name: part.split(",")[1].trim()
                }, cadInstances: [
                            {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                        ]
                    };
                    that.getSelectedComponent();
                    that.$selectedComponent.collection.push(existingPart);

                }
            });
        },

        getSelectedComponent: function () {
            var self = this;
            _(this.componentViews).select(function (view) {
                if (view.isSelected()) {
                    self.$selectedComponent = view;
                }
            })[0];
        }



    });

    return PartsManagementView;
});
