/*global _,define,App,$*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/product_baselines',
    'common-objects/collections/product_instances',
    'text!templates/baselines/baseline_select.html'
], function (Backbone, Mustache, ProductBaselines, ProductInstances, template) {
    'use strict';

    var BaselineSelectView = Backbone.View.extend({

        events: {
            'change #config_spec_type_selector_list': 'onTypeChanged',
            'change #latest_selector_list': 'changeLatest',
            'change #baseline_selector_list': 'changeBaseline',
            'change #product_instance_selector_list': 'changeInstance',
            'change #path_to_path_link_selector_list': 'changePathToPathLink',
            'click .toggle_substitutes': 'toggleSubstitutes'
        },

        availableFilters: ['wip', 'latest', 'latest-released'],

        type: 'product',

        initialize: function () {
            this.baselineCollection = new ProductBaselines({}, {productId: App.config.productId});
            this.listenToOnce(this.baselineCollection, 'reset', this.onBaselineCollectionReset);
            this.productInstanceCollection = new ProductInstances({}, {productId: App.config.productId});
            this.listenToOnce(this.productInstanceCollection, 'reset', this.onProductInstanceCollectionReset);
            this.showSubstitutes = true;
        },

        render: function () {

            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();

            this.$selectBaselineSpec.hide();
            this.$selectProdInstSpec.hide();

            this.baselineCollection.fetch({reset: true});
            this.productInstanceCollection.fetch({reset: true});

            if (_.contains(this.availableFilters, App.config.productConfigSpec)) {
                this.$selectLatestFilter.val(App.config.productConfigSpec);
            }

            this.fetchPathToPathLinkTypes();

            return this;
        },

        bindDomElements: function () {
            // Main selector
            this.$selectConfSpec = this.$('#config_spec_type_selector_list');
            // Sub selectors
            this.$selectLatestFilter = this.$('#latest_selector_list');
            this.$selectBaselineSpec = this.$('#baseline_selector_list');
            this.$selectProdInstSpec = this.$('#product_instance_selector_list');
            this.$selectPathToPathLink = this.$('#path_to_path_link_selector_list');
            this.$divergeSwitchContainer = this.$('#diverge-selector');
            this.$toggleSubstitutes = this.$('.toggle_substitutes');
        },

        onBaselineCollectionReset: function () {

            var selected;

            this.baselineCollection.each(function (baseline) {
                this.$selectBaselineSpec.append('<option value="' + baseline.getId() + '">' + baseline.getName() + '</option>');
                if (App.config.productConfigSpec === '' + baseline.getId()) {
                    selected = baseline;
                }
            }, this);

            this.$selectConfSpec.find('[value="baseline"]').prop('disabled', !this.baselineCollection.size());

            if (selected) {
                this.$selectConfSpec.val('baseline');
                this.$selectProdInstSpec.hide();
                this.$selectLatestFilter.hide();
                this.$selectBaselineSpec.val(selected.getId()).show();
                this.$divergeSwitchContainer.hide();
                this.setDescription(selected.getDescription());
            }
        },

        onProductInstanceCollectionReset: function () {
            var selected;
            this.productInstanceCollection.each(function (productInstance) {
                this.$selectProdInstSpec.append('<option value="pi-' + productInstance.getSerialNumber() + '">' + productInstance.getSerialNumber() + '</option>');
                if (App.config.productConfigSpec === 'pi-' + productInstance.getSerialNumber()) {
                    selected = productInstance;
                }
            }, this);

            this.$selectConfSpec.find('[value="serial-number"]').prop('disabled', !this.productInstanceCollection.size());

            if (selected) {
                this.$selectConfSpec.val('serial-number');
                this.$selectBaselineSpec.hide();
                this.$selectLatestFilter.hide();
                this.$divergeSwitchContainer.hide();
                this.$selectProdInstSpec.val('pi-' + selected.getSerialNumber()).show();
                this.setDescription('');
            }
        },

        onTypeChanged: function () {
            this.$selectConfSpec.show();
            App.config.linkType = null;
            var selectedType = this.$selectConfSpec.val();

            if (selectedType === 'latest-filters') {
                this.changeLatest();
                this.fetchPathToPathLinkTypes();

            } else if (selectedType === 'baseline') {
                this.changeBaseline();

            } else if (selectedType === 'serial-number') {
                this.changeInstance();
            }
        },

        isSerialNumberSelected: function () {
            return this.$selectConfSpec.val() === 'serial-number';
        },

        isBaselineSelected: function () {
            return this.$selectConfSpec.val() === 'baseline';
        },

        changeLatest: function () {
            this.$selectBaselineSpec.hide();
            this.$selectProdInstSpec.hide();
            this.$selectLatestFilter.show();
            this.$divergeSwitchContainer.show();
            this.trigger('config_spec:changed', this.$selectLatestFilter.val());
            this.setDescription('');
        },

        changeBaseline: function () {
            this.$selectProdInstSpec.hide();
            this.$selectLatestFilter.hide();
            this.$selectBaselineSpec.show();
            this.$divergeSwitchContainer.hide();

            var baseline = this.baselineCollection.findWhere({id: parseInt(this.$selectBaselineSpec.val(), 10)});
            this.setDescription(baseline ? baseline.getDescription() : '');

            App.config.linkType = null;
            this.fetchPathToPathLinkTypes();
            this.trigger('config_spec:changed', this.$selectBaselineSpec.val());
        },

        changeInstance: function () {
            this.$selectBaselineSpec.hide();
            this.$selectLatestFilter.hide();
            this.$selectProdInstSpec.show();
            this.$divergeSwitchContainer.hide();

            this.setDescription('');

            App.config.linkType = null;
            this.fetchPathToPathLinkTypes();
            this.trigger('config_spec:changed', this.$selectProdInstSpec.val());
        },

        fetchPathToPathLinkTypes: function () {

            var url = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId;
            var selectedType = this.$selectConfSpec.val();

            if (selectedType === 'serial-number') {
                url += '/product-instances/' + App.config.productId + '/instances/' + this.$selectProdInstSpec.val().substr(3);
            } else if (selectedType === 'baseline') {
                url += '/product-baselines/' + App.config.productId + '/baselines/' + this.$selectBaselineSpec.val();
            } else {
                url += '/products/' + App.config.productId;
            }

            url += '/path-to-path-links-types';

            var $select = this.$selectPathToPathLink;
            $select.empty();
            $select.append('<option value="">' + App.config.i18n.STRUCTURE + '</option>');

            $.getJSON(url).success(function (data) {
                data.map(function (link) {
                    $select.append('<option value="' + link.type + '">' + link.type + '</option>');
                });
            });
        },

        changePathToPathLink: function (e) {
            App.config.linkType = e.target.value;
            this.trigger('config_spec:changed', App.config.productConfigSpec);
        },

        setDescription: function (desc) {
            this.$('.description').text(desc);
        },

        refresh: function () {
            var selectedConfigSpecOption = this.$('option[value="' + App.config.productConfigSpec + '"]');

            if (selectedConfigSpecOption) {
                this.$selectBaselineSpec.hide();
                this.$selectLatestFilter.hide();
                this.$selectProdInstSpec.hide();

                selectedConfigSpecOption.parent().val(App.config.productConfigSpec).show();

                if (_.contains(this.availableFilters, App.config.productConfigSpec)) {
                    this.$selectConfSpec.val('latest-filters');
                } else if (App.config.productConfigSpec.match(/^pi-/)) {
                    this.$selectConfSpec.val('serial-number');
                } else {
                    this.$selectConfSpec.val('baseline');
                }
            }
        },

        toggleSubstitutes: function () {
            if (this.showSubstitutes) {
                this.$toggleSubstitutes.html(App.config.i18n.HIDE_SUBSTITUTES);
            } else {
                this.$toggleSubstitutes.html(App.config.i18n.SHOW_SUBSTITUTES);
            }

            this.showSubstitutes = !this.showSubstitutes;

            App.config.diverge = !App.config.diverge;
            this.trigger('config_spec:changed', App.config.productConfigSpec);
        }

    });

    return BaselineSelectView;
});
