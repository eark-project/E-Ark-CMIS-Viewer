angular.module('eArkPlatform.translations.init', []);

angular.module('eArkPlatform.translations', ['pascalprecht.translate'])
    .factory('availableLanguages', AvailableLanguages)
    .config(config);

/*
 * This is where you define the languages you'll want to use in your project.
 * Make sure to include the corresponding language files (ex en.json).
 */
var defineLangs = [
    {
        title: 'English (US)',
        code: 'en',
        locale: 'en_US'
    },
    {
        title: 'Dansk',
        code: 'da',
        locale: 'da_DK'
    }
];

var availableLanguages = {
    keys: [],
    localesKeys: {},
    languages: []
};

for (var l in defineLangs) {
    availableLanguages.keys.push(defineLangs[l].code);
    availableLanguages.localesKeys[defineLangs[l].locale] = defineLangs[l].code;
    availableLanguages.languages.push(
        {
            title: defineLangs[l].title,
            code: defineLangs[l].code,
            locale: defineLangs[l].locale
        }
    );
};

function AvailableLanguages() {
    return availableLanguages;
}

function config($translateProvider, languageFilesProvider) {
    languageFilesProvider.addFile('app/src/i18n/', '.json');
    $translateProvider.useStaticFilesLoader(languageFilesProvider.getLanguageFiles());

    $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
    $translateProvider
        .registerAvailableLanguageKeys(availableLanguages.keys, availableLanguages.localesKeys)
        .determinePreferredLanguage();

    //set default language if browsers langugage not found
    if (availableLanguages.keys.indexOf($translateProvider.preferredLanguage()) === -1) {
        $translateProvider.preferredLanguage(availableLanguages.keys[0]);
    }

    //Force a refresh of the translation because of a verified race condition issue
    //See: http://stackoverflow.com/a/31836226
    $translateProvider.forceAsyncReload(true);

}