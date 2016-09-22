/*global $,_,define,bootbox,App*/
define([
    'backbone',
    'mustache',
    'require',
    'common-objects/models/document/document_revision',
    'models/folder',
    'collections/folder',
    'common-objects/views/components/list_item',
    'views/folder_list',
    'views/folder_document_list',
    'views/folder_new',
    'views/folder_edit',
    'text!templates/folder_list_item.html'
], function (Backbone, Mustache, require, DocumentRevision, Folder, FolderList, ListItemView, FolderListView, FolderDocumentListView, FolderNewView, FolderEditView, template) {
    'use strict';
    var FolderListItemView = ListItemView.extend({

        template: template,

        tagName: 'li',
        className: 'folder FolderNavListItem',
        collection: function () {
            return new FolderList();
        },
        initialize: function () {
            ListItemView.prototype.initialize.apply(this, arguments);
            // jQuery creates it's own event object, and it doesn't have a
            // dataTransfer property yet. This adds dataTransfer to the event object.
            $.event.props.push('dataTransfer');

            this.isOpen = false;
            if (this.model) {
                this.collection.parent = this.model;
            }
            this.events = _.extend(this.events, {
                'click .header .new-folder': 'actionNewFolder',
                'click .header .edit': 'actionEdit',
                'click .header .delete': 'actionDelete',
                'mouseleave .header': 'hideActions',
                'dragenter >.nav-list-entry': 'onDragEnter',
                'dragover >.nav-list-entry': 'checkDrag',
                'dragleave >.nav-list-entry': 'onDragLeave',
                'dragstart >.nav-list-entry': 'onDragStart',
                'dragend >.nav-list-entry': 'onDragEnd',
                'drop >.nav-list-entry': 'onDrop'
            });
            this.events['click [data-target="#items-' + this.cid + '"]'] = 'forceShow';
            this.events['click .status'] = 'toggle';
            this.bind('shown', this.shown);
            this.bind('hidden', this.hidden);
        },
        modelToJSON: function () {
            var data = this.model.toJSON();
            if (data.id) {
                data.path = data.id.replace(/^[^:]*:?/, '');
                this.modelPath = data.path;
            }
            return data;
        },
        rendered: function () {
            var isHome = this.model ? this.model.get('home') : false;
            var isRoot = _.isUndefined(this.model);
            if (isHome) {
                this.$el.addClass('home');
            }
            if (isRoot || isHome) {
                this.$('.delete').remove();
                this.$('.edit').remove();
            }

            this.foldersView = this.addSubView(
                new FolderListView({
                    el: '#items-' + this.cid,
                    collection: this.collection
                })
            ).render();

            this.folderDiv = this.$('>.nav-list-entry');

        },
        forceShow: function (e) {
            var isRoot = _.isUndefined(this.model);
            if (isRoot) {
                this.show();
            } else {
                this.showContent();
                this.navigate();
            }
            e.stopPropagation();
            e.preventDefault();
            return false;
        },
        show: function (routePath) {
            if (routePath) {
                this.listenToOnce(this.collection, 'reset', this.traverse.bind(this));
            }
            this.routePath = routePath;
            this.isOpen = true;
            this.foldersView.show();
            this.trigger('shown');
        },
        shown: function () {
            this.$el.addClass('open');
            if (!_.isUndefined(this.routePath)) {
                // If from direct url access (address bar)
                // show documents only if not traversed
                var pattern = new RegExp('^' + this.modelPath);
                if (this.routePath.match(pattern)) {
                    this.showContent();
                }
            } else {
                // If not from direct url access (click)
                this.showContent();
                this.navigate();
            }
        },
        showContent: function () {
            this.setActive();
            this.addSubView(new FolderDocumentListView({
                model: this.model
            })).render();
        },
        hide: function () {
            this.isOpen = false;
            this.foldersView.hide();
            this.trigger('hidden');
        },
        hidden: function () {
            this.$el.removeClass('open');
        },
        navigate: function () {
            var path = this.modelPath ? '/' + encodeURIComponent(this.modelPath) : '';
            App.router.navigate(App.config.workspaceId + '/folders' + path, {trigger: false});
        },

        toggle: function () {
            if (this.isOpen) {
                this.hide();
            } else {
                this.show();
            }
            return false;
        },
        traverse: function () {
            if (this.routePath) {
                var routePath = this.routePath;
                _.each(this.foldersView.subViews, function (view) {
                    var pattern = new RegExp('^' + view.modelPath);
                    if (routePath.match(pattern)) {
                        view.show(routePath);
                    }
                });
            }
        },

        /** State */
        setActive: function () {
            if (App.$documentManagementMenu) {
                App.$documentManagementMenu.find('.active').removeClass('active');
            }
            this.$el.find('.header').first().addClass('active');
        },
        isActive: function () {
            return this.$el.find('.header').first().hasClass('active');
        },

        /** Action */
        hideActions: function () {
            // Prevents the actions menu to stay opened all the time
            this.$el.find('.header .btn-group').first().removeClass('open');
        },
        actionNewFolder: function () {
            this.hideActions();
            this.addSubView(
                new FolderNewView({
                    collection: this.collection
                })
            );
            return false;
        },
        actionEdit: function () {
            this.hideActions();
            new FolderEditView({
                model: this.model
            }).show();
            return false;
        },
        actionDelete: function () {
            this.hideActions();
            var that = this;
            bootbox.confirm(App.config.i18n.DELETE_FOLDER_QUESTION,
                App.config.i18n.CANCEL,
                App.config.i18n.DELETE,
                function (result) {
                    if (result) {
                        that.model.destroy({
                            wait: true,
                            dataType: 'text', // server doesn't send a json hash in the response body,
                            success: function () {
                                Backbone.Events.trigger('document:iterationChange');
                            },
                            error: function (model, res) {
                                Backbone.Events.trigger('folder-delete:error', model, res);
                            }
                        });
                    }
                });
            return false;
        },

        onDragEnter: function () {
            var that = this;

            if (!this.isOpen) {
                setTimeout(function () {
                    if (that.folderDiv.hasClass('move-doc-into')) {
                        that.isOpen = true;
                        that.foldersView.show();
                        that.$el.addClass('open');
                    }
                }, 500);
            }
        },

        checkDrag: function (e) {
            e.dataTransfer.dropEffect = 'copy';
            this.folderDiv.addClass('move-doc-into');
            return this.isActive();
        },

        onDragLeave: function (e) {
            e.dataTransfer.dropEffect = 'none';
            this.folderDiv.removeClass('move-doc-into');
        },

        onDragStart: function (e) {

            var data = JSON.stringify(this.model.toJSON());

            if (!data || this.model.get('home')) {
                e.preventDefault();
                e.stopPropagation();
                return false;
            }

            this.$el.addClass('moving');
            var that = this;
            Backbone.Events.on('folder-moved', function () {
                Backbone.Events.off('folder-moved');
                Backbone.Events.off('folder-error-moved');
                that.remove();
            });
            Backbone.Events.on('folder-error-moved', function () {
                Backbone.Events.off('folder-moved');
                Backbone.Events.off('folder-error-moved');
                that.$el.removeClass('moving');
            });


            e.dataTransfer.setData('folder:text/plain', data);

            var img = document.createElement('img');
            img.src = App.config.contextPath + '/images/icon-nav-folder-opened.png';
            e.dataTransfer.setDragImage(img, 0, 0);


            e.dataTransfer.dropEffect = 'none';
            e.dataTransfer.effectAllowed = 'copyMove';
            return e;


        },

        onDragEnd: function () {

        },

        onDrop: function (e) {
            if (e.dataTransfer.getData('document:text/plain')) {
                this.moveDocument(e);
            } else if (e.dataTransfer.getData('folder:text/plain')) {
                this.moveFolder(e);
            }
        },

        moveDocument: function (e) {

            var that = this;
            var documentRevision = new DocumentRevision(JSON.parse(e.dataTransfer.getData('document:text/plain')));

            var path = documentRevision.getWorkspace();
            if (this.model) {
                path = this.model.getPath() + '/' + this.model.getName();
            }
            documentRevision.moveInto(path, function () {
                Backbone.Events.trigger('document-moved');
                that.folderDiv.removeClass('move-doc-into');
                that.folderDiv.highlightEffect();
            }, function (error) {
                Backbone.Events.trigger('document-error-moved', null, error);
                that.folderDiv.removeClass('move-doc-into');
            });

        },

        moveFolder: function (e) {
            var data = JSON.parse(e.dataTransfer.getData('folder:text/plain'));
            var model = this.model || {id: App.config.workspaceId};
            var folder = new Folder(data);
            var that = this;

            folder.moveTo(model).success(function () {
                Backbone.Events.trigger('folder-moved');
                that.folderDiv.removeClass('move-doc-into');
                that.folderDiv.highlightEffect();
                that.show();
            }).error(function () {
                Backbone.Events.trigger('folder-error-moved');
                that.folderDiv.removeClass('move-doc-into');
            });

        }

    });
    return FolderListItemView;
});
