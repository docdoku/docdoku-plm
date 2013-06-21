define([] , function() {
    var VersionedlFileModel = Backbone.Model.extend({

        initialize:function() {
            _.bindAll(this);
        },

        getName:function() {
            return this.get("name");
        },

        getFullPath:function() {
            return this.get("path") + "/" + this.getName();
        },

        getStatus:function() {
            return this.get("status");
        },

        getMTime:function() {
            return this.get("mtime");
        },

        getMTimeParsed:function() {
            return moment(this.getMTime()).format("YYYY-MM-DD HH:MM:ss");
        },

        getPartNumber:function() {
            return this.getStatus().partNumber;
        },

        getVersion:function() {
            return this.getStatus().version;
        },

        setMTime:function(mTime) {
            this.set("mtime", mTime);
        },

        setStatus:function(status) {
            this.set("status", status);
        },

        setPartNumber:function(partNumber) {
            this.set("partNumber", partNumber);
        }
    });

    return VersionedlFileModel;
});