var ddk = new JavaImporter(
    com.docdoku.core.change,
    com.docdoku.core.common,
    com.docdoku.core.configuration,
    com.docdoku.core.document,
    com.docdoku.core.meta,
    com.docdoku.core.product,
    com.docdoku.core.query,
    com.docdoku.core.security,
    com.docdoku.core.sharing,
    com.docdoku.core.util,
    com.docdoku.core.workflow);


// import com.docdoku.core.common.*
var Account = Java.type("com.docdoku.core.common.Account");
var BinaryResource = Java.type("com.docdoku.core.common.BinaryResource");
var Organization = Java.type("com.docdoku.core.common.Organization");
var User = Java.type("com.docdoku.core.common.User");
var UserKey = Java.type("com.docdoku.core.common.UserKey");
var UserGroup = Java.type("com.docdoku.core.common.UserGroup");
var UserGroupKey = Java.type("com.docdoku.core.common.UserGroupKey");
var Version = Java.type("com.docdoku.core.common.Version");
var Workspace = Java.type("com.docdoku.core.common.Workspace");