(function() {

    // var oa2BaseURL = 'http://localhost:8080/demoa2';
    // var oa2BaseURL = 'https://azonzo.herokuapp.com';
    var oa2BaseURL = 'https://demoa2.herokuapp.com';

    var app = angular.module('angulapp', ['ngRoute','ngCookies']);

    app.config(function($routeProvider) {
        $routeProvider.when('/redirect',{
            templateUrl : 'token.html'
        }).when('/client',{
            templateUrl : 'client.html'
        }).when('/admin',{
            templateUrl : 'admin.html'
        }).otherwise({
            templateUrl : 'code.html'
        });
    });

    // ===========================
    // Store Service
    // ===========================
    app.service('StoreService',function($cookieStore) {
        // ==================
        // Admin Access Token
        // ==================
        this.setAdminAccessToken = function(t) {this.setValue('admin_access_token',t);};
        this.getAdminAccessToken = function() {return this.getValue('admin_access_token');};
        this.removeAdminAccessToken = function() {return this.removeValue('admin_access_token');};
        // =================
        // Access Token Data
        // =================
        this.setAccessTokenData = function(d) {this.setValue('access_token_data',d);};
        this.getAccessTokenData = function() {return this.getValue('access_token_data');};
        this.removeAccessTokenData = function() {return this.removeValue('access_token_data');};
        // =================
        // Client Data
        // =================
        this.setClientData = function(d) {this.setValue('client_data',d);};
        this.getClientData = function() {return this.getValue('client_data');};
        // =================
        // Redirect URI
        // =================
        this.setRedirectURI = function(u) {this.setValue('redirect_uri',u);};
        this.getRedirectURI = function() {return this.getValue('redirect_uri');};
        // =================
        // Get Set Delete
        // =================
        this.setValue = function(key,value) {$cookieStore.put(key,value);};
        this.getValue = function(key) {return $cookieStore.get(key);};
        this.removeValue = function(key) {return $cookieStore.remove(key);};
    });

    // ===========================
    // Client Controller
    // ===========================
    app.controller('clientController', function($scope,$http,$window,StoreService) {
        $scope.clients = [];
        $scope.error = false;

        $scope.getClients = function() {
            var adminToken = StoreService.getAdminAccessToken();
            var request = {
                method : 'GET',
                url : oa2BaseURL + '/admin/clients',
                headers : {'Authorization' : 'Bearer ' + adminToken}
            };
            $http(request).then(
                function(response) {
                    $scope.clients = response.data;
                    $scope.clients.sort(function(c1,c2){return c1.client_number - c2.client_number;});
                    // Remove "client0" as it is reserved for Auth Server Admin App
                    $scope.clients.shift();
                },
                function(response) {$scope.clients = null;}
            );
        };

        $scope.checkAdminAccessToken = function() {
            var adminToken = StoreService.getAdminAccessToken();
            var request = {
                method : 'GET',
                url : oa2BaseURL + '/oauth/check_token?token=' + adminToken,
                headers : {'Authorization' : 'Basic Y2xpZW50MDpQQDU1dzByZDA='}
            };
            $http(request).then(
                function(response) {
                    $scope.getClients();
                    $scope.clientNumber = $scope.initClientNumber();
                },
                function(response) {
                    $window.location.href = '#/admin';
                }
            );
        };
        $scope.checkAdminAccessToken();

        // Init dropdown from stored value
        $scope.initClientNumber = function() {
            var c = StoreService.getClientData();
            return c===undefined ? 1 : c.client_number;
        };

        // Lookup from dropdown selected value
        $scope.getClient = function() {
            for (i=0; i<$scope.clients.length; i++) {
                if ($scope.clientNumber==$scope.clients[i].client_number)
                    return $scope.clients[i];
            }
            return undefined;
        };

        $scope.selectClient = function() {
            StoreService.setClientData($scope.getClient());
            StoreService.removeAccessTokenData();
            $window.location.href = 'index.html';
        };
    });

    // ===========================
    // Code Controller
    // ===========================
    app.controller('codeController', function($scope,$location,$window,StoreService) {
        $scope.authURL = oa2BaseURL + '/oauth/authorize?response_type=code';
        $scope.clientData = StoreService.getClientData();

        if ($scope.clientData===undefined) {
            $window.location.href = '#/client';
            return;
        }
        var t = StoreService.getAccessTokenData();
        if (! (t===undefined)) {
            $window.location.href = '#/redirect';
            return;
        }

        // ===============================
        // Redirect to authorization page
        // ===============================
        $scope.getAuthorizationCode = function() {
            var redirectURI = $scope.buildRedirectURI();
            StoreService.setRedirectURI(redirectURI);

            var url = $scope.authURL;
            url += ('&redirect_uri=' + encodeURIComponent(redirectURI));
            url += ('&client_id=' + encodeURIComponent($scope.clientData.client_id));
            url += ('&ts=' + new Date().getTime());
            $window.location.href = url;
        };

        $scope.buildRedirectURI = function() {
            var uri = removeQuery($location.absUrl());
            uri += '#/redirect';
            return uri;
        };
    });

    // ===========================
    // Token Controller
    // ===========================
    app.controller('tokenController', function($scope,$location,$window,$http,StoreService) {
        // =================
        // Stored Data
        // =================
        $scope.clientData = StoreService.getClientData();
        $scope.tokenData = StoreService.getAccessTokenData();
        $scope.redirectURI = StoreService.getRedirectURI();
        // =================
        // Location
        // =================
        $scope.authorizationCode = getQueryParameter($location.absUrl(),'code');
        $scope.error = getQueryParameter($location.absUrl(),'error');
        $scope.errorDesc = getQueryParameter($location.absUrl(),'error_description');
        // =================
        // Display Data
        // =================
        $scope.checkData = {
            data : null,
            wheel : false
        };
        $scope.serviceCall = {
            method : 'GET',
            url : 'https://demoa2.herokuapp.com/things/A/1234',
            wheel : false,
            status : null,
            rq : null,
            rs : null
        };

        // ==================
        // Build exchange URL
        // ==================
        $scope.getExchangeURL = function() {
            var u = oa2BaseURL + '/oauth/token?grant_type=authorization_code';
            u += ('&client_id=' + $scope.clientData.client_id);
            u += ('&redirect_uri=' + encodeURIComponent($scope.redirectURI));
            u += ('&code=' + $scope.authorizationCode);
            return u;
        };

        // ==================
        // Exchange Code
        // ==================
        $scope.exchangeCodeForToken = function() {
            var request = {
                method : 'POST',
                url : $scope.getExchangeURL(),
                headers : {
                    'Authorization' : 'Basic ' + window.btoa($scope.clientData.client_id + ':' + $scope.clientData.client_secret)
                }
            };
            $http(request).success(function(data, status, headers, config){
                $scope.tokenData = data;
                StoreService.setAccessTokenData(data);
            }).error(function(data, status, headers, config){
                $scope.errorDesc = 'Could not exchange Authorization Code for Access Token';
            });
        };

        // ==================
        // Check Token
        // ==================
        $scope.checkAccessToken = function() {
            var request = {
                method : 'GET',
                url : oa2BaseURL + '/oauth/check_token?token=' + $scope.tokenData.access_token,
                headers : {
                    'Authorization' : 'Basic ' + window.btoa($scope.clientData.client_id + ':' + $scope.clientData.client_secret)
                }
            };
            $scope.checkData.data = null;
            $scope.checkData.wheel = true;
            $http(request).success(function(data, status, headers, config){
                $scope.checkData.data = data;
                $scope.checkData.wheel = false;
            }).error(function(data, status, headers, config){
                $scope.checkData.data = 'Could not check Access Token';
                $scope.checkData.wheel = false;
            });
        };

        // ==================
        // start over
        // ==================
        $scope.startOver = function() {
            StoreService.removeAccessTokenData();
            $window.location.href = 'index.html';
        };

        // ==================
        // Service Call
        // ==================
        $scope.makeServiceCall = function() {
            var request = {
                method : $scope.serviceCall.method,
                url : $scope.serviceCall.url,
                headers : {
                    'Authorization' : 'Bearer ' + $scope.tokenData.access_token
                },
                data : $scope.serviceCall.rq
            };
            $scope.serviceCall.wheel = true;
            $http(request).success(function(data, status, headers, config){
                $scope.serviceCall.rs = data;
                $scope.serviceCall.status = status;
                $scope.serviceCall.wheel = false;
            }).error(function(data, status, headers, config) {
                $scope.serviceCall.rs = data;
                $scope.serviceCall.status = status;
                $scope.serviceCall.wheel = false;
            });
        };
    });

    // =====================================
    // Admin Controller : Need admin access
    // Token to access client list
    // =====================================
    app.controller('adminController', function($scope,$http,$window,StoreService) {
        $scope.error = false;
        $scope.wheel = false;
        $scope.getAdminAccessToken = function() {
            $scope.wheel = true;
            var request = {
                method : 'POST',
                url : oa2BaseURL + '/oauth/token',
                headers : {'Authorization' : 'Basic Y2xpZW50MDpQQDU1dzByZDA=',
                           'content-type' : 'application/x-www-form-urlencoded'},
                data : 'grant_type=password&scope=ADMIN_READ ADMIN_WRITE&username='+$scope.username+'&password='+$scope.password
            };
            $http(request).then(
                function(response) {
                    var adminAccessToken = response.data.access_token;
                    StoreService.setAdminAccessToken(adminAccessToken);
                    $scope.error = false;
                    $scope.wheel = false;
                    $window.location.href = '#/client';
                },
                function(response) {
                    adminAccessToken = null;
                    $scope.wheel = false;
                    $scope.error = true;
                }
            );
        };
    });

    // ==================
    // Other
    // ==================

})();

// ===========================
// HTTP Management
// ===========================
function getQueryParameter(url,name) {
    var r = new RegExp('[\?&]' + name + '=([^&#]*)').exec(url);
    return r==null ? null : decodeURIComponent(r[1]);
}

function removeQuery(url) {
    var i=url.indexOf('?');
    return (i==-1) ? url : url.substring(0,i);
}
