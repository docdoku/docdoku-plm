/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'collections/change_order_collection',
    'text!templates/change-orders/change_order_content.html',
    'views/change-orders/change_order_list',
    'views/change-orders/change_order_creation',
    'common-objects/views/tags/tags_management',
    'common-objects/views/security/acl_edit',
    'common-objects/views/alert',
    'text!common-objects/templates/buttons/delete_button.html',
    'text!common-objects/templates/buttons/tags_button.html',
    'text!common-objects/templates/buttons/ACL_button.html'
], function (Backbone, Mustache, ChangeOrderCollection, template, ChangeOrderListView, ChangeOrderCreationView, TagsManagementView, ACLEditView, AlertView, deleteButton, tagsButton, aclButton) {
	'use strict';
	var ChangeOrderContentView = Backbone.View.extend({
        events: {
            'click button.new-order': 'newOrder',
            'click button.delete': 'deleteOrder',
            'click button.edit-acl': 'actionEditAcl',
            'click button.tags': 'actionTags'
        },

        partials: {
            deleteButton: deleteButton,
            tagsButton: tagsButton,
            aclButton: aclButton
        },

        initialize: function () {
            _.bindAll(this);
            this.collection = new ChangeOrderCollection();
        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}, this.partials));

            this.bindDomElements();

            this.listView = new ChangeOrderListView({
                el: this.$('#order_table'),
                collection: this.collection
            }).render();

            this.listView.on('delete-button:display', this.changeDeleteButtonDisplay);
            this.listView.on('acl-button:display', this.changeAclButtonDisplay);
            this.listView.on('error', this.onError);
            this.tagsButton.show();
            this.tagsButton.prop('disabled', App.config.isReadOnly);

            this.$el.on('remove', this.listView.remove);
            return this;
        },

        bindDomElements: function () {
            this.deleteButton = this.$('.delete');
            this.aclButton = this.$('.edit-acl');
            this.tagsButton = this.$('.tags');
            this.$notifications = this.$el.find('.notifications').first();
        },

        newOrder: function () {
            var self = this;
            var orderCreationView = new ChangeOrderCreationView({
                collection: self.collection
            });
            window.document.body.appendChild(orderCreationView.render().el);
            orderCreationView.openModal();

        },

        deleteOrder: function () {
            this.listView.deleteSelectedOrders();
        },

        actionTags: function () {
            var changeOrdersChecked = new Backbone.Collection();


            this.listView.eachChecked(function (view) {
                changeOrdersChecked.push(view.model);
            });

            var tagsManagementView = new TagsManagementView({
                collection: changeOrdersChecked
            });
            window.document.body.appendChild(tagsManagementView.el);
            tagsManagementView.show();


            return false;
        },

        actionEditAcl: function () {
            var modelChecked = this.listView.getChecked();

            if (modelChecked) {
                var self = this;
                modelChecked.fetch();
                var aclEditView = new ACLEditView({
                    editMode: true,
                    acl: modelChecked.getACL()
                });

                aclEditView.setTitle(modelChecked.getName());
                window.document.body.appendChild(aclEditView.render().el);

                aclEditView.openModal();
                aclEditView.on('acl:update', function () {
                    var acl = aclEditView.toList();
                    modelChecked.updateACL({
                        acl: acl || {userEntries: {}, groupEntries: {}},
                        success: function () {
                            modelChecked.set('acl', acl);
                            aclEditView.closeModal();
                            self.listView.redraw();
                        },
                        error: function(model, error){
                            aclEditView.onError(model, error);
                        }
                    });

                });

            }
        },

        changeDeleteButtonDisplay: function (state) {
            if (state) {
                this.deleteButton.show();
            } else {
                this.deleteButton.hide();
            }
        },

        changeAclButtonDisplay: function (state) {
            if (state) {
                this.aclButton.show();
            } else {
                this.aclButton.hide();
            }
        },

        onError: function(model, error) {
            var errorMessage = error ? error.responseText : model;
            this.$notifications.append(new AlertView({
                type: 'error',
                message: errorMessage
            }).render().$el);
        }

    });

    return ChangeOrderContentView;
});
