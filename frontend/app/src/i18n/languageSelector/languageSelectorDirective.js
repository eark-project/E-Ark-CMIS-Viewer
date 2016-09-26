/*
 * Language Selector Directive
 *
 * To use: Just add
 * 
 * <language-selector></language-selector>
 *
 * in any view HTML where you'll to use it. 
 */
angular
    .module('eArkPlatform')
    .directive('languageSelector', languageSelector);

function languageSelector() {

    return {
        restrict: 'E',
        scope: {},
        controller: function ($translate, availableLanguages) {

            var ctrl = this;

            ctrl.langs = availableLanguages.languages;
            ctrl.selected = $translate.use();

            ctrl.changeLanguage = function (lang) {
                $translate.use(lang);
                localStorage.setItem('language', lang);
            };

            // Set same langauge as chosen earlier
            if (localStorage.getItem('language')) {
                $translate.use(localStorage.getItem('language'));
                ctrl.selected = localStorage.getItem('language');
            }

        },
        controllerAs: 'ctrl',
        bindToController: true,
        templateUrl: './app/src/i18n/languageSelector/languageSelectorView.html'
    }
}