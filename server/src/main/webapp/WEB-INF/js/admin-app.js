(function() {

    var app = angular.module('admin', ['ngRoute', 'ngCookies','angularModalService', 'ngAnimate']);
    var adminAccessTokenValue = null;
    var adminAccessToken = null;
    var adminWriter = false;
    var contextURL = getContextURL();

    app.config(function($routeProvider) {
        $routeProvider.when('/users',{
            templateUrl : 'users.html'
        }).when('/clients',{
            templateUrl : 'clients.html'
        }).when('/approvals',{
            templateUrl : 'approvals.html'
        }).when('/tokens',{
            templateUrl : 'tokens.html'
        }).otherwise({
            templateUrl : 'page0.html'
        }); 
    });

    // ===========================
    // Get Admin Items
    // ===========================
    var getAdminItems = function(scope,http,resource) {
        scope.items = [];
        scope.wheel = false;
        scope.error = null;
        var request = {
            method : 'GET',
            url : contextURL + '/admin/' + resource,
            headers : {'Authorization' : 'Bearer ' + adminAccessTokenValue}
        };
        http(request).then(
            function(response) {
                scope.items = response.data;
                scope.wheel = false;
                if (scope.sortItems)
                    scope.items.sort(scope.sortItems);
            },
            function(response) {
                scope.items = null;
                scope.wheel = false;
                scope.error = formatError(response);}
        );
    };

    // ===========================
    // Delete Admin Item
    // ===========================
    var deleteAdminItem = function(scope,http,resource,endOfURL) {
        var request = {
            method : 'DELETE',
            url : contextURL + '/admin/' + resource + endOfURL,
            headers : {'Authorization' : 'Bearer ' + adminAccessTokenValue}
        };
        scope.wheel = true;
        http(request).then(
            function(response) {scope.wheel = false; getAdminItems(scope,http,resource);},
            function(response) {scope.wheel = false; scope.error = formatError(response);}
        );
    };

    // ===========================
    // Client Controller
    // ===========================
    app.controller('clientController', function($scope,$http) {
        $scope.sortItems = function(c1,c2){return c1.client_number - c2.client_number;};
        getAdminItems($scope,$http,'clients');
    });

    // ===========================
    // User Controller
    // ===========================
    app.controller('userController', function($scope,$http) {
        $scope.sortItems = function(u1,u2){return u1.username.localeCompare(u2.username);};
        getAdminItems($scope,$http,'users');
    });

    // ===========================
    // Approval Controller
    // ===========================
    app.controller('approvalController', function($scope,$http) {
        $scope.write = adminWriter;
        getAdminItems($scope,$http,'approvals');

        $scope.revoke = function(u,c,s) {
            deleteAdminItem($scope,$http,'approvals','?user=' + u + '&client=' + c + '&scope=' + s);
        };

    });

    // ===========================
    // Token Details Controller
    // ===========================
    app.controller('tokenDetailsController', ['$scope', '$http', 'close', 'token', function($scope, $http, close, token) {
        $scope.close = close;
        $scope.token = token;
        $scope.data = null;
        $scope.wheel = true;
        var request = {
            method : 'GET',
            url : contextURL + '/user',
            headers : {'Authorization' : 'Bearer ' + token}
        };
        $http(request).then(
            function(response) {$scope.wheel = false; $scope.data = response.data;},
            function(response) {$scope.wheel = false; $scope.data = null;}
        );
    }]);

    // ===========================
    // Token Controller
    // ===========================
    app.controller('tokenController', function($scope,$http,ModalService) {

        $scope.tokenPopup = function(t) {
            ModalService.showModal({
                templateUrl : "tokenDetails.html",
                controller : "tokenDetailsController",
                inputs : {token : t}
            }).then(
                function(modal) {
                    modal.close.then(function(result) {
                    $scope.result = result;});
                }
            );
        };

        $scope.write = adminWriter;
        getAdminItems($scope,$http,'tokens');

        $scope.removeToken = function(token) {
            deleteAdminItem($scope,$http,'tokens','/'+token);
        };
    });

    // ===========================
    // Page0 Controller
    // ===========================
    app.controller('page0Controller', function($scope,$http,$cookies) {
        $scope.error = false;
        $scope.data = null;
        $scope.token = $cookies.get('ADMIN_ACCESS_TOKEN');
        adminAccessTokenValue = $scope.token;

        var request = {
            method : 'GET',
            url : contextURL + '/oauth/check_token?token=' + adminAccessTokenValue,
            headers : {'Authorization' : 'Basic Y2xpZW50MDpQQDU1dzByZDA='}
        };
        $http(request).then(
            function(response) {
                $scope.data = response.data;
                $scope.error = false;
                adminAccessToken = response.data;
                adminWriter = canWrite(adminAccessToken);
            },
            function(response) {
                $scope.data = response.data;
                $scope.error = true;
                adminAccessToken = null;
                adminWriter = false;
            }
        );

    });

    // ===========================
    // Other Controller
    // ===========================
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

function formatError(response) {
    return 'ERROR ' + response.status + ' ' + response.data.error_description;
}

function canWrite(token) {
    var scopes = token.scope;
    for (i=0; i<scopes.length; i++) {
        if ('ADMIN_WRITE' == scopes[i]) return true;
    }
    return false;
}

function getContextURL() {
    var n = document.URL.indexOf('/app/');
    return document.URL.substring(0,n);
}






