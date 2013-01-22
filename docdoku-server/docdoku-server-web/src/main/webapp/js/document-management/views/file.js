define([
    "text!templates/file.html"
], function (template) {
    var FileView = Backbone.View.extend({

        tagName: "li",
        className: 'file',

        events: {
            "change input.file-check" : "fileCheckChanged"
        },

        initialize: function() {
            this.model.url = this.options.deleteBaseUrl+"/files/"+this.model.get("shortName");
        },

        fileCheckChanged: function() {
            if (this.checkbox.is(":checked")) {
                this.fileNameEl.addClass("stroke");
                this.options.filesToDelete.add(this.model);
            } else {
                this.fileNameEl.removeClass("stroke");
                this.options.filesToDelete.remove(this.model);
            }
        },

        render: function() {
            this.$el.html(Mustache.render(template,
                {
                    created: this.model.isCreated(),
                    url: this.model.isCreated() ? this.options.baseUrl+this.model.get("shortname") : false,
                    shortName: this.model.get("shortName")
                }
            ));

            this.bindDomElements();

            return this;
        },

        bindDomElements: function(){
            this.checkbox = this.$("input.file-check");
            this.fileNameEl = this.$(".fileName");
        }
    });
    return FileView;
});
