/*global _,define,bootbox,App,window*/
define([
    'backbone',
    'views/content',
    'views/document_list',
    'views/document/document_new_version',
    'views/advanced_search',
    'common-objects/views/prompt',
    'common-objects/views/security/acl_edit',
    'common-objects/views/tags/tags_management',
    'common-objects/views/alert',
    'async'
], function (Backbone, ContentView, DocumentListView, DocumentNewVersionView, AdvancedSearchView, PromptView, ACLEditView, TagsManagementView, AlertView, async) {
    'use strict';
    var ContentDocumentListView = ContentView.extend({

        initialize: function () {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events['click .actions .checkout'] = 'actionCheckout';
            this.events['click .actions .undocheckout'] = 'actionUndocheckout';
            this.events['click .actions .checkin'] = 'actionCheckin';
            this.events['click .actions .delete'] = 'actionDelete';
            this.events['click .actions .tags'] = 'actionTags';
            this.events['click .actions .new-version'] = 'actionNewVersion';
            this.events['submit .actions #document-search-form'] = 'onQuickSearch';
            this.events['click .actions .advanced-search-button'] = 'onAdvancedSearchButton';
            this.events['click .actions .edit-acl'] = 'onEditAcl';
            Backbone.Events.on('folder-delete:error',this.onError);
        },

        rendered: function () {

            this.checkoutGroup = this.$('.actions .checkout-group');
            this.checkoutButton = this.$('.checkout');
            this.undoCheckoutButton = this.$('.undocheckout');
            this.checkinButton = this.$('.checkin');
            this.deleteButton = this.$('.actions .delete');
            this.tagsButton = this.$('.actions .tags');
            this.newVersionButton = this.$('.actions .new-version');
            this.aclButton = this.$('.actions .edit-acl');
            this.notifications = this.$('>.notifications');

            this.tagsButton.show();

            this.listView = this.addSubView(
                new DocumentListView({
                    el: '#list-' + this.cid,
                    collection: this.collection
                })
            );
            var self = this;
            this.collection.fetch({reset: true}).error(function(err) {
                self.onError(null,err);
            });

            this.listenTo(this.listView, 'selectionChange', this.onStateChange);
            this.listenTo(this.collection, 'change', this.onStateChange);
            this.listenTo(this.collection, 'add', this.highlightAddedView);

            this.$('.tabs').tabs();
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
                    var canUndo = document.getLastIteration().get('iteration') > 1;
                    this.updateActionsButton(false, canUndo, true);
                } else {
                    this.updateActionsButton(false, false, false);
                }
            } else {
                this.newVersionButton.prop('disabled', false);
                this.updateActionsButton(true, false, false);
            }


            if ((App.config.workspaceAdmin || document.attributes.author.login === App.config.login)) {
                this.aclButton.show();
            }

        },
        areAllDocumentsCheckedOut: function () {
            var isCheckedOut = true;
            this.listView.eachChecked(function (view) {
                if (!view.model.isCheckout()) {
                    isCheckedOut = false;
                }
            });
            return isCheckedOut;
        },
        areAllDocumentsNotCheckedOut: function () {
            var isNotCheckedOut = true;
            this.listView.eachChecked(function (view) {
                if (view.model.isCheckout()) {
                    isNotCheckedOut = false;
                }
            });
            return isNotCheckedOut;
        },
        areAllDocumentsCheckedOutByConnectedUser: function () {
            var isCheckedOutByMe = true;
            this.listView.eachChecked(function (view) {
                if (!view.model.isCheckoutByConnectedUser()) {
                    isCheckedOutByMe = false;
                }
            });
            return isCheckedOutByMe;
        },
        isNotThefirstIteration: function () {
            var notFirstIteration = true;
            this.listView.eachChecked(function (view) {
                if (view.model.getLastIteration().get('iteration') <= 1) {
                    notFirstIteration = false;
                }
            });
            return notFirstIteration;
        },
        showCheckinCheckoutUndoCheckoutButtons: function () {
            if (this.areAllDocumentsCheckedOut()) {
                if (this.areAllDocumentsCheckedOutByConnectedUser()) {
                    this.updateActionsButton(false, this.isNotThefirstIteration(), true);
                } else {
                    this.updateActionsButton(false, false, false);
                }
            } else {
                if (this.areAllDocumentsNotCheckedOut()) {
                    this.updateActionsButton(true, false, false);
                } else {
                    this.updateActionsButton(false, false, false);
                }
            }
        },
        onSeveralDocumentsSelected: function () {
            this.deleteButton.show();
            this.newVersionButton.hide();
            this.checkoutGroup.css('display', 'inline-block');
            this.showCheckinCheckoutUndoCheckoutButtons();
            this.aclButton.hide();
        },

        updateActionsButton: function (canCheckout, canUndo, canCheckin) {
            this.checkoutButton.prop('disabled', !canCheckout);
            this.undoCheckoutButton.prop('disabled', !canUndo);
            this.checkinButton.prop('disabled', !canCheckin);
        },

        actionCheckout: function () {

            var self = this;

            var selectedDocuments = this.listView.checkedViews();

            var queueCheckOut= async.queue(function(docView,callback){
                docView.model.checkout().success(callback);
            });

            queueCheckOut.drain = function(){
                self.multipleCheckInCheckOutDone(selectedDocuments);
            };

            queueCheckOut.push(selectedDocuments);
            return false;
        },

        actionUndocheckout: function () {
            var self = this;
            bootbox.confirm(App.config.i18n.UNDO_CHECKOUT_QUESTION, function (result) {
                if (result) {

                    var selectedDocuments = self.listView.checkedViews();
                    var queueUndoCheckOut= async.queue(function(docView,callback){
                            docView.model.undocheckout().success(callback);
                        });
                    queueUndoCheckOut.drain = function(){
                        self.multipleCheckInCheckOutDone(selectedDocuments);
                    };

                    queueUndoCheckOut.push(selectedDocuments);
                }
            });
            return false;
        },

        actionCheckin: function () {
            var selectedDocuments = this.listView.checkedViews();
            var selectedDocumentsWithoutNote = 0;

            _.each(selectedDocuments, function (selectedDocView) {
                if (!selectedDocView.model.getLastIteration().get('revisionNote')) {
                    selectedDocumentsWithoutNote++;
                }
            });

            var self = this;

            if (selectedDocumentsWithoutNote > 0) {
                var promptView = new PromptView();

                if (selectedDocuments.length > 1) {
                    promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.DOCUMENT_REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
                } else {
                    promptView.setPromptOptions(App.config.i18n.REVISION_NOTE, App.config.i18n.REVISION_NOTE_PROMPT_LABEL, App.config.i18n.REVISION_NOTE_PROMPT_OK, App.config.i18n.REVISION_NOTE_PROMPT_CANCEL);
                }

                promptView.specifyInput('textarea');
                window.document.body.appendChild(promptView.render().el);
                promptView.openModal();

                this.listenTo(promptView, 'prompt-ok', function (args) {

                    var iterationNote = args[0];
                    if(_.isEqual(iterationNote, '')){
                        iterationNote = null;
                    }

                    var queueCheckIn = async.queue(function(docView, callback) {
                        var revisionNote;
                        if (iterationNote) {
                            revisionNote = docView.model.getLastIteration().get('revisionNote');
                            if (!revisionNote) {
                                revisionNote = iterationNote;
                            }
                        }

                        docView.model.getLastIteration().save({
                            revisionNote: revisionNote
                        }).success(function(){
                            docView.model.checkin().success(callback);
                        });
                    });

                    queueCheckIn.drain = function(){
                        self.multipleCheckInCheckOutDone(selectedDocuments);
                    };

                    queueCheckIn.push(selectedDocuments);
                });

                this.listenTo(promptView, 'prompt-cancel', function () {
                    var queueCheckIn= async.queue(function(docView,callback){
                        docView.model.checkin().success(callback);
                    });

                    queueCheckIn.drain = function(){
                        self.multipleCheckInCheckOutDone(selectedDocuments);
                    };

                    queueCheckIn.push(selectedDocuments);
                });

            } else {
                var queueCheckIn = async.queue(function(docView, callback) {
                    docView.model.getLastIteration().save().success(function(){
                        docView.model.checkin().success(callback);
                    });
                });

                queueCheckIn.drain = function(){
                    self.multipleCheckInCheckOutDone(selectedDocuments);
                };

                queueCheckIn.push(selectedDocuments);
            }

            return false;
        },

        multipleCheckInCheckOutDone: function (selectedDocuments) {
            var that = this;
            this.collection.fetch({reset: true}).success(function(){
                that.listView.checkCheckboxes(selectedDocuments);
            });
            Backbone.Events.trigger('document:iterationChange');
        },

        actionDelete: function () {
            var that = this;

            bootbox.confirm(App.config.i18n.DELETE_SELECTION_QUESTION, function (result) {
                if (result) {

                    var checkedViews = that.listView.checkedViews();
                    var requestsToBeDone = checkedViews.length;
                    var requestsDone = 0;

                    var onRequestOver = function () {
                        if (++requestsDone === requestsToBeDone) {
                            that.listView.redraw();
                            that.collection.fetch();
                            that.onStateChange();
                            Backbone.Events.trigger('document:iterationChange');
                        }
                    };

                    that.listView.eachChecked(function (view) {
                        view.model.destroy({
                            wait: true,
                            dataType: 'text', // server doesn't send a json hash in the response body
                            success: onRequestOver,
                            error: function (model, err) {
                                that.onError(model, err);
                                onRequestOver();
                            }
                        });
                    });
                }
            });

            return false;
        },

        onError: function (model, error) {
            var errorMessage = error ? error.responseText : model;

            this.notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
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
            });

            window.document.body.appendChild(newVersionView.render().el);

            newVersionView.openModal();

            return false;

        },

        onQuickSearch: function (e) {

            if (e.target.children[0].value) {
                App.router.navigate(App.config.workspaceId + '/search/q=' + e.target.children[0].value, {trigger: true});
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
                    acl: documentChecked.get('acl')
                });

                aclEditView.setTitle(documentChecked.getReference());
                window.document.body.appendChild(aclEditView.render().el);

                aclEditView.openModal();
                aclEditView.on('acl:update', function () {

                    var acl = aclEditView.toList();

                    documentChecked.updateACL({
                        acl: acl || {userEntries: {}, groupEntries: {}},
                        success: function () {
                            documentChecked.set('acl', acl);
                            aclEditView.closeModal();
                            that.listView.redraw();
                        },
                        error: function () {
                            window.alert(App.config.i18n.EDITION_ERROR);
                        }
                    });
                });
            }
            return false;
        },

        highlightAddedView: function (model) {
            this.listView.redraw();
            var addedView = _.find(this.listView.subViews, function (view) {
                return view.model === model;
            });
            if (addedView) {
                addedView.$el.highlightEffect();
            }
        }

    });
    return ContentDocumentListView;
});
