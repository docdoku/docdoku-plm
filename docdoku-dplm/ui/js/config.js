require.config({

    baseUrl: './js',

    deps: [
        "lib/handlebars",
        "lib/less-1.3.3.min",
        "lib/backbone-1.0.0-min",
        "lib/bootstrap.min",
        "app",
        "global",
        "menu"
    ],

    paths: {
        "i18n": "lib/i18n-2.0.4.js",
        "localization": "localization",
        "text": "lib/text-2.0.10.js"
    },

    config: {
        i18n: {
            locale: "en"
        }
    },

    shim: {
    }

});