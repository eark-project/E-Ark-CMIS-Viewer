angular
    .module('eArkPlatform')
    .controller('FooterController', FooterController);

function FooterController($scope, serverVersionService) {
    var vm = this;
    //activate();
    function activate() {
        vm.isDevelopmentMode = document.location.hostname == "localhost";
        serverVersionService.getGitDetails().then(function (details) {
            vm.gitCommitId = details.gitCommitId;
            vm.gitBranch = details.gitBranch;
        });
    }
}