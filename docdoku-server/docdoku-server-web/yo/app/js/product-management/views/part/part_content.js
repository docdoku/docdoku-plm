/*global _,define,App,bootbox,window*/
define([
    'backbone',
    'mustache',
    'common-objects/collections/part_collection',
    'common-objects/collections/part_search_collection',
    'text!templates/part/part_content.html',
    'views/part/part_list',
    'views/part/part_creation_view',
    'views/part/part_new_version',
    'common-objects/views/prompt',
    'common-objects/views/security/acl_edit',
    'views/advanced_search',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
	'text!templates/part/search_part_form.html',
    'common-objects/views/alert'
], function (Backbone, Mustache, PartCollection, PartSearchCollection, template, PartListView, PartCreationView, PartNewVersionView, PromptView, ACLEditView, AdvancedSearchView, deleteButton, checkoutButtonGroup, newVersionButton, releaseButton, aclButton, searchForm, AlertView) {
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
            'click button.next-page': 'toNextPage',
            'click button.previous-page': 'toPreviousPage',
            'click button.first-page': 'toFirstPage',
            'click button.last-page': 'toLastPage',
            'click button.current-page': 'goToPage',
            'submit #part-search-form': 'onQuickSearch',
            'click .advanced-search-button': 'onAdvancedSearch'
        },

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton,
            checkoutButtonGroup: checkoutButtonGroup,
            newVersionButton: newVersionButton,
            releaseButton: releaseButton,
            searchForm: searchForm
        },

        initialize: function () {
            _.bindAll(this);
            this.query = null;
        },
        setQuery: function (query) {
            this.query = query;
            this.partsCollection = null;
            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));
            this.bindDomElements();

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
            this.releaseButton = this.$('.new-release');
            this.currentPageIndicator = this.$('.current-page');
            this.pageControls = this.$('.page-controls');
        },

        bindEvent: function(){
            // Try to remove this
            Backbone.Events.on('refresh_tree', this.resetCollection);

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

            this.delegateEvents();

        },

        newPart: function () {
            var partCreationView = new PartCreationView();
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
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },

        changeACLButtonDisplay: function (state) {
            if (state) {
                this.aclButton.show();
            } else {
                this.aclButton.hide();
            }
        },

        changeCheckoutGroupDisplay: function (state) {
            if (state) {
                this.checkoutGroup.show();
            } else {
                this.checkoutGroup.hide();
            }
        },

        changeVersionButtonDisplay: function (state) {
            if (state) {
                this.newVersionButton.show();
            } else {
                this.newVersionButton.hide();
            }
        },

        changeReleaseButtonDisplay: function (state) {
            if (state) {
                this.releaseButton.show();
            } else {
                this.releaseButton.hide();
            }
        },

        updateCheckoutButtons: function (values) {
            this.checkoutButton.prop('disabled', !values.canCheckout);
            this.undoCheckoutButton.prop('disabled', !values.canUndo);
            this.checkinButton.prop('disabled', !values.canCheckin);
        },

        checkin: function () {
            var self = this;
            var selectedPart = this.partListView.getSelectedPart();

            if (!selectedPart.getLastIteration().get('iterationNote')) {
                var promptView = new PromptView();
                promptView.setPromptOptions(App.config.i18n.ITERATION_NOTE, App.config.i18n.ITERATION_NOTE_PROMPT_LABEL, App.config.i18n.ITERATION_NOTE_PROMPT_OK, App.config.i18n.ITERATION_NOTE_PROMPT_CANCEL);
                window.document.body.appendChild(promptView.render().el);
                promptView.openModal();

                self.listenTo(promptView, 'prompt-ok', function (args) {
                    var iterationNote = args[0];
                    if (_.isEqual(iterationNote, '')) {
                        iterationNote = null;
                    }
                    selectedPart.getLastIteration().save({
                        iterationNote: iterationNote
                    }).success(function () {
                        selectedPart.checkin();
                    });
                });

                self.listenTo(promptView, 'prompt-cancel', function () {
                    selectedPart.checkin();
                });
            } else {
                selectedPart.checkin();
            }
        },
        checkout: function () {
            this.partListView.getSelectedPart().checkout();
        },
        undocheckout: function () {
            var _this= this;
            bootbox.confirm(App.config.i18n.UNDO_CHECKOUT_QUESTION, function(result){
                if(result){
                    _this.partListView.getSelectedPart().undocheckout();
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
            }).render();
            window.document.body.appendChild(partNewVersionView.el);
        },

        releasePart: function () {
            this.partListView.releaseSelectedParts();
        },

        resetCollection: function () {
            this.partListView.collection.fetch({reset: true});
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

        onQuickSearch: function (e) {
            if (e.target.children[0].value) {
                App.router.navigate(App.config.workspaceId + '/parts-search/q=' + e.target.children[0].value, {trigger: true});
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
        }

    });

    return PartContentView;
});
