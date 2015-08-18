/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/search_part_advanced_form.html',
    'common-objects/collections/users',
    'common-objects/views/attributes/attribute_list',
    'collections/part_templates',
    'common-objects/utils/date',
    'common-objects/collections/lovs'
], function (Backbone, Mustache, template, Users, PartAttributeListView, Templates, date, LOVCollection) {
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
            this.$el.html(Mustache.render(template, {i18n: App.config.i18n,timeZone:App.config.timeZone}));
            this.bindDomElements();
            this.fillInputs();
            this.initAttributesView();
            return this;
        },

        initAttributesView: function () {

            this.attributes = new Backbone.Collection();

            var that = this;
            this.lovs.fetch().success(function(){
                that.attributesView = new PartAttributeListView({
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

        onSubmitForm: function () {
            var queryString = this.constructQueryString();
            if (queryString) {
                App.router.navigate(App.config.workspaceId + '/parts-search/' + encodeURIComponent(queryString), {trigger: true});
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
            this.$tags = this.$('#search-tags');
            this.$createdFrom = this.$('#search-creation-from');
            this.$createdTo = this.$('#search-creation-to');
            this.$modifiedFrom = this.$('#search-modification-from');
            this.$modifiedTo = this.$('#search-modification-to');
            this.$templatesId = this.$('#template-attributes-helper');
            this.$standardPart = this.$('input[name=search-standardPart]');
            this.$content = this.$('#search-content');
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
            var tags = this.$tags.val().replace(/ /g, '');
            var createdFrom = this.$createdFrom.val();
            var createdTo = this.$createdTo.val();
            var modifiedFrom = this.$modifiedFrom.val();
            var modifiedTo = this.$modifiedTo.val();
            var standardPart = this.$standardPart.filter(':checked').val() === 'all' ? null : this.$standardPart.filter(':checked').val();
            var content = this.$content.val();

            var queryString = '';

            if (number) {
                queryString += '&number=' + number;
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
            if (tags) {
                queryString += '&tags=' + tags;
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
            if (standardPart) {
                queryString += '&standardPart=' + standardPart;
            }
            if (content) {
                queryString += '&content=' + content;
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
            //replace first occurence of & to ?
            queryString = queryString.replace('&','?');
            return queryString;

        }

    });

    return AdvancedSearchView;

});
