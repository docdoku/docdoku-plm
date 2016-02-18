/**
 * Created by laurent on 18/02/16.
 */
define([
    'backbone',
    'common-objects/models/tag',
    'common-objects/collections/tag',
    'common-objects/views/components/modal',
    'text!templates/importer.html'
], function (Backbone,Tag, TagList, ModalView, template) {
    'use strict';
    var TagsManagementView = ModalView.extend({

        template: template,

        templateExtraData: {},

        initialize: function () {
            ModalView.prototype.initialize.apply(this, arguments);
        }


    });
        return TagsManagementView;

    });
