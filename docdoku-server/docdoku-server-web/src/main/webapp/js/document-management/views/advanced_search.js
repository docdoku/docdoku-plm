define(    [
        "text!templates/search_document_advanced_form.html",
        "i18n!localization/nls/document-management-strings"
    ],

    function (template, i18n) {

    var AdvancedSearchView = Backbone.View.extend({

        events: {
            "hidden #advanced_search_modal": "onHidden",
            "submit #advanced_search_form" : "onSubmitForm"
        },

        template: Mustache.compile(template),

        initialize: function() {
            _.bindAll(this);
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));
            this.$modal = this.$('#advanced_search_modal');
            return this;
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
            this.router.navigate("search/"+this.constructQueryString(), {trigger: true});
            this.closeModal();
            return false;
        },


        constructQueryString : function(){

            var id = this.$("#search-id").val();
            var title = this.$("#search-title").val();
            var type = this.$("#search-type").val();
            var version = this.$("#search-version").val();
            var author = this.$("#search-author").val();
            var tags = this.$("#search-tags").val();
            var content = this.$("#search-content").val();

            var queryString = "id="+id;

            if(title) queryString += "&title="+title;
            if(type) queryString += "&type="+type;
            if(version) queryString += "&version="+version;
            if(author) queryString += "&author="+author;
            if(tags) queryString += "&tags="+tags;
            if(content) queryString += "&content="+content;

            return queryString;

        }

    });

    return AdvancedSearchView;

});