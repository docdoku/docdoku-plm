/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/search_document_advanced_form.html',
    'common-objects/collections/users',
    'common-objects/views/attributes/attribute_list',
    'collections/template',
    'common-objects/utils/date',
    'common-objects/collections/lovs'
], function (Backbone,Mustache, template, Users, DocumentAttributeListView, Templates, date,LOVCollection) {
    'use strict';
    var AdvancedSearchView = Backbone.View.extend({

        events: {
            'hidden #advanced_search_modal': 'onHidden',
            'submit #advanced_search_form': 'onSubmitForm',
            'click #search-add-attributes': 'addAttribute',
            'change #template-attributes-helper': 'changeAttributes'
        },

        lovs : new LOVCollection(),

        initialize: function () {
            _.bindAll(this);

        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n, timeZone:App.config.timeZone}));
            this.bindDomElements();
            this.fillInputs();
            this.initAttributesView();
            return this;
        },

        initAttributesView: function () {

            this.attributes = new Backbone.Collection();

            var that = this;
            this.lovs.fetch().success(function(){
                that.attributesView = new DocumentAttributeListView({
                    collection: that.attributes,
                    lovs : that.lovs,
                    displayOnly: true
                });

                that.$('#attributes-list').html(that.attributesView.$el);
            });

        },

        fillInputs: function () {

            var that = this;

            this.users = new Users();
            this.users.fetch({reset: true, success: function () {
                that.users.each(function (user) {
                    that.$author.append('<option value="' + user.get('login') + '">' + user.get('name') + '</option>');
                });
            }});

            this.templates = new Templates();
            this.types = [];
            this.templatesId = [];
            this.templates.fetch({reset: true, success: function () {
                that.templates.each(function (template) {
                    var type = template.get('documentType');
                    if (!_.contains(that.types, type) && type) {
                        that.types.push(type);
                        that.$type.append('<option value="' + type + '">' + type + '</option>');
                    }
                    var templateId = template.get('id');
                    if (!_.contains(that.templatesId, templateId) && templateId) {
                        that.templatesId.push(type);
                        that.$templatesId.append('<option value="' + templateId + '">' + templateId + '</option>');
                    }
                });
            }});

        },

        addAttribute: function () {
            this.attributes.add({
                name: '',
                type: 'TEXT',
                value: ''
            });
        },

        openModal: function () {
            this.$modal.modal('show');
        },

        closeModal: function () {
            this.$modal.modal('hide');
        },

        onHidden: function () {
            this.remove();
        },

        onSubmitForm: function () {
            var queryString = this.constructQueryString();
            if (queryString) {
                App.router.navigate(App.config.workspaceId + '/search/' + encodeURIComponent(queryString), {trigger: true});
                this.closeModal();
            }
            return false;
        },

        bindDomElements: function () {
            this.$modal = this.$('#advanced_search_modal');
            this.$id = this.$('#search-id');
            this.$title = this.$('#search-title');
            this.$type = this.$('#search-type');
            this.$version = this.$('#search-version');
            this.$author = this.$('#search-author');
            this.$tags = this.$('#search-tags');
            this.$content = this.$('#search-content');
            this.$createdFrom = this.$('#search-creation-from');
            this.$createdTo = this.$('#search-creation-to');
            this.$modifiedFrom = this.$('#search-modification-from');
            this.$modifiedTo = this.$('#search-modification-to');
            this.$templatesId = this.$('#template-attributes-helper');
        },

        changeAttributes: function (e) {
            if (e.target.value) {
                var search = _.where(this.templates.models, {id: e.target.value});
                if (search[0]) {
                    this.attributes.reset(search[0].get('attributeTemplates'));
                }
            } else {
                this.attributes.reset();
            }
        },

        constructQueryString: function () {

            var id = this.$id.val();
            var title = this.$title.val();
            var type = this.$type.val();
            var version = this.$version.val();
            var author = this.$author.val();
            var tags = this.$tags.val().replace(/ /g, '');
            var content = this.$content.val();
            var createdFrom = this.$createdFrom.val();
            var createdTo = this.$createdTo.val();
            var modifiedFrom = this.$modifiedFrom.val();
            var modifiedTo = this.$modifiedTo.val();

            var queryString = '';

            if (id) {
                queryString += '&id=' + id;
            }
            if (title) {
                queryString += '&title=' + title;
            }
            if (type) {
                queryString += '&type=' + type;
            }
            if (version) {
                queryString += '&version=' + version;
            }
            if (author) {
                queryString += '&author=' + author;
            }
            if (tags) {
                queryString += '&tags=' + tags;
            }
            if (content) {
                queryString += '&content=' + content;
            }
            if (createdFrom) {
                queryString += '&createdFrom=' + date.toUTCWithTimeZoneOffset(createdFrom);
            }
            if (createdTo) {
                queryString += '&createdTo=' + date.toUTCWithTimeZoneOffset(createdTo);
            }
            if (modifiedFrom) {
                queryString += '&modifiedFrom=' + date.toUTCWithTimeZoneOffset(modifiedFrom);
            }
            if (modifiedTo) {
                queryString += '&modifiedTo=' + date.toUTCWithTimeZoneOffset(modifiedTo);
            }

            if (this.attributes.length) {
                queryString += '&attributes=';
                this.attributes.each(function (attribute) {
                    var type = attribute.get('type');
                    var name = attribute.get('name');
                    var value = attribute.get('value');
                    value = type === 'BOOLEAN' ? (value ? 'true' : 'false') : value;
                    value = type === 'LOV' ? attribute.get('items')[value].name : value;
                    queryString += type + ':' + name + ':' + value + ';';
                });
                // remove last '+'
                queryString = queryString.substr(0, queryString.length - 1);
            }

            return queryString;

        }

    });

    return AdvancedSearchView;

});
