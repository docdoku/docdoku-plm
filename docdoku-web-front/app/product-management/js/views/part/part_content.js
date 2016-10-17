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
    'views/query_builder',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/checkout_button_group.html',
    'text!common-objects/templates/buttons/new_version_button.html',
    'text!common-objects/templates/buttons/release_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'text!common-objects/templates/buttons/new_product_button.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/obsolete_button.html',
    'text!templates/part/search_part_form.html',
    'text!common-objects/templates/task/status_filter.html',
    'common-objects/views/alert',
    'common-objects/views/tags/tags_management',
    'views/part/part_importer',
    'views/product/product_creation_view',
    'views/advanced_search',
    'views/part/part_grouped_by_list',
    'text!common-objects/templates/buttons/import_button.html',
], function (Backbone, Mustache, Async, PartCollection, PartSearchCollection, template, PartListView, PartCreationView, PartNewVersionView, PromptView, ACLEditView, QueryBuilder, deleteButton, checkoutButtonGroup, newVersionButton, releaseButton, aclButton, newProductButton, tagsButton, obsoleteButton, searchForm, statusFilter, AlertView, TagsManagementView, PartImporterView, ProductCreationView, AdvancedSearchView, PartGroupedByView, importButton) {
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
            'click .display-query-builder-button': 'toggleQueryBuilder',
            'click .import': 'showImporter',
            'click button[value="all"]': 'showAllWithTask',
            'click button[value="in_progress"]': 'showTaskInProgress'
        },

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton,
            checkoutButtonGroup: checkoutButtonGroup,
            newVersionButton: newVersionButton,
            releaseButton: releaseButton,
            searchForm: searchForm,
            newProductButton: newProductButton,
            tagsButton: tagsButton,
            obsoleteButton: obsoleteButton,
            importButton: importButton,
            statusFilter: statusFilter
        },

        id: 'part_content',

        initialize: function () {
            _.bindAll(this);
        },

        render: function () {

            this.isQueryBuilderDisplayed = false;
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, filter: this.options.filter}, this.partials));
            this.bindDomElements();

            this.tagsButton.show();
            this.tagsButton.prop('disabled', App.config.isReadOnly);

            if (!this.query && !this.partsCollection) {
                this.partsCollection = new PartCollection();
            } else if (this.query) {
                this.partsCollection = new PartSearchCollection();
                this.partsCollection.setQuery(this.query);
            }

            if (this.partListView) {
                this.partListView.remove();
            }

            if (this.queryBuilder) {
                this.queryBuilder.remove();
            }

            this.partListView = new PartListView({
                el: this.$('#part_table'),
                collection: this.partsCollection
            }).render();

            this.queryBuilder = new QueryBuilder({
                el: this.$queryBuilder
            });

            if (this.options.filter) {
                this.$('button.filter[value=' + this.options.filter + ']').addClass('active');
            }

            this.bindEvent();
            return this;
        },

        setCollection: function (collection) {
            this.partsCollection = collection;
            return this;
        },

        setQuery: function (query) {
            this.query = query;
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

            this.$queryBuilder = this.$('.query-builder');
            this.$displayQueryBuilderButton = this.$('.display-query-builder-button');
            this.$queryTableContainer = this.$('#query_table_container');
            this.$partTableContainer = this.$('#part_table_container');
        },

        bindEvent: function () {
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
            this.queryBuilder.on('query:search', this.onQueryBuilderSearch);
            this.delegateEvents();
        },

        onQueryBuilderSearch: function (data) {
            this.queryTable = new PartGroupedByView({
                data: data,
                el: this.$queryTableContainer
            }).render();
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

        changeObsoleteButtonDisplay: function (state) {
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
            var selectedPartsWithoutNote = 0;

            _.each(selectedParts, function (selectedPart) {
                if (!selectedPart.getLastIteration().get('iterationNote')) {
                    selectedPartsWithoutNote++;
                }
            });

            var _this = this;

            if (selectedPartsWithoutNote > 0) {
                var promptView = new PromptView();

                if (selectedParts.length > 1) {
                    promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.PART_REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
                } else {
                    promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
                }

                promptView.specifyInput('textarea');
                window.document.body.appendChild(promptView.render().el);
                promptView.openModal();

                this.listenTo(promptView, 'prompt-ok', function (args) {
                    var iterationNote = args[0];
                    if (_.isEqual(iterationNote, '')) {
                        iterationNote = null;
                    }
                    Async.each(selectedParts, function (part, callback) {
                        var revisionNote;
                        if (iterationNote) {
                            revisionNote = part.getLastIteration().get('iterationNote');
                            if (!revisionNote) {
                                revisionNote = iterationNote;
                            }
                        }

                        part.getLastIteration().save({
                            iterationNote: revisionNote
                        }).then(function () {
                            return part.checkin();
                        }).then(function () {
                            callback();
                        });

                    }, function (err) {
                        if (err) {
                            _this.onError(undefined, err);
                        } else {
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

            } else {
                Async.each(selectedParts, function (part, callback) {

                    part.getLastIteration().save().success(function () {
                        part.checkin().success(callback);
                    });

                }, function (err) {
                    if (err) {
                        _this.onError(undefined, err);
                    } else {
                        _this.allCheckinDone();
                    }
                });
            }
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
            var _this = this;
            var toBeDone = this.partListView.selectedPartIndexes.length;
            var done = 0;
            var onSuccess = function () {
                if (++done === toBeDone) {
                    _this.allCheckinDone();
                }
            };
            bootbox.confirm(App.config.i18n.UNDO_CHECKOUT_QUESTION,
                App.config.i18n.CANCEL,
                App.config.i18n.CONFIRM,
                function (result) {
                    if (result) {
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


        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        },
        onWarning: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.$notifications.append(new AlertView({
                type: 'warning',
                message: errorMessage
            }).render().$el);
        },

        newProduct: function () {
            var productCreationView = new ProductCreationView();
            window.document.body.appendChild(productCreationView.render().el);
            var that = this;
            productCreationView.on('product:created', function () {
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
        markAsObsolete: function () {
            var _this = this;
            bootbox.confirm(App.config.i18n.MARK_PART_AS_OBSOLETE_QUESTION,
                App.config.i18n.CANCEL,
                App.config.i18n.CONFIRM,
                function (result) {
                    if (result) {
                        _(_this.partListView.getSelectedParts()).each(function (part) {
                            part.markAsObsolete();
                        });
                    }
                });
        },

        toggleQueryBuilder: function () {
            this.isQueryBuilderDisplayed = !this.isQueryBuilderDisplayed;
            this.$el.toggleClass('displayQueryBuilder', this.isQueryBuilderDisplayed);
            this.$displayQueryBuilderButton.toggleClass('fa-angle-double-down', !this.isQueryBuilderDisplayed);
            this.$displayQueryBuilderButton.toggleClass('fa-angle-double-up', this.isQueryBuilderDisplayed);
            this.$queryTableContainer.toggle(this.isQueryBuilderDisplayed);
            this.$partTableContainer.toggle(!this.isQueryBuilderDisplayed);

            this.$('.actions *:not(.display-query-builder-button)').prop('disabled', this.isQueryBuilderDisplayed);

            if (this.isQueryBuilderDisplayed) {
                this.queryBuilder.render();
            } else {
                this.queryBuilder.destroy();
            }

        },

        showImporter: function () {
            var partImporterView = new PartImporterView();
            partImporterView.render();
            document.body.appendChild(partImporterView.el);
            partImporterView.openModal();
            return false;
        },

        destroy: function () {
            if (this.partListView) {
                this.partListView.remove();
            }
            this.remove();
        },

        showAllWithTask: function () {
            window.location.hash = '#' + App.config.workspaceId + '/tasks';
        },

        showTaskInProgress: function () {
            window.location.hash = '#' + App.config.workspaceId + '/tasks/in_progress';
        }

    });

    return PartContentView;
});
