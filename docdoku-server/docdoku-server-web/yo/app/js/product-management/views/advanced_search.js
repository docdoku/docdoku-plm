/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/search_part_advanced_form.html',
    'common-objects/collections/users',
    'common-objects/views/attributes/attribute_list',
    'collections/part_templates'
], function (Backbone, Mustache, template, Users, PartAttributeListView, Templates) {
    'use strict';
    var AdvancedSearchView = Backbone.View.extend({

        events: {
            'hidden #advanced_search_modal': 'onHidden',
            'submit #advanced_search_form': 'onSubmitForm',
            'click #search-add-attributes': 'addAttribute',
            'change #template-attributes-helper': 'changeAttributes'
        },

        initialize: function () {
            _.bindAll(this);

        },

        render: function () {
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n}));
            this.bindDomElements();
            this.fillInputs();
            this.initAttributesView();
            return this;
        },

        initAttributesView: function () {

            this.attributes = new Backbone.Collection();

            this.attributesView = new PartAttributeListView({
                collection: this.attributes
            });

            this.$('#attributes-list').html(this.attributesView.$el);

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
                    var type = template.get('partType');
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

        onSubmitForm: function (e) {
            var queryString = this.constructQueryString();
            if (queryString) {
                App.router.navigate(App.config.workspaceId + '/parts-search/' + queryString, {trigger: true});
                this.closeModal();
            }
            return false;
        },

        bindDomElements: function () {
            this.$modal = this.$('#advanced_search_modal');
            this.$number = this.$('#search-number');
            this.$name = this.$('#search-name');
            this.$type = this.$('#search-type');
            this.$version = this.$('#search-version');
            this.$author = this.$('#search-author');
            this.$from = this.$('#search-from');
            this.$to = this.$('#search-to');
            this.$templatesId = this.$('#template-attributes-helper');
            this.$standardPart = this.$('input[name=search-standardPart]');
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

            var number = this.$number.val();
            var name = this.$name.val();
            var type = this.$type.val();
            var version = this.$version.val();
            var author = this.$author.val();
            var from = this.$from.val();
            var to = this.$to.val();
            var standardPart = this.$standardPart.filter(':checked').val() === 'all' ? null : this.$standardPart.filter(':checked').val();

            var queryString = '';

            if (number) {
                queryString += 'number=' + number;
            }
            if (name) {
                queryString += '&name=' + name;
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
            if (from) {
                queryString += '&from=' + new Date(from).getTime().toString();
            }
            if (to) {
                queryString += '&to=' + new Date(to).getTime().toString();
            }
            if (standardPart) {
                queryString += '&standardPart=' + standardPart;
            }

            if (this.attributes.length) {
                queryString += '&attributes=';
                this.attributes.each(function (attribute) {
                    var type = attribute.get('type');
                    var name = attribute.get('name');
                    var value = attribute.get('value');
                    value = type === 'BOOLEAN' ? (value ? '1' : '0') : value;
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
