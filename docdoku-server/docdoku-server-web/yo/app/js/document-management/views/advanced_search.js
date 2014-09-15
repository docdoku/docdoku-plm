/*global define*/
define([
        "backbone",
        "mustache",
        "text!templates/search_document_advanced_form.html",
        "common-objects/collections/users",
        "common-objects/views/attributes/attribute_list",
        "collections/template"
    ],

    function (Backbone,Mustache, template, Users, DocumentAttributeListView, Templates) {

        var AdvancedSearchView = Backbone.View.extend({

            events: {
                "hidden #advanced_search_modal": "onHidden",
                "submit #advanced_search_form": "onSubmitForm",
                "click #search-add-attributes": "addAttribute",
                "change #template-attributes-helper": "changeAttributes"
            },

            initialize: function () {
                _.bindAll(this);

            },

            render: function () {
                this.$el.html(Mustache.render(template, {i18n: APP_CONFIG.i18n}));
                this.bindDomElements();
                this.fillInputs();
                this.initAttributesView();
                return this;
            },

            initAttributesView: function () {

                this.attributes = new Backbone.Collection();

                this.attributesView = new DocumentAttributeListView({
                    collection: this.attributes
                });

                this.$("#attributes-list").html(this.attributesView.$el);

            },

            fillInputs: function () {

                var that = this;

                this.users = new Users();
                this.users.fetch({reset: true, success: function () {
                    that.users.each(function (user) {
                        that.$author.append("<option value='" + user.get("login") + "'>" + user.get("name") + "</option>");
                    });
                }});

                this.templates = new Templates();
                this.types = [];
                this.templatesId = [];
                this.templates.fetch({reset: true, success: function () {
                    that.templates.each(function (template) {
                        var type = template.get("documentType");
                        if (!_.contains(that.types, type) && type) {
                            that.types.push(type);
                            that.$type.append("<option value='" + type + "'>" + type + "</option>");
                        }
                        var templateId = template.get("id");
                        if (!_.contains(that.templatesId, templateId) && templateId) {
                            that.templatesId.push(type);
                            that.$templatesId.append("<option value='" + templateId + "'>" + templateId + "</option>");
                        }
                    });
                }});

            },

            addAttribute: function () {
                this.attributes.add({
                    name: "",
                    type: "TEXT",
                    value: ""
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
                    App.router.navigate(APP_CONFIG.workspaceId + "/search/" + queryString, {trigger: true});
                    this.closeModal();
                }
                return false;
            },

            bindDomElements: function () {
                this.$modal = this.$('#advanced_search_modal');
                this.$id = this.$("#search-id");
                this.$title = this.$("#search-title");
                this.$type = this.$("#search-type");
                this.$version = this.$("#search-version");
                this.$author = this.$("#search-author");
                this.$tags = this.$("#search-tags");
                this.$content = this.$("#search-content");
                this.$from = this.$("#search-from");
                this.$to = this.$("#search-to");
                this.$templatesId = this.$("#template-attributes-helper");
            },

            changeAttributes: function (e) {
                if (e.target.value) {
                    var search = _.where(this.templates.models, {id: e.target.value});
                    if (search[0]) {
                        this.attributes.reset(search[0].get("attributeTemplates"));
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
                var tags = this.$tags.val().replace(/ /g, "");
                var content = this.$content.val();
                var from = this.$from.val();
                var to = this.$to.val();

                var queryString = "";

                if (id) {
                    queryString += "id=" + id;
                }
                if (title) {
                    queryString += "&title=" + title;
                }
                if (type) {
                    queryString += "&type=" + type;
                }
                if (version) {
                    queryString += "&version=" + version;
                }
                if (author) {
                    queryString += "&author=" + author;
                }
                if (tags) {
                    queryString += "&tags=" + tags;
                }
                if (content) {
                    queryString += "&content=" + content;
                }
                if (from) {
                    queryString += "&from=" + new Date(from).getTime().toString();
                }
                if (to) {
                    queryString += "&to=" + new Date(to).getTime().toString();
                }

                if (this.attributes.length) {
                    queryString += "&attributes=";
                    this.attributes.each(function (attribute) {
                        var type = attribute.get("type");
                        var name = attribute.get("name");
                        var value = attribute.get("value");
                        value = type == "BOOLEAN" ? (value ? "1" : "0") : value;
                        queryString += type + ":" + name + ":" + value + ";";
                    });
                    // remove last '+'
                    queryString = queryString.substr(0, queryString.length - 1);
                }

                return queryString;

            }

        });

        return AdvancedSearchView;

    });