angular
    .module('eArkPlatform.erms')
    .provider('ermsService', ermsServiceProvider);

function ermsServiceProvider() {
    var modules = [];
    this.addERMSModule = addERMSModule;
    this.$get = ermsService;

    function addERMSModule(labelKey, sref, icon) {
        modules.push({
            labelKey: labelKey,
            sref: sref,
            icon: icon || 'content_copy'
        });
        return this;
    }

    function ermsService() {
        return {
            getModules: getModules
        };

        function getModules() {
            return modules;
        }
    }
}