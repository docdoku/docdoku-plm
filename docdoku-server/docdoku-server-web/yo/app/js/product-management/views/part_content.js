/*global define*/
define([
    'backbone',
    "mustache",
    "common-objects/collections/part_collection",
    "common-objects/collections/part_search_collection",
    "text!templates/part_content.html",
    "views/part_list",
    "views/part_creation_view",
    "views/part_new_version",
    "common-objects/views/prompt",
    "common-objects/views/security/acl_edit",
    "views/advanced_search",
    "text!common-objects/templates/buttons/delete_button.html",
    "text!common-objects/templates/buttons/checkout_button_group.html",
    "text!common-objects/templates/buttons/new_version_button.html",
    "text!common-objects/templates/buttons/release_button.html",
    "text!common-objects/templates/buttons/ACL_button.html"
], function (Backbone, Mustache, PartCollection, PartSearchCollection, template, PartListView, PartCreationView, PartNewVersionView, PromptView, ACLEditView, AdvancedSearchView, deleteButton, checkoutButtonGroup, newVersionButton, release_button, aclButton) {
    var PartContentView = Backbone.View.extend({

        el: "#product-management-content",

        events: {
            "click button.new-part": "newPart",
            "click button.delete": "deletePart",
            "click button.checkout": "checkout",
            "click button.undocheckout": "undocheckout",
            "click button.checkin": "checkin",
            "click button.edit-acl": "updateACL",
            "click button.new-version": "newVersion",
            "click button.new-release": "releasePart",
            "click button.next-page": "toNextPage",
            "click button.previous-page": "toPreviousPage",
            "click button.first-page": "toFirstPage",
            "click button.last-page": "toLastPage",
            "click button.current-page": "goToPage",
            "submit #part-search-form": "onQuickSearch",
            "click .advanced-search-button": "onAdvancedSearch"
        },

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton,
            checkoutButtonGroup: checkoutButtonGroup,
            newVersionButton: newVersionButton,
            release_button: release_button
        },

        initialize: function () {
            _.bindAll(this);
            Backbone.Events.on("refresh_tree", this.resetCollection);
            this.query = null;
        },

        setQuery: function (query) {
            this.query = query;
            return this;
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));

            this.bindDomElements();

            var collection;

            if (this.query) {
                collection = new PartSearchCollection();
                collection.setQuery(this.query);
            } else {
                collection = new PartCollection();
            }

            this.partListView = new PartListView({
                el: this.$("#part_table"),
                collection: collection
            }).render();

            this.partListView.collection.on("page-count:fetch", this.onPageCountFetched);
            this.partListView.collection.fetchPageCount();

            this.partListView.on("delete-button:display", this.changeDeleteButtonDisplay);
            this.partListView.on("checkout-group:display", this.changeCheckoutGroupDisplay);
            this.partListView.on("checkout-group:update", this.updateCheckoutButtons);
            this.partListView.on("acl-edit-button:display", this.changeACLButtonDisplay);
            this.partListView.on("new-version-button:display", this.changeVersionButtonDisplay);
            this.partListView.on("release-button:display", this.changeReleaseButtonDisplay);

            return this;
        },

        bindDomElements: function () {
            this.deleteButton = this.$(".delete");
            this.checkoutGroup = this.$(".checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.aclButton = this.$(".edit-acl");
            this.checkinButton = this.$(".checkin");
            this.newVersionButton = this.$(".new-version");
            this.releaseButton = this.$(".new-release");
            this.currentPageIndicator = this.$(".current-page");
            this.pageControls = this.$(".page-controls");
        },

        newPart: function () {
            var partCreationView = new PartCreationView();
            this.listenTo(partCreationView, 'part:created', this.fetchPartAndAdd);
            window.document.body.appendChild(partCreationView.render().el);
            partCreationView.openModal();
        },

        fetchPartAndAdd: function (part) {
            var self = this;
            part.set("partKey", part.getNumber() + "-A");
            part.fetch().success(function () {
                self.addPartInList(part);
            });
            this.partListView.collection.fetchPageCount();
        },

        deletePart: function () {
            this.partListView.deleteSelectedParts();
        },

        addPartInList: function (part) {
            this.partListView.pushPart(part);
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

            if (!selectedPart.getLastIteration().get("iterationNote")) {
                var promptView = new PromptView();
                promptView.setPromptOptions(App.config.i18n.ITERATION_NOTE, App.config.i18n.ITERATION_NOTE_PROMPT_LABEL, App.config.i18n.ITERATION_NOTE_PROMPT_OK, App.config.i18n.ITERATION_NOTE_PROMPT_CANCEL);
                window.document.body.appendChild(promptView.render().el);
                promptView.openModal();

                self.listenTo(promptView, 'prompt-ok', function (args) {
                    var iterationNote = args[0];
                    if (_.isEqual(iterationNote, "")) {
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
            if (confirm(App.config.i18n.UNDO_CHECKOUT_QUESTION)) {
                this.partListView.getSelectedPart().undocheckout();
            }
        },

        updateACL: function () {

            var that = this;

            var selectedPart = that.partListView.getSelectedPart();

            var aclEditView = new ACLEditView({
                editMode: true,
                acl: selectedPart.get("acl")
            });

            aclEditView.setTitle(selectedPart.getPartKey());

            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();

            aclEditView.on("acl:update", function () {

                var acl = aclEditView.toList();

                selectedPart.updateACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        selectedPart.set("acl", acl);
                        aclEditView.closeModal();
                    },
                    error: function (error) {
                        alert(error.responseText);
                    }
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
            var requestedPage = prompt(App.config.i18n.GO_TO_PAGE, "1");
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
            this.currentPageIndicator.text(this.partListView.collection.getCurrentPage() + " / " + this.partListView.collection.getPageCount());
        },

        onQuickSearch: function (e) {
            if (e.target.children[0].value) {
                App.router.navigate("parts-search/number=" + e.target.children[0].value, {trigger: true});
            }
            e.preventDefault();
            return false;
        },

        onAdvancedSearch: function () {
            var advancedSearchView = new AdvancedSearchView();
            window.document.body.appendChild(advancedSearchView.render().el);
            advancedSearchView.openModal();
        }

    });

    return PartContentView;

});
