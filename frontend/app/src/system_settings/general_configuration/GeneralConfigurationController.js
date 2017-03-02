
angular
        .module('eArkPlatform.systemsettings')
        .controller('GeneralConfigurationController', GeneralConfigurationController);

function GeneralConfigurationController($mdDialog, $translate, $state, notificationUtilsService, sysConfigService, base64) {
    var vm = this;
    vm.loadList = loadList;
    vm.saveParameters = saveParameters;
    vm.repoDetails = {};
    vm.loadList();
    vm.toggleEditMode = toggleEditMode;
    vm.editMode = false;

    function loadList() {
        sysConfigService.getRepoDetails().then(function(data) {
            vm.repoDetails = data.repository;
            return data;
        });
    }

    function toggleEditMode(){
        vm.editMode = !vm.editMode;
    }

    function saveParameters(ev) {
        var confirm = $mdDialog.confirm()
                .title($translate.instant('COMMON.CONFIRM'))
                .textContent($translate.instant('ADMIN.SYS_SETTINGS.MESSAGES.SAVE_PROMPT'))
                .targetEvent(ev)
                .ok($translate.instant('COMMON.YES'))
                .cancel($translate.instant('COMMON.CANCEL'));
        $mdDialog.show(confirm).then(function() {
            toggleEditMode();
            vm.repoDetails.password = base64.encode(vm.repoDetails.password);
            sysConfigService.saveRepoDetails(vm.repoDetails).then(function() {
                notificationUtilsService.notify($translate.instant("ADMIN.SYS_SETTINGS.MESSAGES.SAVED_SUCCESSFULLY"));
                //reload full state
                $state.reload();
            });
        });
    }
}