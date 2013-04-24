define([
    "i18n!localization/nls/document-management-strings",
    "views/content",
    "views/document_list",
    "views/document/documents_tags",
    "views/document/document_new_version",
    "views/advanced_search",
    "common-objects/views/prompt"
], function(i18n, ContentView, DocumentListView, DocumentsTagsView, DocumentNewVersionView, AdvancedSearchView, PromptView) {
    var ContentDocumentListView = ContentView.extend({

        initialize: function() {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events["click .actions .checkout"] = "actionCheckout";
            this.events["click .actions .undocheckout"] = "actionUndocheckout";
            this.events["click .actions .checkin"] = "actionCheckin";
            this.events["click .actions .delete"] = "actionDelete";
            this.events["click .actions .tags"] = "actionTags";
            this.events["click .actions .new-version"] = "actionNewVersion";
            this.events["submit .actions #document-search-form"] = "onQuickSearch";
            this.events["click .actions .advanced-search-button"] = "onAdvancedSearchButton";
        },

        rendered: function() {

            this.checkoutGroup = this.$(".actions .checkout-group");
            this.checkoutButton = this.$(".checkout");
            this.undoCheckoutButton = this.$(".undocheckout");
            this.checkinButton = this.$(".checkin");
            this.deleteButton = this.$(".actions .delete");
            this.tagsButton = this.$(".actions .tags");
            this.newVersionButton = this.$(".actions .new-version");

            this.listView = this.addSubView(
                new DocumentListView({
                    el: "#list-" + this.cid,
                    collection: this.collection
                })
            );

            this.collection.fetch();

            this.listenTo(this.listView, "selectionChange", this.onStateChange);
            this.listenTo(this.collection, "change", this.onStateChange);
            this.listenTo(this.collection, "add", this.highlightAddedView);

            this.$(".tabs").tabs();
        },

        onStateChange: function() {

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

        onNoDocumentSelected: function() {
            this.deleteButton.hide();
            this.checkoutGroup.hide();
            this.tagsButton.hide();
            this.newVersionButton.hide();
        },

        onOneDocumentSelected: function(document) {
            this.deleteButton.show();
            this.checkoutGroup.css('display', 'inline-block');
            this.tagsButton.show();
            this.newVersionButton.show();

            if (document.isCheckout()) {
                this.newVersionButton.prop('disabled', true);
                if (document.isCheckoutByConnectedUser()) {
                    this.updateActionsButton(false, true);
                } else {
                    this.updateActionsButton(false, false);
                }
            } else {
                this.newVersionButton.prop('disabled', false);
                this.updateActionsButton(true, false);
            }

        },

        onSeveralDocumentsSelected: function() {
            this.deleteButton.show();
            this.tagsButton.show();
            this.newVersionButton.hide();
            this.checkoutGroup.hide();
        },

        updateActionsButton: function(canCheckout, canUndoAndCheckin) {
            this.checkoutButton.prop('disabled', !canCheckout);
            this.undoCheckoutButton.prop('disabled', !canUndoAndCheckin);
            this.checkinButton.prop('disabled', !canUndoAndCheckin);
        },

        actionCheckout: function() {
            this.listView.eachChecked(function(view) {
                view.model.checkout();
            });
            return false;
        },

        actionUndocheckout: function() {
            this.listView.eachChecked(function(view) {
                view.model.undocheckout();
            });
            return false;
        },

        actionCheckin: function() {
            var self = this;
            this.listView.eachChecked(function(view) {
                if (_.isNull(view.model.getLastIteration().attributes.revisionNote)) {
                    var promptView = new PromptView();
                    promptView.setPromptOptions(i18n.REVISION_NOTE, i18n.REVISION_NOTE_PROMPT_LABEL, i18n.REVISION_NOTE_PROMPT_OK, i18n.REVISION_NOTE_PROMPT_CANCEL);
                    $("body").append(promptView.render().el);
                    promptView.openModal();
                    self.listenTo(promptView, 'prompt-ok', function(args) {
                        var revisionNote = args[0];
                        if(_.isEqual(revisionNote, "")) {
                            revisionNote = null;
                        }
                        view.model.getLastIteration().save({
                            revisionNote: revisionNote
                        });
                        view.model.checkin();
                    });
                    self.listenTo(promptView, 'prompt-cancel', function() {
                        view.model.checkin();
                    });
                } else {
                    view.model.checkin();
                }

            });
            return false;
        },

        actionDelete: function() {
            if (confirm(i18n["DELETE_SELECTION_?"])) {
                this.listView.eachChecked(function(view) {
                    view.model.destroy();
                });
            }
            return false;
        },

        actionTags: function() {

            var documentsChecked = new Backbone.Collection();


            this.listView.eachChecked(function(view) {
                documentsChecked.push(view.model);
            });

            this.addSubView(
                new DocumentsTagsView({
                    collection: documentsChecked
                })
            ).show();

            return false;

        },

        actionNewVersion: function() {

            var documentChecked;

            this.listView.eachChecked(function(view) {
                documentChecked = view.model;
            });

            var newVersionView = new DocumentNewVersionView({
                model: documentChecked
            }).render();

            $("body").append(
                newVersionView.$el
            );

            return false;

        },

        onQuickSearch: function(e) {

            if (e.target.children[0].value) {
                this.router.navigate("search/id=" + e.target.children[0].value, {trigger: true});
            }

            return false;
        },

        onAdvancedSearchButton: function() {
            var advancedSearchView = new AdvancedSearchView();
            $("body").append(advancedSearchView.render().el);
            advancedSearchView.openModal();
            advancedSearchView.setRouter(this.router);
        },

        highlightAddedView:function(model){
            var addedView = _.filter(this.listView.subViews, function(view){ return view.model == model})[0];
            if(addedView){
                addedView.$el.effect("highlight", {color: "#8fbc8f"}, 1000);
            }
        }


    });

    return ContentDocumentListView;

});
