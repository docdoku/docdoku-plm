define(    [
        "text!templates/search_document_advanced_form.html",
        "i18n!localization/nls/document-management-strings",
        "common-objects/collections/users",
        "common-objects/views/attributes/attribute_list",
        "collections/template"
    ],

    function (template, i18n, Users, DocumentAttributeListView, Templates) {

    var AdvancedSearchView = Backbone.View.extend({

        events: {
            "hidden #advanced_search_modal": "onHidden",
            "submit #advanced_search_form" : "onSubmitForm",
            "click #search-add-attributes" : "addAttribute"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);

        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.bindDomElements();
            this.fillInputs();
            this.initAttributesView();
            return this;
        },

        initAttributesView:function(){

            this.attributes = new Backbone.Collection();

            this.attributesView = new DocumentAttributeListView({
                collection: this.attributes
            });

            this.$("#attributes-list").html(this.attributesView.$el);

        },

        fillInputs:function(){

            var that = this ;

            this.users = new Users();
            this.users.fetch({success:function(){
                that.users.each(function(user){
                    that.$author.append("<option value='"+user.get("login")+"'>"+user.get("name")+"</option>");
                });
            }});

            this.templates = new Templates();
            this.types = [];
            this.templates.fetch({success:function(){
                that.templates.each(function(template){
                    var type = template.get("documentType");
                    if(!_.contains(that.types, type) && type){
                        that.types.push(type);
                        that.$type.append("<option value='"+type+"'>"+type+"</option>");
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

        setRouter :function(router){
            this.router = router;
        },

        openModal: function() {
            this.$modal.modal('show');
        },

        closeModal: function() {
            this.$modal.modal('hide');
        },

        onHidden: function() {
            this.remove();
        },

        onSubmitForm: function(e) {
            var queryString = this.constructQueryString() ;
            this.router.navigate("search/"+queryString, {trigger: true});
            this.closeModal();
            return false;
        },

        bindDomElements:function(){
            this.$modal   = this.$('#advanced_search_modal');
            this.$id      = this.$("#search-id");
            this.$title   = this.$("#search-title");
            this.$type    = this.$("#search-type");
            this.$version = this.$("#search-version");
            this.$author  = this.$("#search-author");
            this.$tags    = this.$("#search-tags");
            this.$content = this.$("#search-content");
            this.$from    = this.$("#search-from");
            this.$to      = this.$("#search-to");
        },

        constructQueryString : function(){

            var id      = this.$id.val();
            var title   = this.$title.val();
            var type    = this.$type.val();
            var version = this.$version.val();
            var author  = this.$author.val();
            var tags    = this.$tags.val().replace(/ /g,"");
            var content = this.$content.val();
            var from    = this.$from.val();
            var to      = this.$to.val();

            var queryString = "id="+id;

            if(title) queryString += "&title="+title;
            if(type) queryString += "&type="+type;
            if(version) queryString += "&version="+version;
            if(author) queryString += "&author="+author;
            if(tags) queryString += "&tags="+tags;
            if(content) queryString += "&content="+content;
            if(from) queryString += "&from="+new Date(from).getTime().toString();
            if(to) queryString += "&to="+new Date(to).getTime().toString();

            if(this.attributes.length){
                queryString += "&attributes=";
                this.attributes.each(function(attribute){
                    var type = attribute.get("type");
                    var name = attribute.get("name");
                    var value = attribute.get("value");
                    value = type == "BOOLEAN" ? (value ? "1": "0") : value ;
                    queryString += type + ":" + name + ":" + value + ";";
                });
                // remove last '+'
                queryString = queryString.substr(0, queryString.length-1);
            }

            return queryString;

        }

    });

    return AdvancedSearchView;

});