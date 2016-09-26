angular
    .module('openeApp.systemsettings')
    .controller('SystemSettingsController', SystemSettingsCtrl);

function SystemSettingsCtrl(systemSettingsPagesService, authService) {
    var vm = this;
    vm.pages = systemSettingsPagesService.getPages();
    vm.modulesPages = systemSettingsPagesService.getModulesPages();
}