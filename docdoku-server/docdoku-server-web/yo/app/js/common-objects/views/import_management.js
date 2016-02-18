/**
 * Created by laurent on 18/02/16.
 */
define([
    'backbone',
    'common-objects/models/tag',
    'common-objects/collections/tag',
    'common-objects/views/components/modal',
    'common-objects/views/tags/tag',
    'text!common-objects/templates/tags/tags_management.html'
], function (Backbone,Tag, TagList, ModalView, TagView, template) {
    'use strict';
    var TagsManagementView = ModalView.extend({

        template: template,

        templateExtraData: {},

    }
