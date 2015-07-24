/*global _,$,define,App*/
define([
    'backbone',
    'mustache',
    'text!common-objects/templates/part/part_assembly.html',
    'common-objects/views/part/part_usage_link_view'
], function (Backbone, Mustache, template, PartUsageLinkView) {

    'use strict';

    var PartAssemblyView = Backbone.View.extend({

        events: {
            'click #create-part-revision-as-part-usage-link': 'createNewPartRevisionAsPartUsageLink',
            'click #create-part-revision-as-part-substitute-link': 'createNewPartRevisionAsPartSubstitutesLink',
            'selectstart': 'preventSelect'
        },

        initialize: function () {
            this.collection.bind('add', this.addPartUsageLink, this);
            this.collection.bind('remove', this.removePartUsageLink, this);
            this.selectedPartUsageLinkView = null;
        },

        preventSelect: function () {
            return false;
        },

        render: function () {

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, editMode: this.options.editMode}));
            this.$components = this.$('.components');

            this.collection.each(this.addPartUsageLinkView.bind(this));

            if (this.options.editMode) {
                this.bindPartUsageLinkTypeAhead();
                this.bindPartSubstituteLinkTypeAhead();
            }

            return this;
        },

        addPartUsageLinkView: function (partUsageLink) {

            var partUsageLinkView = new PartUsageLinkView({
                model: partUsageLink,
                editMode: this.options.editMode
            }).render();

            this.$components.append(partUsageLinkView.$el);
            this.listenTo(partUsageLinkView, 'part-link:toggle-selected', this.togglePartUsageLinkSelected.bind(this));

        },

        unSelectView: function () {
            this.$('.component.selected').removeClass('selected');
            this.selectedPartUsageLinkView = null;
            this.$el.removeClass('component-selected');
        },

        togglePartUsageLinkSelected: function (view) {
            if (view === this.selectedPartUsageLinkView) {
                this.unSelectView();
            } else {
                this.$('.component.selected').removeClass('selected');
                view.$el.addClass('selected');
                this.selectedPartUsageLinkView = view;
                this.$el.addClass('component-selected');
            }
        },

        addPartUsageLink: function (model) {
            this.addPartUsageLinkView(model);
        },

        removePartUsageLink: function (model) {
            if (this.selectedPartUsageLinkView && this.selectedPartUsageLinkView.model === model) {
                this.unSelectView();
            }
        },

        createNewPartRevisionAsPartUsageLink: function () {
            this.collection.push({
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
            });
        },

        createNewPartRevisionAsPartSubstitutesLink: function () {
            this.selectedPartUsageLinkView.substitutes.add({
                amount: this.selectedPartUsageLinkView.model.get('amount'),
                unit: this.selectedPartUsageLinkView.model.get('unit'),
                substitute: {
                    standardPart: false
                },
                cadInstances: [
                    {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                ]
            });
        },

        bindPartUsageLinkTypeAhead: function () {

            var collection = this.collection;

            this.$('#part-usage-link-type-ahead').typeahead({
                source: this.source.bind(this),
                updater: function (part) {
                    collection.push({
                        amount: 1,
                        component: {
                            number: part.split('<')[1].replace('>', '').trim(),
                            name: part.split('<')[0].trim()
                        },
                        substitutes: [],
                        cadInstances: [
                            {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                        ]
                    });
                }
            });
        },

        bindPartSubstituteLinkTypeAhead: function () {
            var that = this;
            this.$('#part-substitute-link-type-ahead').typeahead({
                source: this.source.bind(this),
                updater: function (part) {
                    that.selectedPartUsageLinkView.substitutes.add({
                        amount: 1,
                        unit: '',
                        substitute: {
                            number: part.split('<')[1].replace('>', '').trim(),
                            name: part.split('<')[0].trim()
                        },
                        cadInstances: [
                            {tx: 0, ty: 0, tz: 0, rx: 0, ry: 0, rz: 0}
                        ]
                    });
                }
            });
        },

        source: function (query, process) {
            var model = this.model;
            $.getJSON(App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/parts/numbers?q=' + query, function (data) {
                var partNumbers = [];
                _(data).each(function (d) {
                    if ((!model.getNumber()) || (model.getNumber() !== d.partNumber)) {
                        partNumbers.push(d.partName + ' < ' + d.partNumber + ' >');
                    }
                });
                process(partNumbers);
            });
        }

    });

    return PartAssemblyView;
});
