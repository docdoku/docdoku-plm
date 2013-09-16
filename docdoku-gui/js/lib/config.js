// dirty hack to prevent text-2.0.3 to crash with undefined nodeRequire
window.require.nodeRequire = window.requireNode;
require.config({

    baseUrl: './js',

    deps: ["lib/backbone-1.0.0-min", "lib/handlebars", "lib/bootstrap.min", "lib/less-1.3.3.min", "app"],

    // 3rd party script alias names (Easier to type "jquery" than "libs/jquery-1.7.2.min")
    paths: {
        "require":"lib/require.min-2.1.2",
        "i18n": "lib/i18n-2.0.1",
        "localization": "localization",
        "text": "lib/text-2.0.3"
    },

    config: {
        i18n: {
            locale: "en"
        }
    },

    // Sets the configuration for your third party scripts that are not AMD compatible
    shim: {
    }
});