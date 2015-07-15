/*global _,$,define,App,bootbox,window*/
define([
    'backbone',
    'mustache',
    'async',
    'common-objects/collections/part_collection',
    'common-objects/collections/part_search_collection',
    'text!templates/part/part_content.html',
    'views/part/part_list',
    'views/part/part_creation_view',
    'views/part/part_new_version',
    'common-objects/views/prompt',
    'common-objects/views/security/acl_edit',
    '../query_builder',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!common-objects/templates/buttons/new_product_button.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/obsolete_button.html',
	'text!templates/part/search_part_form.html',
    'common-objects/views/alert',
    'common-objects/views/tags/tags_management',
    'views/product/product_creation_view',
    'views/advanced_search',
    'views/part/part_grouped_by_list'
], function (Backbone, Mustache, Async, PartCollection, PartSearchCollection, template, PartListView, PartCreationView, PartNewVersionView, PromptView, ACLEditView, QueryBuilder, deleteButton, checkoutButtonGroup, newVersionButton, releaseButton, aclButton, newProductButton, tagsButton, obsoleteButton, searchForm, AlertView,TagsManagementView,ProductCreationView,AdvancedSearchView, PartGroupedByView) {
    'use strict';
	var PartContentView = Backbone.View.extend({
        events: {
            'click button.new-part': 'newPart',
            'click button.delete': 'deletePart',
            'click button.checkout': 'checkout',
            'click button.undocheckout': 'undocheckout',
            'click button.checkin': 'checkin',
            'click button.edit-acl': 'updateACL',
            'click button.new-version': 'newVersion',
            'click button.new-release': 'releasePart',
            'click button.mark-as-obsolete': 'markAsObsolete',
            'click button.next-page': 'toNextPage',
            'click button.previous-page': 'toPreviousPage',
            'click .actions .tags': 'actionTags',
            'click button.first-page': 'toFirstPage',
            'click button.last-page': 'toLastPage',
            'click button.current-page': 'goToPage',
            'click button.show-all': 'showAll',
            'click button.new-product': 'newProduct',
            'submit #part-search-form': 'onQuickSearch',
            'click .advanced-search-button': 'onAdvancedSearch',
            'click .display-query-builder-button': 'toggleQueryBuilder'
        },

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton,
            checkoutButtonGroup: checkoutButtonGroup,
            newVersionButton: newVersionButton,
            releaseButton: releaseButton,
            searchForm: searchForm,
            newProductButton:newProductButton,
            tagsButton: tagsButton,
            obsoleteButton:obsoleteButton
        },

        initialize: function () {
            _.bindAll(this);
            this.query = null;
        },

        setCollection:function(collection){
            this.partsCollection = collection;
            return this;
        },

        setQuery: function (query) {
            this.query = query;
            this.partsCollection = null;
            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();
            //always show tag button
            this.tagsButton.show();

            if(!this.partsCollection){
                if (this.query) {
                    this.partsCollection = new PartSearchCollection();
                    this.partsCollection.setQuery(this.query);
                } else {
                    this.partsCollection = new PartCollection();
                }
            }

            if(this.partListView){
                this.partListView.remove();
            }

            this.partListView = new PartListView({
                el: this.$('#part_table'),
                collection: this.partsCollection
            }).render();


            this.queryBuilder = new QueryBuilder({
                el: this.$('.query-builder')
            });

            this.bindEvent();
            return this;
        },

        bindDomElements: function () {
            this.$notifications = this.$el.find('.notifications').first();
            this.deleteButton = this.$('.delete');
            this.checkoutGroup = this.$('.checkout-group');
            this.checkoutButton = this.$('.checkout');
            this.undoCheckoutButton = this.$('.undocheckout');
            this.aclButton = this.$('.edit-acl');
            this.checkinButton = this.$('.checkin');
            this.newVersionButton = this.$('.new-version');
            this.newProductButton = this.$('.new-product');
            this.releaseButton = this.$('.new-release');
            this.obsoleteButton = this.$('.mark-as-obsolete');
            this.tagsButton = this.$('.actions .tags');
            this.currentPageIndicator = this.$('.current-page');
            this.pageControls = this.$('.page-controls');
            this.pagingButtons = this.$('.paging-buttons');
            this.showAllButton = this.$('.show-all');
        },

        bindEvent: function(){
            this.partListView.collection.on('page-count:fetch', this.onPageCountFetched);
            this.partListView.collection.fetchPageCount();

            this.partListView.on('error', this.onError);
            this.partListView.on('warning', this.onWarning);
            this.partListView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.partListView.on('checkout-group:display', this.changeCheckoutGroupDisplay);
            this.partListView.on('checkout-group:update', this.updateCheckoutButtons);
            this.partListView.on('acl-edit-button:display', this.changeACLButtonDisplay);
            this.partListView.on('new-version-button:display', this.changeVersionButtonDisplay);
            this.partListView.on('release-button:display', this.changeReleaseButtonDisplay);
            this.partListView.on('obsolete-button:display', this.changeObsoleteButtonDisplay);
            this.partListView.on('new-product-button:display', this.changeNewProductButtonDisplay);

            this.delegateEvents();

            var self = this;
            this.queryBuilder.on('query:search', function(data){
                if(self.partListView){
                    self.partListView.remove();
                    self.pageControls.remove();
                    self.$('#part_table_filter').remove();
                }
                self.queryTable = new PartGroupedByView({
                    data : data,
                    el: self.$('#query-table')
                }).render();
            });

        },

        newPart: function () {
            var partCreationView = new PartCreationView({
                autoAddTag: this.partListView.collection.tag
            });
            this.listenTo(partCreationView, 'part:created', this.fetchPartAndAdd);
            window.document.body.appendChild(partCreationView.el);
            partCreationView.openModal();
        },

        fetchPartAndAdd: function (part) {
            var self = this;
            part.set('partKey', part.getNumber() + '-A');
            part.fetch().success(function () {
                self.addPartInList(part);
            });
            this.partListView.collection.fetchPageCount();
        },

        deletePart: function () {
            this.partListView.deleteSelectedParts();
        },

        addPartInList: function (part) {
            this.partsCollection.push(part);
        },

        changeDeleteButtonDisplay: function (state) {
            this.deleteButton.toggle(state);
        },

        changeACLButtonDisplay: function (state) {
            this.aclButton.toggle(state);
        },

        changeCheckoutGroupDisplay: function (state) {
            this.checkoutGroup.toggle(state);
        },

        changeVersionButtonDisplay: function (state) {
            this.newVersionButton.toggle(state);
        },

        changeNewProductButtonDisplay: function (state) {
            this.newProductButton.toggle(state);
        },

        changeReleaseButtonDisplay: function (state) {
            this.releaseButton.toggle(state);
        },

        changeObsoleteButtonDisplay: function(state) {
            this.obsoleteButton.toggle(state);
        },

        updateCheckoutButtons: function (values) {
            this.checkoutButton.prop('disabled', !values.canCheckout);
            this.undoCheckoutButton.prop('disabled', !values.canUndo);
            this.checkinButton.prop('disabled', !values.canCheckin);
        },

        checkin: function () {
            this.partListView.getSelectedPartIndexes();

            var selectedParts = this.partListView.getSelectedParts();
            var promptView = new PromptView();
            promptView.setPromptOptions(App.config.i18n.ITERATION_NOTE, App.config.i18n.ITERATION_NOTE_PROMPT_LABEL, App.config.i18n.ITERATION_NOTE_PROMPT_OK, App.config.i18n.ITERATION_NOTE_PROMPT_CANCEL);
            promptView.specifyInput('textarea');
            window.document.body.appendChild(promptView.render().el);
            promptView.openModal();

            this.listenTo(promptView, 'prompt-ok', function (args) {
                var iterationNote = args[0];
                if (_.isEqual(iterationNote, '')) {
                    iterationNote = null;
                }

                var _this = this;
                Async.each(selectedParts, function(part, callback) {
                    part.getLastIteration().save({
                        iterationNote: iterationNote
                    }).success(function () {
                        part.checkin().success(callback);
                    });

                }, function(err) {
                    if (!err) {
                        _this.allCheckinDone();
                    }
                });

            });

            this.listenTo(promptView, 'prompt-cancel', function () {
                var ajaxes = [];
                _(selectedParts).each(function (part) {
                    ajaxes.push(part.checkin());
                });
                $.when.apply($, ajaxes).then(this.allCheckinDone);
            });

        },
        allCheckinDone: function () {
            this.resetCollection();
            Backbone.Events.trigger('part:iterationChange');
        },

        checkout: function () {
            _(this.partListView.getSelectedParts()).each(function (view) {
                view.checkout();
            });
        },
        undocheckout: function () {
            this.partListView.getSelectedPartIndexes();
            var _this= this;
            var toBeDone = this.partListView.selectedPartIndexes.length;
            var done = 0;
            var onSuccess = function() {
                if(++done === toBeDone) {
                    _this.allCheckinDone();
                }
            };
            bootbox.confirm(App.config.i18n.UNDO_CHECKOUT_QUESTION, function(result){
                if(result){
                    _(_this.partListView.getSelectedParts()).each(function (view) {
                        view.undocheckout().success(onSuccess);
                    });
                }

            });
        },

        updateACL: function () {
            var _this = this;

            var selectedPart = _this.partListView.getSelectedPart();

            var aclEditView = new ACLEditView({
                editMode: true,
                acl: selectedPart.get('acl')
            });

            aclEditView.setTitle(selectedPart.getPartKey());

            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();

            aclEditView.on('acl:update', function () {

                var acl = aclEditView.toList();

                selectedPart.updateACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        selectedPart.set('acl', acl);
                        aclEditView.closeModal();
                    },
                    error: _this.onError
                });

            });

        },

        newVersion: function () {
            var partNewVersionView = new PartNewVersionView({
                model: this.partListView.getSelectedPart()
            });
            window.document.body.appendChild(partNewVersionView.render().el);
            partNewVersionView.openModal();
        },

        releasePart: function () {
            this.partListView.releaseSelectedParts();
        },

        resetCollection: function () {
            this.partListView.collection.fetch({reset: true}).success(function () {
                this.partListView.checkCheckboxes();
                this.partListView.canCheckinCheckoutOrUndoCheckout();
            }.bind(this));
        },

        onPageCountFetched: function () {
            this.updatePageIndicator();
            if (this.partListView.collection.hasSeveralPages()) {
                this.pageControls.show();
            } else {
                this.pageControls.hide();
            }
        },

        goToPage: function () {
            var requestedPage = window.prompt(App.config.i18n.GO_TO_PAGE, '1');
            if (requestedPage - 1 >= 0 && requestedPage <= this.partListView.collection.getPageCount()) {
                this.partListView.collection.setCurrentPage(requestedPage - 1).fetch({reset: true});
                this.updatePageIndicator();
                this.partListView.onNoPartSelected();
            }
        },

        toFirstPage: function () {
            this.partListView.collection.setFirstPage().fetch({reset: true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toLastPage: function () {
            this.partListView.collection.setLastPage().fetch({reset: true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toNextPage: function () {
            this.partListView.collection.setNextPage().fetch({reset: true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        toPreviousPage: function () {
            this.partListView.collection.setPreviousPage().fetch({reset: true});
            this.updatePageIndicator();
            this.partListView.onNoPartSelected();
        },

        updatePageIndicator: function () {
            this.currentPageIndicator.text(this.partListView.collection.getCurrentPage() + ' / ' + this.partListView.collection.getPageCount());
        },

        showAll: function () {
            if (this.partListView.collection.resultsPerPage) {
                this.partListView.collection.setResultsPerPage(0);
                this.pagingButtons.hide();
                this.showAllButton.html(App.config.i18n.SHOW_BY_PAGE);
            } else {
                this.partListView.collection.setResultsPerPage(20);
                this.pagingButtons.show();
                this.showAllButton.html(App.config.i18n.SHOW_ALL);
            }
            this.partListView.collection.fetch({reset: true});
            this.partListView.onNoPartSelected();
        },

        onQuickSearch: function (e) {
            if (e.target.children[1].value) {
                App.router.navigate(App.config.workspaceId + '/parts-search/?q=' + e.target.children[1].value, {trigger: true});
            }
            e.preventDefault();
            return false;
        },
        onAdvancedSearch: function () {
            var advancedSearchView = new AdvancedSearchView();
            window.document.body.appendChild(advancedSearchView.render().el);
            advancedSearchView.openModal();
        },


        onError:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },
        onWarning:function(model, error){
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'warning',
                message: errorMessage
            }).render().$el);
        },

        newProduct:function(){
            var productCreationView = new ProductCreationView();
            window.document.body.appendChild(productCreationView.render().el);
            var that = this ;
            productCreationView.on('product:created',function(){
                that.$notifications.append(new AlertView({
                    type: 'info',
                    message: App.config.i18n.PRODUCT_CREATED
                }).render().$el);
            });
            productCreationView.setRootPart(this.partListView.getSelectedPart())
                .openModal();
        },
        actionTags: function () {

            var partsChecked = new Backbone.Collection();

            this.partListView.eachChecked(function (view) {
                partsChecked.push(view.model);
            });

            var tagsManagementView = new TagsManagementView({
                collection: partsChecked
            });
            window.document.body.appendChild(tagsManagementView.el);
            tagsManagementView.show();

            return false;

        },
        markAsObsolete:function(){
            var _this = this;
            bootbox.confirm(App.config.i18n.MARK_AS_OBSOLETE_QUESTION, function(result){
                if(result){
                    _(_this.partListView.getSelectedParts()).each(function (part) {
                        part.markAsObsolete();
                    });
                }
            });
        },

        toggleQueryBuilder:function(){
            this.$el.toggleClass('displayQueryBuilder');
            this.$('.display-query-builder-button').toggleClass('fa-angle-double-down');
            this.$('.display-query-builder-button').toggleClass('fa-angle-double-up');
            if(this.$el.hasClass('displayQueryBuilder')){
                this.queryBuilder.render();
            }else{
                this.queryBuilder.destroy();
            }
        },

        destroy:function(){
            if(this.partListView){
                this.partListView.remove();
            }
            this.remove();
        }

    });

    return PartContentView;
});
