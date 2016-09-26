angular
    .module('eArkPlatform.search', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config(modulesMenuServiceProvider) {
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/search/view/menuItem.html',
        order: 3
    });
}