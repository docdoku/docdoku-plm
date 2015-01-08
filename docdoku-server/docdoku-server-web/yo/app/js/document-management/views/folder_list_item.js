/*global $,_,define,App*/
define([
    'backbone',
    'mustache',
    'require',
    'models/document',
    'collections/folder',
    'common-objects/views/components/list_item',
    'views/folder_list',
    'views/folder_document_list',
    'views/folder_new',
    'views/folder_edit',
    'text!templates/folder_list_item.html'
], function (Backbone,Mustache, require, Document, FolderList, ListItemView, FolderListView, FolderDocumentListView, FolderNewView, FolderEditView, template) {
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
            this.templateExtraData = {
                isReadOnly: App.appView.isReadOnly()
            };
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
                'drop >.nav-list-entry': 'onDrop'
            });
            this.events['click [data-target="#items-' + this.cid + '"]'] = 'forceShow';
            this.events['click .status'] = 'toggle';

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
            this.bind('shown', this.shown);
            this.bind('hidden', this.hidden);

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
            App.router.navigate(App.config.workspaceId + '/configspec/' + App.config.configSpec + '/folders' + path, {trigger: false});
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
            if (confirm(App.config.i18n.DELETE_FOLDER_QUESTION)) {
                this.model.destroy({
                    dataType: 'text' // server doesn't send a json hash in the response body
                });
            }
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
            if (!_.isUndefined(e.dataTransfer.getData('document:text/plain'))) {
                e.dataTransfer.dropEffect = 'copy';
                this.folderDiv.addClass('move-doc-into');
                return this.isActive();
            }
            return true;
        },

        onDragLeave: function (e) {
            e.dataTransfer.dropEffect = 'none';
            this.folderDiv.removeClass('move-doc-into');
        },

        onDrop: function (e) {
            var that = this;
            var document = new Document(JSON.parse(e.dataTransfer.getData('document:text/plain')));

            var path = document.getWorkspace();
            if (this.model) {
                path = this.model.getPath() + '/' + this.model.getName();
            }
            document.moveInto(path, function () {
                Backbone.Events.trigger('document-moved');
                that.folderDiv.removeClass('move-doc-into');
                that.folderDiv.highlightEffect();
            }, function () {
                Backbone.Events.trigger('document-error-moved');
                that.folderDiv.removeClass('move-doc-into');

            });
        }
    });
    return FolderListItemView;
});
