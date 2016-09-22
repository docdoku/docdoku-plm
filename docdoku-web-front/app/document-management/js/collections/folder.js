/*global define,App*/
define([
    'backbone',
    'models/folder'
], function (Backbone,Folder) {
	'use strict';

    //TODO : rename the file to FolderList
    var FolderList = Backbone.Collection.extend({

        model: Folder,

        className: 'FolderList',

        url: function () {
            var baseUrl = App.config.contextPath + '/api/workspaces/' + App.config.workspaceId + '/folders';
            if (this.parent) {
                return baseUrl + '/' + this.parent.get('id') + '/folders';
            } else {
                return baseUrl;
            }
        },

        parse: function (data) {
            if (!this.parent) {
                // inject the user home folder
                data.unshift({
                    id: App.config.workspaceId + ':~' + App.config.login,
                    name: '~' + App.config.login,
                    path: App.config.workspaceId,
                    home: true
                });
            }
            return data;
        },
        comparator: function (folderA, folderB) {
            // sort folders by name
            var nameA = folderA.get('name');
            var nameB = folderB.get('name');

            if (folderB.get('home')) {
                return 1;
            }
            if (folderA.get('home')) {
                return -1;
            }
            if (nameA === nameB) {
                return 0;
            }
            return (nameA < nameB) ? -1 : 1;
        }
    });

    return FolderList;
});
