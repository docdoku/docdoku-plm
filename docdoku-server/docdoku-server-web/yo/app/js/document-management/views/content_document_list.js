/*global define*/
define([
    "backbone",
    "views/content",
    "views/document_list",
    "views/document/document_new_version",
    "views/advanced_search",
    "common-objects/views/prompt",
    "common-objects/views/security/acl_edit",
    "common-objects/views/tags/tags_management"
], function (Backbone, ContentView, DocumentListView, DocumentNewVersionView, AdvancedSearchView, PromptView, ACLEditView, TagsManagementView) {
    var ContentDocumentListView = ContentView.extend({

        initialize: function () {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events["click .actions .checkout"] = "actionCheckout";
            this.events["click .actions .undocheckout"] = "actionUndocheckout";
            this.events["click .actions .checkin"] = "actionCheckin";
            this.events["click .actions .delete"] = "actionDelete";
            this.events["click .actions .tags"] = "actionTags";
            this.events["click .actions .new-version"] = "actionNewVersion";
            this.events["submit .actions #document-search-form"] = "onQuickSearch";
            this.events["click .actions .advanced-search-button"] = "onAdvancedSearchButton";
            this.events["click .actions .edit-acl"] = "onEditAcl";
        },

        rendered: function () {

            this.checkoutGroup = this.$(".actions .checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.checkinButton = this.$(".checkin");
            this.deleteButton = this.$(".actions .delete");
            this.tagsButton = this.$(".actions .tags");
            this.newVersionButton = this.$(".actions .new-version");
            this.aclButton = this.$(".actions .edit-acl");

            this.tagsButton.show();

            this.listView = this.addSubView(
                new DocumentListView({
                    el: "#list-" + this.cid,
                    collection: this.collection
                })
            );

            this.collection.fetch({reset: true});

            this.listenTo(this.listView, "selectionChange", this.onStateChange);
            this.listenTo(this.collection, "change", this.onStateChange);
            this.listenTo(this.collection, "add", this.highlightAddedView);

            this.$(".tabs").tabs();
        },

        onStateChange: function () {

            var checkedViews = this.listView.checkedViews();

            switch (checkedViews.length) {
                case 0:
                    this.onNoDocumentSelected();
                    break;
                case 1:
                    this.onOneDocumentSelected(checkedViews[0].model);
                    break;
                default:
                    this.onSeveralDocumentsSelected();
                    break;
            }

        },

        onNoDocumentSelected: function () {
            this.deleteButton.hide();
            this.checkoutGroup.hide();
            //this.tagsButton.show();
            this.newVersionButton.hide();
            this.aclButton.hide();
        },

        onOneDocumentSelected: function (document) {
            this.deleteButton.show();
            this.checkoutGroup.css('display', 'inline-block');
            this.newVersionButton.show();

            if (document.isCheckout()) {
                this.newVersionButton.prop('disabled', true);
                if (document.isCheckoutByConnectedUser()) {
                    var canUndo = document.getLastIteration().get("iteration") > 1;
                    this.updateActionsButton(false, canUndo, true);
                } else {
                    this.updateActionsButton(false, false, false);
                }
            } else {
                this.newVersionButton.prop('disabled', false);
                this.updateActionsButton(true, false, false);
            }


            if ((APP_CONFIG.workspaceAdmin || document.attributes.author.login == APP_CONFIG.login)) {
                this.aclButton.show();
            }

        },

        onSeveralDocumentsSelected: function () {
            this.deleteButton.show();
            this.newVersionButton.hide();
            this.checkoutGroup.hide();
            this.aclButton.hide();
        },

        updateActionsButton: function (canCheckout, canUndo, canCheckin) {
            this.checkoutButton.prop('disabled', !canCheckout);
            this.undoCheckoutButton.prop('disabled', !canUndo);
            this.checkinButton.prop('disabled', !canCheckin);
        },

        actionCheckout: function () {
            this.listView.eachChecked(function (view) {
                view.model.checkout();
            });
            return false;
        },

        actionUndocheckout: function () {
            if (confirm(APP_CONFIG.i18n.UNDO_CHECKOUT_QUESTION)) {
                this.listView.eachChecked(function (view) {
                    view.model.undocheckout();
                });
            }
            return false;
        },

        actionCheckin: function () {
            var self = this;
            this.listView.eachChecked(function (view) {
                if (!view.model.getLastIteration().get("revisionNote")) {
                    var promptView = new PromptView();
                    promptView.setPromptOptions(APP_CONFIG.i18n.REVISION_NOTE, APP_CONFIG.i18n.REVISION_NOTE_PROMPT_LABEL, APP_CONFIG.i18n.REVISION_NOTE_PROMPT_OK, APP_CONFIG.i18n.REVISION_NOTE_PROMPT_CANCEL);
                    window.document.body.appendChild(promptView.render().el);
                    promptView.openModal();

                    self.listenTo(promptView, 'prompt-ok', function (args) {
                        var revisionNote = args[0];
                        if (_.isEqual(revisionNote, "")) {
                            revisionNote = null;
                        }
                        view.model.getLastIteration().save({
                            revisionNote: revisionNote
                        }).success(function () {
                            view.model.checkin();
                        });

                    });

                    self.listenTo(promptView, 'prompt-cancel', function () {
                        view.model.checkin();
                    });

                } else {
                    view.model.checkin();
                }

            });
            return false;
        },

        actionDelete: function () {
            var that = this;
            if (confirm(APP_CONFIG.i18n["DELETE_SELECTION_QUESTION"])) {
                this.listView.eachChecked(function (view) {
                    view.model.destroy({success: function () {
                        that.listView.redraw();
                    }});
                });
            }
            return false;
        },

        actionTags: function () {
            var self = this;
            var documentsChecked = new Backbone.Collection();


            this.listView.eachChecked(function (view) {
                documentsChecked.push(view.model);
            });


            self.addSubView(
                new TagsManagementView({
                    collection: documentsChecked
                })
            ).show();


            return false;

        },

        actionNewVersion: function () {

            var documentChecked = null;

            this.listView.eachChecked(function (view) {
                documentChecked = view.model;
            });

            var newVersionView = new DocumentNewVersionView({
                model: documentChecked
            }).render();

            window.document.body.appendChild(newVersionView.el);

            return false;

        },

        onQuickSearch: function (e) {

            if (e.target.children[0].value) {
                App.router.navigate(APP_CONFIG.workspaceId + "/search/id=" + e.target.children[0].value, {trigger: true});
            }

            return false;
        },

        onAdvancedSearchButton: function () {
            var advancedSearchView = new AdvancedSearchView();
            window.document.body.appendChild(advancedSearchView.render().el);
            advancedSearchView.openModal();
        },

        onEditAcl: function () {

            var that = this;
            var documentChecked;

            this.listView.eachChecked(function (view) {
                documentChecked = view.model;
            });

            if (documentChecked) {

                var aclEditView = new ACLEditView({
                    editMode: true,
                    acl: documentChecked.get("acl")
                });

                aclEditView.setTitle(documentChecked.getReference());
                window.document.body.appendChild(aclEditView.render().el);

                aclEditView.openModal();
                aclEditView.on("acl:update", function () {

                    var acl = aclEditView.toList();

                    documentChecked.updateACL({
                        acl: acl || {userEntries: {}, groupEntries: {}},
                        success: function () {
                            documentChecked.set("acl", acl);
                            aclEditView.closeModal();
                            that.listView.redraw();
                        },
                        error: function () {
                            alert("Error on update acl");
                        }
                    });

                });

            }

            return false;

        },

        highlightAddedView: function (model) {
            this.listView.redraw();
            var addedView = _.find(this.listView.subViews, function (view) {
                return view.model == model;
            });
            if (addedView) {
                addedView.$el.highlightEffect();
            }
        }

    });

    return ContentDocumentListView;

});
