define([
    "i18n!localization/nls/document-management-strings",
    "views/content",
    "views/document_list",
    "views/document/documents_tags",
    "views/advanced_search"
], function(i18n, ContentView, DocumentListView, DocumentsTagsView, AdvancedSearchView) {
    var ContentDocumentListView = ContentView.extend({

        initialize: function() {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events["click .actions .checkout"] = "actionCheckout";
            this.events["click .actions .undocheckout"] = "actionUndocheckout";
            this.events["click .actions .checkin"] = "actionCheckin";
            this.events["click .actions .delete"] = "actionDelete";
            this.events["click .actions .tags"] = "actionTags";
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

            this.listView = this.addSubView(
                new DocumentListView({
                    el: "#list-" + this.cid,
                    collection: this.collection
                })
            );

            this.collection.fetch();

            this.listenTo(this.listView, "selectionChange", this.onStateChange);
            this.listenTo(this.collection, "change", this.onStateChange);

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
        },

        onOneDocumentSelected: function(document) {
            this.deleteButton.show();
            this.checkoutGroup.css('display', 'inline-block');
            this.tagsButton.show();

            if (document.isCheckout()) {
                if (document.isCheckoutByConnectedUser()) {
                    this.updateActionsButton(false, true);
                } else {
                    this.updateActionsButton(false, false);
                }
            } else {
                this.updateActionsButton(true, false);
            }

        },

        onSeveralDocumentsSelected: function() {
            this.deleteButton.show();
            this.tagsButton.show();
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
            this.listView.eachChecked(function(view) {
                view.model.checkin();
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

            var documentsChecked = new Backbone.Collection;


            this.listView.eachChecked(function(view) {
                documentsChecked.push(view.model);
            });

            var view = this.addSubView(
                new DocumentsTagsView({
                    collection: documentsChecked
                })
            ).show();

            return false;

        },

        onQuickSearch:function(e){

            if(e.target.children[0].value){
                this.router.navigate("search/id="+e.target.children[0].value, {trigger: true});
            }

            return false;
        },

        onAdvancedSearchButton:function(){
            var advancedSearchView = new AdvancedSearchView();
            $("body").append(advancedSearchView.render().el);
            advancedSearchView.openModal();
            advancedSearchView.setRouter(this.router);
        }


    });

    return ContentDocumentListView;

});
