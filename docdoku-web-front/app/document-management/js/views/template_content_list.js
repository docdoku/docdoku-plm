/*global define,bootbox,App*/
define([
    'collections/template',
    'views/content',
    'views/template_list',
    'views/template_new',
    'common-objects/views/security/acl_edit',
    'text!templates/template_content_list.html',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/ACL_button.html',
    'common-objects/views/alert',
    'common-objects/views/lov/lov_modal'
], function (TemplateList, ContentView, TemplateListView, TemplateNewView, ACLEditView, template, deleteButton, aclButton, AlertView, LOVModalView) {
    'use strict';
    var TemplateContentListView = ContentView.extend({

        template: template,

        partials: {
            deleteButton: deleteButton,
            aclButton: aclButton

        },

        collection: function () {
            return TemplateList.getInstance();
        },
        initialize: function () {
            ContentView.prototype.initialize.apply(this, arguments);
            this.events['click .actions .new-template'] = 'actionNew';
            this.events['click .actions .delete'] = 'actionDelete';
            this.events['click .actions .edit-acl'] = 'onEditAcl';
            this.events['click .actions .list-lov'] = 'showLovs';
        },
        bindDomElement: function () {
            this.$aclButton = this.$('.actions .edit-acl');
            this.$deleteButton = this.$('.actions .delete');

        },
        rendered: function () {
            this.listView = this.addSubView(new TemplateListView({
                el: '#list-' + this.cid,
                collection: this.collection
            }));
            this.listView.collection.fetch({reset: true});
            this.listView.on('selectionChange', this.selectionChanged);
            this.bindDomElement();
            this.selectionChanged();
        },
        selectionChanged: function () {

            var checkedViews = this.listView.checkedViews();
            switch (checkedViews.length) {
                case 0:
                    this.onNoTemplateSelected();
                    break;
                case 1:
                    this.onOneTemplateSelected(checkedViews[0].model);
                    break;
                default:
                    this.onSeveralTemplateSelected();
                    break;
            }
        },
        onNoTemplateSelected: function () {
            this.$aclButton.hide();
            this.$deleteButton.hide();
        },

        onOneTemplateSelected: function () {
            this.$aclButton.show();
            this.$deleteButton.show();

        },
        onSeveralTemplateSelected: function () {
            this.$aclButton.hide();
            this.$deleteButton.show();
        },
        onEditAcl: function () {

            var templateSelected;

            this.listView.eachChecked(function (view) {
                templateSelected = view.model;

            });
            var self = this;
            var aclEditView = new ACLEditView({
                editMode: true,
                acl: templateSelected.get('acl')
            });

            aclEditView.setTitle(templateSelected.getId());
            window.document.body.appendChild(aclEditView.render().el);

            aclEditView.openModal();
            aclEditView.on('acl:update', function () {

                var acl = aclEditView.toList();

                templateSelected.updateACL({
                    acl: acl || {userEntries: {}, groupEntries: {}},
                    success: function () {
                        templateSelected.set('acl', acl);
                        aclEditView.closeModal();
                        self.listView.redraw();
                    },
                    error: function (error) {
                        aclEditView.onError(error);
                    }
                });
            });

            return false;
        },

        actionNew: function () {
            this.addSubView(
                new TemplateNewView({
                    collection: this.collection
                })
            ).show();
            this.listView.redraw();
            return false;
        },
        actionDelete: function () {
            var that = this;

            bootbox.confirm(App.config.i18n.DELETE_SELECTION_QUESTION,
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function (result) {
                    if (result) {
                        that.listView.eachChecked(function (view) {
                            view.model.destroy({
                                dataType: 'text', // server doesn't send a json hash in the response body
                                success: function () {
                                    that.listView.redraw();
                                }
                            });
                        });
                    }
                });

            return false;
        },
        showLovs: function () {
            var lovmodal = new LOVModalView({});
            window.document.body.appendChild(lovmodal.render().el);
            lovmodal.openModal();
        }
    });
    return TemplateContentListView;
});
